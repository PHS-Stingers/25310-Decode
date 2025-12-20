package org.firstinspires.ftc.teamcode.pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;

import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.createFollower;
import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.Poses;

@TeleOp(name = "Driver Controlled", group = "Competition")
public class Decode_TeleOp extends LinearOpMode {

    // ===== FLYWHEEL RPM CONSTANTS =====
    private static final double FRONT_SHOOT_RPM = 3000;
    private static final double BACK_SHOOT_RPM = 6000;

    // ===== GATE SERVO POSITIONS (normalized 0-1) =====
    private static final double GATE_CLOSED_POSITION = 1.0;    // 180 degrees
    private static final double GATE_OPEN_POSITION = 140.0 / 180.0;    // 140 degrees (approx 0.778)

    // ===== FLYWHEEL SPEED VALIDATION =====
    private static final double RPM_TOLERANCE = 200;  // RPM tolerance for speed validation
    private static final double ENCODER_TICKS_PER_REV = 28;  // REV HD Hex Motor encoder ticks
    private static final double POSITION_TOLERANCE_INCHES = 3.0;  // Position tolerance

    // ===== HARDWARE DECLARATIONS =====
    private DcMotor intake;
    private DcMotorEx flywheel;
    private Servo gate;
    private Follower follower;
    private MecanumDrive drive;

    // ===== SHOOTING SYSTEM STATE VARIABLES =====
    private double targetFlywheelRPM = 0;
    private boolean isMovingToShootPosition = false;
    private ShootPosition currentShootPosition = ShootPosition.NONE;
    private boolean isTestShootingMode = false;

    // State tracking for D-Pad button releases
    private boolean previousDpadUp = false;
    private boolean previousDpadDown = false;

    // ===== ENUM FOR SHOOT POSITIONS =====
    private enum ShootPosition {
        NONE, FRONT, BACK, TEST
    }

    @Override
    public void runOpMode() {
        // --- INITIALIZATION PHASE ---

        // Initialize the MecanumDrive object. This will map and configure all drive motors.
        drive = new MecanumDrive(hardwareMap);

        // Initialize PedroPathing follower for autonomous positioning
        follower = createFollower(hardwareMap);

        // Map the intake motor from the hardware configuration
        intake = hardwareMap.get(DcMotor.class, "intake");

        // Map the flywheel motor (using DcMotorEx for RPM/velocity control)
        flywheel = hardwareMap.get(DcMotorEx.class, "Output");

        // Map the gate servo
        gate = hardwareMap.get(Servo.class, "Gate");

        // Set the direction of the intake motor if needed.
        intake.setDirection(DcMotorSimple.Direction.REVERSE);

        // Configure flywheel motor for velocity-based control
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Initialize gate to closed position
        gate.setPosition(GATE_CLOSED_POSITION);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // --- WAIT FOR START ---
        waitForStart();

        // --- TELEOP LOOP ---
        while (opModeIsActive()) {
            // --- Drive Train Control ---
            double y = gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;

            // Call the drive method from our MecanumDrive class
            drive.drive(y, x, rx);

            // --- Shooting Position Control ---
            handleShootingPositionInput();

            // --- Test Shooting Mode (Left Trigger) ---
            handleTestShootingMode();

            // --- Update Follower (PedroPathing)
            follower.update();
            updateRobotPosition();

            // --- Intake Control ---
            if (gamepad1.right_trigger > 0.1) {
                intake.setPower(1.0);
            } else {
                intake.setPower(0.0);
            }

            // --- Flywheel and Gate Control ---
            handleFlywheelAndGate();

            // --- Telemetry ---
            telemetry.addData("Status", "TeleOp Running");
            telemetry.addData("Left Stick Y", y);
            telemetry.addData("Left Stick X", x);
            telemetry.addData("Right Stick X", rx);
            telemetry.addData("Intake Power", intake.getPower());
            telemetry.addData("Current Position", currentShootPosition);
            telemetry.addData("Test Shooting Mode", isTestShootingMode);
            telemetry.addData("Target Flywheel RPM", targetFlywheelRPM);
            telemetry.addData("Current Flywheel RPM", getFlywheelRPM());
            telemetry.addData("Gate Position", gate.getPosition());
            telemetry.addData("Robot X", follower.getPose().getX());
            telemetry.addData("Robot Y", follower.getPose().getY());
            telemetry.update();
        }
    }

    // ===== SHOOTING POSITION HANDLING =====
    private void handleShootingPositionInput() {
        // Move to front shoot position when D-Pad Up is pressed
        if (gamepad1.dpad_up) {
            moveToFrontShootPosition();
        } else if (previousDpadUp && !gamepad1.dpad_up) {
            // D-Pad Up was released - reset shooting state to allow gate to close
            resetShootingState();
        }

        // Move to back shoot position when D-Pad Down is pressed
        if (gamepad1.dpad_down) {
            moveToBackShootPosition();
        } else if (previousDpadDown && !gamepad1.dpad_down) {
            // D-Pad Down was released - reset shooting state to allow gate to close
            resetShootingState();
        }

        // Update previous D-Pad state for next iteration
        previousDpadUp = gamepad1.dpad_up;
        previousDpadDown = gamepad1.dpad_down;
    }

