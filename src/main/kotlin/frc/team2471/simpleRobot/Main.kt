@file:JvmName("Main") // set the compiled Java class name to "Main" rather than "MainKt"
package frc.team2471.simpleRobot

import org.wpilib.command3.Scheduler
import org.wpilib.framework.OpModeRobot
import org.wpilib.framework.RobotBase
import org.wpilib.system.RuntimeType
import java.net.NetworkInterface


/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 *
 * 2027-alpha: Changed robot to be a class instead of an object as wpilib does not support RobotBase as an object.
 * Most things can still function inside a companion object, although makes syntax slightly strange.
 */
object Robot : OpModeRobot(0.01) {
    val isCompBot = getCompBotBoolean()
    val scheduler = Scheduler.getDefault()

    val autonomous = Autonomous

    // Subsystems:
//    val intake = Intake
//    val oi = OI

//    var allSubsystems = arrayOf<Mechanism>(intake)

    init {
        println("Robot init")
        println("isCompBot = $isCompBot")

        println("Amount of autos ${Autonomous.autos.size}")

//        throw Exception("Evil initialization exception in Robot/OpModeRobot init")

        // Call all subsystems, make sure their init's run
//        allSubsystems.forEach { println("initializing subsystem ${it.name}") }



        println("Finished Robot init")
    }

    /** This function is called periodically during all modes.  */
    override fun robotPeriodic() {
        // Runs the Scheduler.  This is responsible for polling buttons, adding newly scheduled
        // commands, running already-scheduled commands, removing finished or interrupted commands,
        // and running subsystem periodic() methods.  This must be called from the robot's periodic
        // block in order for anything in the Command-based framework to work.
        scheduler.run()
    }

    /** This function is called once when the robot is disabled.  */
    override fun disabledInit() {}

    /** Function called when the robot is disabled. Similar to enableInit */
    override fun disabledExit() {
        println("Robot disabled")
    }

    /** This function is called periodically when disabled.  */
    override fun disabledPeriodic() {}

    /** This function is called once when the robot is first started up in sim.  */
    override fun simulationInit() {}

    /** This function is called when the driver station connects.  */
    override fun driverStationConnected() {
        println("DS connected yay! ฅ^•ﻌ•^ฅ")
    }

    /** This function is called periodically whilst in simulation.  */
    override fun simulationPeriodic() {}


    private fun getCompBotBoolean(): Boolean {
        var compBot = true
        if (RobotBase.getRuntimeType() == RuntimeType.SYSTEMCORE) {
            val networkInterfaces =  NetworkInterface.getNetworkInterfaces()
            println("retrieving network interfaces")
            for (iFace in networkInterfaces) {
                println(iFace.name)
                if (iFace.name == "eth0") {
                    println("NETWORK NAME--->${iFace.name}<----")
                    var macString = ""
                    for (byteVal in iFace.hardwareAddress){
                        macString += String.format("%s", byteVal)
                    }
                    println("FORMATTED---->$macString<-----")

                    compBot = (macString == "0-128475710531")
                }
            }
        } else { println("Not real so I am compbot") }
        println("I am compbot = $compBot")
        return compBot
    }
}








/**
 * Main initialization function. Do not perform any initialization here
 * other than calling `RobotBase.startRobot`. Do not modify this file
 * except to change the object passed to the `startRobot` call.
 *
 * If you change the package of this file, you must also update the
 * `ROBOT_MAIN_CLASS` variable in the gradle build file. Note that
 * this file has a `@file:JvmName` annotation so that its compiled
 * Java class name is "Main" rather than "MainKt". This is to prevent
 * any issues/confusion if this file is ever replaced with a Java class.
 * See the [Package Level Functions](https://kotlinlang.org/docs/java-to-kotlin-interop.html#package-level-functions)
 * section on the *Calling Kotlin from Java* page of the Kotlin Docs.
 *
 * If you change your main frc.team2471.frc2026.Robot object (name), change the parameter of the
 * `RobotBase.startRobot` call below to the new name. (If you use the IDE's
 * Rename * Refactoring when renaming the object, it will get changed everywhere
 * including here.)
 */
fun main() = RobotBase.startRobot(Robot::class.java)
//fun main() = RobotBase.startRobot {
//    Robot
//}