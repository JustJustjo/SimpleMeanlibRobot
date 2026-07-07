package frc.team2471.frc2026

import com.ctre.phoenix6.swerve.utility.PhoenixPIDController
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.commands.onCancel
import org.team2471.frc.lib.commands.periodic
import org.team2471.frc.lib.commands.setDefaultCommand
import org.team2471.frc.lib.commands.use
import org.team2471.frc.lib.control.CurrentLimits
import org.team2471.frc.lib.control.LoopLogger
import org.team2471.frc.lib.control.rightStickButton
import org.team2471.frc.lib.ctre.currentLimits
import org.team2471.frc.lib.ctre.modifyConfiguration
import org.team2471.frc.lib.localization.PoseLocalizer
import org.team2471.frc.lib.math.cube
import org.team2471.frc.lib.math.square
import org.team2471.frc.lib.swerve.SwerveDriveSubsystem
import org.team2471.frc.lib.units.asMetersPerSecondPerSecond
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.inches
import org.team2471.frc.lib.math.DynamicInterpolatingTreeMap
import org.team2471.frc.lib.units.asRotation2d
import org.team2471.frc.lib.units.inchesPerSecond
import org.team2471.frc.lib.units.metersPerSecondPerSecond
import org.team2471.frc.lib.units.perSecond
import org.team2471.frc.lib.units.radians
import org.team2471.frc.lib.units.unWrap
import org.team2471.frc.lib.util.demoSpeed
import org.team2471.frc.lib.util.isBlueAlliance
import org.team2471.frc.lib.vision.Fiducial
import org.team2471.frc.lib.vision.QuixVisionCamera
import org.wpilib.command3.Command
import org.wpilib.driverstation.RobotState
import org.wpilib.math.controller.PIDController
import org.wpilib.math.geometry.Pose2d
import org.wpilib.math.geometry.Rotation2d
import org.wpilib.math.geometry.Translation2d
import org.wpilib.math.interpolation.Interpolator
import org.wpilib.math.interpolation.InverseInterpolator
import org.wpilib.math.kinematics.ChassisVelocities
import org.wpilib.networktables.NetworkTableInstance
import org.wpilib.system.Timer
import org.wpilib.units.measure.Angle
import kotlin.math.atan2


object Drive: SwerveDriveSubsystem(DriveConstants.drivetrainConstants, *DriveConstants.moduleConfigs) {

    // To reset position use this, also add other pose sources that need reset here.
    override var pose: Pose2d
        get() = savedState.Pose
        set(value) {
            resetPose(value)
        }

    override var heading: Rotation2d
        get() = pose.rotation
        set(value) {
            resetRotation(value)
            resetPoseTime = Timer.getMonotonicTimestamp()
        }
    override var centerOfRotation = Translation2d(0.0, 0.0)

    var headingAngleUnwrapped: Angle = heading.measure
        get() = heading.measure.unWrap(field)

    val headingHistory: DynamicInterpolatingTreeMap<Double, Double> = DynamicInterpolatingTreeMap(InverseInterpolator.forDouble(), Interpolator.forDouble(), 75)

    private var resetPoseTime = 0.0

    override val autoPilot = createAPObject(Double.POSITIVE_INFINITY.inchesPerSecond, 100.0.metersPerSecondPerSecond, 2.0.metersPerSecondPerSecond.perSecond, 0.5.inches, 1.0.degrees)
    val fastAutoPilot = createAPObject(Double.POSITIVE_INFINITY.inchesPerSecond, 100.0.metersPerSecondPerSecond, 5.0.metersPerSecondPerSecond.perSecond, 0.5.inches, 1.0.degrees)
    val slowAutoPilot = createAPObject(Double.POSITIVE_INFINITY.inchesPerSecond, 100.0.metersPerSecondPerSecond, 0.5.metersPerSecondPerSecond.perSecond, 0.25.inches, 1.0.degrees)

    override val pathXController = PIDController(7.0, 0.0, 0.0)
    override val pathYController = PIDController(7.0, 0.0, 0.0)
    override val pathThetaController = PIDController(8.0, 0.0, 0.0)

    override val autoDriveToPointController = PIDController(3.0, 0.0, 0.1)
    override val teleopDriveToPointController = PIDController(3.0, 0.0, 0.1)

    override val driveAtAnglePIDController = PhoenixPIDController(7.7, 0.0, 0.072)

    override val isDisabledSupplier: () -> Boolean = { Robot.isDisabled }

    /** false = paths made on the blue side, true = paths made on the red side */
    override val choreoPathsStartOnRed: Boolean = false

    init {
        println("inside Drive init")

        pose = Pose2d(3.0, 3.0, heading)

        println("max acceleration ${DriveConstants.kMaxAcceleration.asMetersPerSecondPerSecond}")

        setDefaultCommand {
            await(joystickVelocityDrive())
        }

        finalInitialization()
    }

    override fun periodic() {
        LoopLogger.record("Inside Drive periodic")

        super.periodic()
        LoopLogger.record("super Drive periodic")

        headingHistory.put(Timer.getMonotonicTimestamp(), heading.degrees)

        LoopLogger.record("Drive periodic")
    }

    /**
     * Sets all drive motor current limits to be the passed in [currentLimits].
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setDriveCurrentLimits(currentLimits: CurrentLimits) {
        GlobalScope.launch {
            modules.forEach {
                it.driveMotor.modifyConfiguration {
                    currentLimits(
                        currentLimits.continuousLimit,
                        currentLimits.peakLimit,
                        currentLimits.peakDuration
                    )
                }
            }
        }
    }

    /**
     * Returns [ChassisSpeeds] with a percentage power from the driver controller.
     */
    override fun getJoystickPercentageSpeed(): ChassisVelocities {
        val rawJoystick = OI.driveTranslation
        // Square drive input and apply demoSpeed
        val power = rawJoystick.norm.square() * demoSpeed * if (inSnakeMode) 0.8 else 1.0
        // Apply modified power to joystick vector and flip depending on alliance
        val joystickTranslation = rawJoystick * power * if (isBlueAlliance) -1.0 else 1.0

        val rawJoystickRotation = OI.driveRotation
        // Cube rotation input and apply demoSpeed
        val omega = rawJoystickRotation.cube() * demoSpeed

        return ChassisVelocities(joystickTranslation.x, joystickTranslation.y, omega)
    }

    var inSnakeMode = false
    fun snakeMode(): Command = use(Drive) {
        this.periodic {
            println("snake mode")
            inSnakeMode = true
            val driveTranslation = OI.driveTranslation
            if (driveTranslation.norm > 0.1) {
                driveAtAngle(
                    atan2(
                        driveTranslation.x,
                        -driveTranslation.y
                    ).radians.asRotation2d - Rotation2d(90.0.degrees)
                )
            } else {
                driveVelocity(getChassisVelocitiesFromJoystick().apply { omega = 0.0 })
            }
        }
    }.onCancel {
        inSnakeMode = false
    }

    fun zeroGyroCommand() = use(Drive) {
        println("zero gyro command")
        zeroGyro()
    }
}