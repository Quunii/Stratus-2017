package org.usfirst.frc.team1389.autonomous;

import java.util.function.Function;

import org.usfirst.frc.team1389.robot.RobotSoftware;
import org.usfirst.frc.team1389.systems.GearIntakeSystem;
import org.usfirst.frc.team1389.systems.GearIntakeSystem.State;

import com.team1389.auto.AutoModeBase;
import com.team1389.auto.AutoModeEndedException;
import com.team1389.auto.command.WaitTimeCommand;
import com.team1389.command_framework.CommandUtil;
import com.team1389.command_framework.command_base.Command;
import com.team1389.control.SynchronousPIDController;
import com.team1389.hardware.value_types.Percent;
import com.team1389.hardware.value_types.Position;
import com.team1389.system.SystemManager;
import com.team1389.system.drive.DriveOut;
import com.team1389.util.list.AddList;
import com.team1389.watch.Watchable;

public class DriveStraightDropOffGear extends AutoModeBase {
	RobotSoftware robot;
	SynchronousPIDController<Percent, Position> leftPID;
	SynchronousPIDController<Percent, Position> rightPID;
	GearIntakeSystem gearSystem;
	SystemManager manager;

	DriveStraightDropOffGear(RobotSoftware robot) {
		this.robot = robot;
		DriveOut<Percent> tankDrive = robot.voltageDrive.getAsTank();
		leftPID = new SynchronousPIDController<Percent, Position>(0.25, 0.25, 0.25, 0.25,
				robot.frontLeft.getPositionInput(), tankDrive.left());
		rightPID = new SynchronousPIDController<Percent, Position>(0.25, 0.25, 0.25, 0.25,
				robot.frontRight.getPositionInput(), tankDrive.right());
		gearSystem = new GearIntakeSystem(robot.armAngle, robot.armVel, robot.armElevator.getVoltageOutput(),
				robot.gearIntake.getVoltageOutput(), robot.gearIntakeCurrent);
		manager = new SystemManager(gearSystem);
	}

	@Override
	public AddList<Watchable> getSubWatchables(AddList<Watchable> stem) {
		return null;
	}

	@Override
	public String getName() {
		return "Drive straight and drop off the gear";
	}

	@Override
	protected void routine() throws AutoModeEndedException {
	//	Command driving = new DriveStraightCommand(leftPID, rightPID, 10, 10, 10, 10);
		Function<State, Command> gearState = s -> gearSystem
				.pairWithBackgroundCommand(gearSystem.getEnterStateCommand(s));
	//	Function<Double, Command> distanceChecker = d -> new WaitForBooleanCommand(() -> (robot.frontLeft.getPositionInput().get() > d));
		Function<Double, Command> distanceChecker = WaitTimeCommand::new;
		Command gearPlacing = CommandUtil.combineSequential(gearState.apply(State.CARRYING), distanceChecker.apply(5.0),
				gearState.apply(State.ALIGNING), distanceChecker.apply(2.0), gearState.apply(State.PLACING));
		runCommand(CommandUtil.combineSimultaneous(gearPlacing));
	}

}
