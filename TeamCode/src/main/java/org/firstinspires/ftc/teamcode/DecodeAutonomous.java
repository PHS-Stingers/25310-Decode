package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.pedropathing.geometry.*;
import com.pedropathing.paths.*;

/**
 * Autonomous Mode for FTC Decode Season 2025
 *
 * This autonomous uses Pedro Pathing for precise path following.
 * Customize the paths and actions for your specific autonomous strategy.
 */
@Autonomous(name = "Decode Autonomous", group = "Competition")
public class DecodeAutonomous extends LinearOpMode {

    // Hardware configuration object
    HardwareConfig robot = new HardwareConfig();
    private ElapsedTime runtime = new ElapsedTime();

    // Starting position (adjust based on your starting tile)
    private Pose startPose = new Pose(0, 0, 0);

    @Override
    public void runOpMode() {
        // Initialize hardware with Pedro Pathing enabled
        robot.init(hardwareMap, true);
        robot.setStartingPose(startPose);

        telemetry.addData("Status", "Initialized");
        telemetry.addData("Mode", "Autonomous - 30 seconds");
        telemetry.addData("Starting Pose", "X: %.1f, Y: %.1f, Heading: %.1f",
                startPose.getX(), startPose.getY(), Math.toDegrees(startPose.getHeading()));
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        // Run autonomous routine
        if (opModeIsActive()) {
            // Example autonomous sequence using Pedro Pathing
            // Customize this based on your strategy

            runAutonomousPath();

            // Final stop
            robot.stopAllMotors();
            telemetry.addData("Status", "Autonomous Complete");
            telemetry.addData("Runtime", "%.2f seconds", runtime.seconds());
            telemetry.update();
        }
    }

    /**
     * Main autonomous path routine
     * Customize this method with your competition strategy
     */
    private void runAutonomousPath() {
        // Example: Drive forward to scoring position
        Pose scoringPose = new Pose(24, 0, 0);
        Path pathToScoring = new BezierLine(startPose, scoringPose);

        telemetry.addData("Status", "Following path to scoring position");
        telemetry.update();

        robot.follower.followPath(pathToScoring);
        while (opModeIsActive() && robot.follower.isBusy()) {
            robot.updateFollower();
            updateTelemetry();
        }

        // Example: Run launcher to score
        telemetry.addData("Status", "Scoring");
        telemetry.update();
        robot.runLauncher();
        sleep(2000);
        robot.stopLauncher();

        // Example: Curved path to collect game pieces
        Pose collectPose = new Pose(48, 24, Math.toRadians(90));
        Path pathToCollect = new BezierCurve(
                scoringPose,
                new Pose(36, 12, 0),  // Control point
                collectPose
        );

        telemetry.addData("Status", "Moving to collection zone");
        telemetry.update();

        robot.follower.followPath(pathToCollect);
        while (opModeIsActive() && robot.follower.isBusy()) {
            robot.updateFollower();
            updateTelemetry();
        }

        // Example: Run intake
        telemetry.addData("Status", "Collecting");
        telemetry.update();
        robot.runIntake();
        sleep(2000);
        robot.stopIntake();

        // Example: Return to scoring position
        Path pathReturn = new BezierLine(collectPose, scoringPose);

        telemetry.addData("Status", "Returning to scoring position");
        telemetry.update();

        robot.follower.followPath(pathReturn);
        while (opModeIsActive() && robot.follower.isBusy()) {
            robot.updateFollower();
            updateTelemetry();
        }

        // Score again
        telemetry.addData("Status", "Scoring again");
        telemetry.update();
        robot.runLauncher();
        sleep(2000);
        robot.stopLauncher();

        // Park
        Pose parkPose = new Pose(60, 60, Math.toRadians(45));
        Path pathToPark = new BezierLine(scoringPose, parkPose);

        telemetry.addData("Status", "Parking");
        telemetry.update();

        robot.follower.followPath(pathToPark);
        while (opModeIsActive() && robot.follower.isBusy()) {
            robot.updateFollower();
            updateTelemetry();
        }
    }

    /**
     * Update telemetry with current position and status
     */
    private void updateTelemetry() {
        Pose currentPose = robot.getPose();
        telemetry.addData("X", "%.2f", currentPose.getX());
        telemetry.addData("Y", "%.2f", currentPose.getY());
        telemetry.addData("Heading", "%.2f degrees", Math.toDegrees(currentPose.getHeading()));
        telemetry.addData("Runtime", "%.2f seconds", runtime.seconds());
        telemetry.update();
    }
}
