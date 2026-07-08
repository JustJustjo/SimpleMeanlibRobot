package frc.team2471.simpleRobot

import com.ctre.phoenix6.CANBus
import com.ctre.phoenix6.controls.DutyCycleOut
import com.ctre.phoenix6.hardware.TalonFX
import org.wpilib.command3.Command
import org.wpilib.command3.Mechanism



object Intake: Mechanism {
    val motor = TalonFX(if (Robot.isCompBot) 1 else 2, CANBus())


    init {
        defaultCommand = Command.requiring(Intake).executing { motor.setControl(DutyCycleOut(0.0)) }.named("Intake Idle")
    }


    fun intakingCommand() = Command.requiring(Intake).executing {
        while (true) {
            motor.setControl(DutyCycleOut(1.0))
            it.yield()
        }
    }.named("Intake Intaking")
}