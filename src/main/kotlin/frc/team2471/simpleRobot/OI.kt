package frc.team2471.simpleRobot

import org.wpilib.networktables.NetworkTableInstance
import org.wpilib.opmode.PeriodicOpMode
import org.wpilib.opmode.Teleop

object OI {
    private val table = NetworkTableInstance.getDefault().getTable("OI")


    init {
        println("inside OI init")

//        Robot.add

    }

    @Teleop(name = "Country Roads!")
    class TeleopMode: PeriodicOpMode() {
        val test = disabledPeriodic().apply { println("test") }

        init {
            println("TeleopMode selected")

        }

        override fun disabledPeriodic() {}

        override fun start() {
            println("Teleop Mode start!")
        }

        override fun periodic() {}

        override fun end() {
            println("Teleop Mode end!")
        }
    }
}