    private void resetShootingState() {
        // Reset all shooting position variables to close gate and stop flywheel
        isMovingToShootPosition = false;
        currentShootPosition = ShootPosition.NONE;
        targetFlywheelRPM = 0;
    }

    private void moveToFrontShootPosition() {
        if (!isMovingToShootPosition) {
            isMovingToShootPosition = true;
            currentShootPosition = ShootPosition.FRONT;
            targetFlywheelRPM = FRONT_SHOOT_RPM;

            // Build a path from current position to front score position
            Pose currentPose = follower.getPose();
            Pose frontPose = Poses.frontScorePose;

            follower.followPath(follower.pathBuilder()
                .addPath(new com.pedropathing.geometry.BezierLine(currentPose, frontPose))
                .setLinearHeadingInterpolation(currentPose.getHeading(), frontPose.getHeading())
                .build());
        }
    }

    private void moveToBackShootPosition() {
        if (!isMovingToShootPosition) {
            isMovingToShootPosition = true;
            currentShootPosition = ShootPosition.BACK;
            targetFlywheelRPM = BACK_SHOOT_RPM;

            // Build a path from current position to back score position
            Pose currentPose = follower.getPose();
            Pose backPose = Poses.backScorePose;

            follower.followPath(follower.pathBuilder()
                .addPath(new com.pedropathing.geometry.BezierLine(currentPose, backPose))
                .setLinearHeadingInterpolation(currentPose.getHeading(), backPose.getHeading())
                .build());
        }
    }

    // ===== UPDATE ROBOT POSITION AND VALIDATE ARRIVAL =====
    private void updateRobotPosition() {
        // Check if the robot has arrived at the target position
        if (isMovingToShootPosition && !follower.isBusy()) {
            Pose currentPose = follower.getPose();
            double distanceToTarget = 0;

            if (currentShootPosition == ShootPosition.FRONT) {
                distanceToTarget = calculateDistance(currentPose, Poses.frontScorePose);
            } else if (currentShootPosition == ShootPosition.BACK) {
                distanceToTarget = calculateDistance(currentPose, Poses.backScorePose);
            }

            // If within tolerance, consider arrived
            if (distanceToTarget < POSITION_TOLERANCE_INCHES) {
                isMovingToShootPosition = false;
            }
        }
    }

    private double calculateDistance(Pose pose1, Pose pose2) {
        double dx = pose1.getX() - pose2.getX();
        double dy = pose1.getY() - pose2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // ===== FLYWHEEL AND GATE CONTROL =====
    private void handleFlywheelAndGate() {
        // Set flywheel speed based on target RPM
        if (targetFlywheelRPM > 0) {
            // Convert RPM to ticks per second for velocity control
            double ticksPerSecond = (targetFlywheelRPM * ENCODER_TICKS_PER_REV) / 60.0;
            flywheel.setVelocity(ticksPerSecond);
        } else {
            flywheel.setPower(0);
        }

        // Validate conditions for opening gate
        boolean flywheelAtSpeed = isFlywheelAtTargetSpeed();

        // Two conditions for gate opening:
        // 1. Normal mode: Robot must be at a position and stopped
        boolean normalModeReady = !isMovingToShootPosition &&
                                  (currentShootPosition == ShootPosition.FRONT ||
                                   currentShootPosition == ShootPosition.BACK);

        // 2. Test mode: Just need flywheel at speed (no position requirement)
        boolean testModeReady = isTestShootingMode;

        // Open gate only if conditions are met
        if (flywheelAtSpeed && (normalModeReady || testModeReady) && targetFlywheelRPM > 0) {
            gate.setPosition(GATE_OPEN_POSITION);
        } else {
            gate.setPosition(GATE_CLOSED_POSITION);
        }
    }

    // ===== FLYWHEEL SPEED VALIDATION =====
    private boolean isFlywheelAtTargetSpeed() {
        if (targetFlywheelRPM <= 0) {
            return false;
        }

        double currentRPM = getFlywheelRPM();
        double difference = Math.abs(currentRPM - targetFlywheelRPM);

        return difference <= RPM_TOLERANCE;
    }

    private double getFlywheelRPM() {
        // Convert velocity (ticks per second) to RPM
        double ticksPerSecond = flywheel.getVelocity();
        return (ticksPerSecond * 60.0) / ENCODER_TICKS_PER_REV;
    }

    // ===== TEST SHOOTING MODE (LEFT TRIGGER) =====
    private void handleTestShootingMode() {
        // Check if left trigger is pressed (> 0.1 threshold)
        if (gamepad1.left_trigger > 0.1) {
            // Enable test shooting mode
            if (!isTestShootingMode) {
                isTestShootingMode = true;
                currentShootPosition = ShootPosition.TEST;
                targetFlywheelRPM = BACK_SHOOT_RPM;  // Default to back shoot RPM for testing
                isMovingToShootPosition = false;  // No movement required
            }
        } else {
            // Disable test shooting mode when trigger is released
            if (isTestShootingMode) {
                isTestShootingMode = false;
                currentShootPosition = ShootPosition.NONE;
                targetFlywheelRPM = 0;  // Stop flywheel
            }
        }
    }
}