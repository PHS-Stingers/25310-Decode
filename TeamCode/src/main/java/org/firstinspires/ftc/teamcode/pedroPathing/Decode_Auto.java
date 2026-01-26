package org.firstinspires.ftc.teamcode.pedroPathing;



import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.Poses.startPose;
//import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.MecanumConstants;

import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.Paths;
import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.createFollower;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.DcMotorEx; // Added to access getVelocity()


@Autonomous(name = "Decode Auto", group = "Competition")
public class Decode_Auto extends LinearOpMode {


    private DcMotor intake;
    private DcMotorEx flyWheel; // use DcMotorEx so getVelocity() is available
    private Servo feeder;
    private Follower follower;
    private Timer pathTimer, actionTimer, opmodeTimer;
    private int pathState;


    public void shoot(double targetRPM) {
        // Set flywheel to run using encoder
        flyWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Calculate target velocity (ticks per second)
        double ticksPerRevolution = flyWheel.getMotorType().getTicksPerRev();
        double targetVelocity = (targetRPM / 60.0) * ticksPerRevolution;

        // Spin up flywheel
        flyWheel.setPower(1.0);

        // Wait until flywheel reaches target speed (within 5% tolerance)
        while (opModeIsActive() && Math.abs(flyWheel.getVelocity() - targetVelocity) > targetVelocity * 0.05) {
            telemetry.addData("Current RPM", (flyWheel.getVelocity() / ticksPerRevolution) * 60);
            telemetry.addData("Target RPM", targetRPM);
            telemetry.update();
        }


        // In Decode_Auto.java
    }
    @Override
    public void runOpMode () {
        // Initialize the follower and hardware
        follower = createFollower(hardwareMap);
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        flyWheel = hardwareMap.get(DcMotorEx.class, "Output"); // Uncomment when ready

        // Build all paths from the Paths class
        Paths paths = new Paths(follower);
        flyWheel = hardwareMap.get(DcMotorEx.class, "flyWheel"); // use DcMotorEx.class
        feeder = hardwareMap.get(Servo.class, "feeder");

        telemetry.addData("Status", "Initialization Complete");
        telemetry.update();

        // Wait for the start button to be pressed
        waitForStart();

        if (opModeIsActive()) {
            // Path 1: Moves from starting position (56, 10) to the back scoring position (70, 20).
            telemetry.addData("Status", "Following Path 1 to Back Score Position");
            telemetry.update();
            follower.followPath(paths.Path1);

            // Action: Score pre-loaded artifacts on the goal.
            // (Shooter/placing code to be added here)

            // Path 2: Moves from the back scoring position (70, 20) to the far-side pickup area (40, 85).
            telemetry.addData("Status", "Following Path 2 to Far Pickup Area");
            telemetry.update();
            follower.followPath(paths.Path2);

            // Action: Start intake to pick up artifacts.
            telemetry.addData("Status", "Running Intake");
            telemetry.update();
            intake.setPower(1.0);

            // Path Pickup 1: Strafes left (to 25, 85) to secure the artifacts.
            telemetry.addData("Status", "Following Pickup Path 1");
            telemetry.update();
            follower.followPath(paths.PathPickup1);
            intake.setPower(0.0); // Stop intake after pickup.

            // Path 3: Moves from the pickup spot (25, 85) to the front scoring position (59, 92).
            telemetry.addData("Status", "Following Path 3 to Front Score Position");
            telemetry.update();
            follower.followPath(paths.Path3);

            // Action: Score the picked-up artifacts on the goal.
            // (Shooter/placing code to be added here)

            // Path 4: Moves from the front scoring position (59, 92) to the middle pickup area (40, 60).
            telemetry.addData("Status", "Following Path 4 to Middle Pickup Area");
            telemetry.update();
            follower.followPath(paths.Path4);

            // Action: Start intake to pick up artifacts.
            telemetry.addData("Status", "Running Intake");
            telemetry.update();
            intake.setPower(1.0);

            // Path Pickup 2: Strafes left (to 25, 60) to secure the artifacts.
            telemetry.addData("Status", "Following Pickup Path 2");
            telemetry.update();
            follower.followPath(paths.PathPickup2);
            intake.setPower(0.0); // Stop intake after pickup.

            // Path 5: Moves from the pickup spot (25, 60) back to the front scoring position (59, 92).
            telemetry.addData("Status", "Following Path 5 to Front Score Position");
            telemetry.update();
            follower.followPath(paths.Path5);

            // Action: Score the picked-up artifacts on the goal.
            // (Shooter/placing code to be added here)

            // Path 6: Moves from the front scoring position (59, 92) to the near-side pickup area (40, 35).
            telemetry.addData("Status", "Following Path 6 to Near Pickup Area");
            telemetry.update();
            follower.followPath(paths.Path6);

            // Action: Start intake to pick up artifacts.
            telemetry.addData("Status", "Running Intake");
            telemetry.update();
            intake.setPower(1.0);

            // Path Pickup 3: Strafes left (to 25, 35) to secure the artifacts.
            telemetry.addData("Status", "Following Pickup Path 3");
            telemetry.update();
            follower.followPath(paths.PathPickup3);
            intake.setPower(0.0); // Stop intake after pickup.

            // Path 7: Moves from the pickup spot (25, 35) back to the front scoring position (59, 92).
            telemetry.addData("Status", "Following Path 7 to Front Score Position");
            telemetry.update();
            follower.followPath(paths.Path7);

            // Action: Score the picked-up artifacts on the goal.
            // (Shooter/placing code to be added here)

            // Path 8: Moves from the front scoring position (59, 92) to the parking area (20, 70).
            telemetry.addData("Status", "Following Path 8 to Park");
            telemetry.update();
            follower.followPath(paths.Path8);

            telemetry.addData("Status", "Autonomous Finished");
            telemetry.update();
        }
    }


}

