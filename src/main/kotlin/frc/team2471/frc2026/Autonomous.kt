package frc.team2471.frc2026

//import frc.team2471.frc2026.tests.*
import org.team2471.frc.lib.autonomous.Autonomi
import org.team2471.frc.lib.autonomous.AutoRoutine
import org.team2471.frc.lib.autonomous.TestRoutine
import org.team2471.frc.lib.commands.onCancel
import org.team2471.frc.lib.commands.parallel
import org.team2471.frc.lib.commands.periodic
import org.team2471.frc.lib.commands.periodicTimeout
import org.team2471.frc.lib.commands.use
import org.team2471.frc.lib.commands.useUnnamed
import org.team2471.frc.lib.math.round
import org.team2471.frc.lib.units.feet
import org.team2471.frc.lib.units.meters
import org.team2471.frc.lib.units.seconds
import org.wpilib.command3.Command
import org.wpilib.command3.Scheduler
import org.wpilib.hardware.hal.RobotMode
import org.wpilib.math.geometry.Pose2d
import kotlin.math.absoluteValue


object Autonomous: Autonomi() {

//    val paths: MutableMap<String, Trajectory<SwerveSample>> = findChoreoPaths()  <-- already inside AutoMaker

    override val autos: List<AutoRoutine> = listOf(
        AutoRoutine("Print for 20 seconds", printFor20Seconds()),
    )

    override val tests: List<TestRoutine> = listOf(
        TestRoutine("Drive Set Angle Offsets", Drive.setAngleOffsets(), { Robot.disableAllDefaultCommands() }),
    )

    /** Supplier that sets the robot's pose. */
    override val drivePoseSetter: (Pose2d) -> Unit = { Drive.pose = it }

    /** Warmup function for speeding up auto loop times. Runs when selected auto changes. */
    override val warmupFunction: () -> Unit = {
        println("scheduling auto warmup")
        Scheduler.getDefault().schedule(warmupDriveAlongPath())
        println("finished scheduling auto warmup")
    }


    init {
        println("Autonomous init")
    }

    fun registerAutoOpModes() {
        autos.forEach {
            Robot.addOpModeFactory({ it.toAutoOpMode() }, RobotMode.AUTONOMOUS, it.name)
            println("Registered ${it.name} as an AutoOpMode")
        }
        tests.forEach {
            Robot.addOpModeFactory({ it.toTestOpMode() }, RobotMode.UTILITY, it.name)
            println("Registered ${it.name} TestOpMode")
        }
        Robot.publishOpModes()
    }

    /** Autonomous commands */

    private fun printFor20Seconds() = use(Drive) {
        println("starting printFor20Seconds")
        periodicTimeout(20.0) {
            println("dt time: ${it.round(2)}")
        }
        println("finished")
    }

    fun warmupDriveAlongPath() = useUnnamed(Drive) {
//        val warmupPath = paths["eightFoot"]!!//.sideToSideFlip(true) //TODO: UNCOMMENT WHEN 2027 CHOREO
//        await(Drive.driveAlongChoreoPath(warmupPath.getSplit(0).get(), exitSupplier = { percent, error -> percent >= 1.0 || Robot.isEnabled}))
        println("Warmup DAL")
    }.withPriority(Command.LOWEST_PRIORITY + 1).named("Warmup Drive Along Path")
}