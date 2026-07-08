package frc.team2471.simpleRobot

import org.wpilib.command3.Command
import org.wpilib.command3.Trigger
import org.wpilib.driverstation.RobotState
import org.wpilib.hardware.hal.RobotMode
import org.wpilib.opmode.OpMode
import org.wpilib.units.Units.Seconds


object Autonomous {
    val autos: List<Command> = listOf(
        printFor10Seconds()
    )

    init {
        println("Autonomous init")

        autos.forEach {
            Robot.addOpModeFactory({ AutoOpMode(it) }, RobotMode.AUTONOMOUS, it.name())
            println("Registered ${it.name()} as an AutoOpMode")
        }
        Robot.publishOpModes()
    }

    /** Autonomous commands */

    private fun printFor10Seconds() = Command.requiring(Intake).executing {
        println("starting printFor20Seconds")
        while (true) {
            println("printing..")
            it.wait(Seconds.of(1.0))
        }
    }.whenCanceled {
        println("finished")
    }.named("Print for 20 seconds").withTimeout(Seconds.of(10.0))


    class AutoOpMode(command: Command): OpMode {
        val trigger = Trigger(RobotState::isEnabled)

        init {
            trigger.whileTrue(command)
            println("${command.name()} has been added to the trigger")
        }
    }
}