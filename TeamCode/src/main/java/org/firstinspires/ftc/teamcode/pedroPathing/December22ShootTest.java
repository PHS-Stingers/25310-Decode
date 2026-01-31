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

@TeleOp(name = "December22 Shoot Test", group = "Testing")
public class December22ShootTest extends LinearOpMode {

    // ===== FLYWHEEL RPM CONSTANTS =====
    private static final double FRONT_SHOOT_RPM = 3000;
    private static final double BACK_SHOOT_RPM = 4500;

     //===== GATE SERVO POSITIONS (normalized 0-1) - SERVO DISABLED =====
     private static final double GATE_CLOSED_POSITION = 0.5;    // 180 degrees
     private static final double GATE_OPEN_POSITION = 140.0 / 180.0;    // 140 degrees (approx 0.778)

    // ===== FLYWHEEL SPEED VALIDATION =====
    private static final double RPM_TOLERANCE = 200;  // RPM tolerance for speed validation
    private static final double ENCODER_TICKS_PER_REV = 28;  // REV HD Hex Motor encoder ticks
    private static final double POSITION_TOLERANCE_INCHES = 3.0;  // Position tolerance

    // ===== HARDWARE DECLARATIONS =====
    private DcMotor intake;
    private DcMotorEx flywheel;

    private Servo gate;  // SERVO DISABLED - NOT USED IN THIS TEST

    private Follower follower;
    private MecanumDrive drive;

    // ===== SHOOTING SYSTEM STATE VARIABLES =====
    private double targetFlywheelRPM = 0;
    private double flywheelMaxRPM = 0;
    private boolean isMaxMode = false;
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
        flywheel = hardwareMap.get(DcMotorEx.class, "output");

        // // SERVO GATE INITIALIZATION DISABLED - NOT USED IN THIS TEST
        gate = hardwareMap.get(Servo.class, "gate");

        // Set the direction of the intake motor if needed.
        intake.setDirection(DcMotorSimple.Direction.REVERSE);
        flywheel.setDirection(DcMotorEx.Direction.REVERSE);
        // Configure flywheel motor for velocity-based control
        flywheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Discover motor max RPM for "full capacity" mode
        flywheelMaxRPM = flywheel.getMotorType().getMaxRPM();

        // // SERVO GATE CLOSED POSITION INITIALIZATION DISABLED - NOT USED IN THIS TEST
        gate.setPosition(GATE_CLOSED_POSITION);

        telemetry.addData("Status", "Initialized");
        telemetry.addData("Note", "Servo gate disabled for testing");
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
            // POSITION NAVIGATION COMMENTED OUT - NOT NEEDED FOR THIS TEST
            // handleShootingPositionInput();

            // --- Test Shooting Mode (Left Trigger) - NO POSITION OR SERVO REQUIREMENTS ---
            handleTestShootingMode();

            // --- Max RPM Mode: press and hold Left Bumper to run at motor max RPM ---
            if (gamepad1.left_bumper) {
                isMaxMode = true;
                targetFlywheelRPM = flywheelMaxRPM;
                currentShootPosition = ShootPosition.TEST;
            } else {
                if (isMaxMode) {
                    isMaxMode = false;
                    if (!isTestShootingMode) {
                        targetFlywheelRPM = 0;
                        currentShootPosition = ShootPosition.NONE;
                    } else {
                        targetFlywheelRPM = BACK_SHOOT_RPM;
                    }
                }
            }

            // --- Update Follower (PedroPathing)
            follower.update();
            // updateRobotPosition();  // COMMENTED OUT - POSITION TRACKING NOT NEEDED

            // --- Intake Control ---
            if (gamepad1.right_trigger > 0.1) {
                intake.setPower(1.0);
            } else {
                intake.setPower(0.0);
            }

            // --- Flywheel Control Priority System ---
            // Priority 1: D-Pad Down - Run at 100% full power
            if (gamepad1.dpad_down) {
                flywheel.setPower(1.0);  // Full power (100%)
            }
            // Priority 2: Right Bumper - Reverse at 0.5 power
            else if (gamepad1.right_bumper) {
                flywheel.setPower(-0.75);  // Reverse at 1 power
                intake.setPower(-1);    // reverse at 1 power
            }
            // Priority 3: Left Bumper, Left Trigger, or normal RPM control
            else {
                // --- Flywheel Control (No Gate) ---
                handleFlywheelOnly();
            }
            if (gamepad1.dpad_left) {
                gate.setPosition(90);
            }
               else if (gamepad1.dpad_right) {
                gate.setPosition(0);
            }

            // --- Telemetry ---
            double currentRPM = getFlywheelRPM();
            double percentOfMax = (flywheelMaxRPM > 0) ? (currentRPM / flywheelMaxRPM) * 100.0 : 0.0;
            boolean atFull = flywheelMaxRPM > 0 && currentRPM >= (flywheelMaxRPM * 0.99);

            telemetry.addData("Status", "TeleOp Running - Shoot Test Mode");
            telemetry.addData("Left Trigger Value", gamepad1.left_trigger);
            telemetry.addData("Left Stick Y", y);
            telemetry.addData("Left Stick X", x);
            telemetry.addData("Right Stick X", rx);
            telemetry.addData("Intake Power", intake.getPower());
            telemetry.addData("Current Position", currentShootPosition);
            telemetry.addData("Test Shooting Mode", isTestShootingMode);
            telemetry.addData("Max Flywheel RPM", "%.1f", flywheelMaxRPM);
            telemetry.addData("Target Flywheel RPM", targetFlywheelRPM);
            telemetry.addData("Current Flywheel RPM", "%.1f", currentRPM);
            telemetry.addData("Percent of Max", "%.1f%%", percentOfMax);
            telemetry.addData("At 100% Capacity?", atFull);

            // When D-Pad Down is pressed, show detailed flywheel capacity info
            if (gamepad1.dpad_down) {
                double flywheelPower = Math.abs(flywheel.getPower());
                double flywheelCapacityPercent = flywheelPower * 100.0;
                boolean flywheelAt100 = flywheelPower >= 0.99;

                telemetry.addData("--- D-PAD DOWN ACTIVE ---", "");
                telemetry.addData("Flywheel Motor Capacity", "%.1f%%", flywheelCapacityPercent);
                telemetry.addData("Flywheel at 100%?", flywheelAt100);
            }

            telemetry.addData("Flywheel Velocity (ticks/sec)", flywheel.getVelocity());
            telemetry.addData("Gate Position", gate.getPosition());  // GATE DISABLED
            telemetry.addData("Robot X", follower.getPose().getX());
            telemetry.addData("Robot Y", follower.getPose().getY());
            telemetry.update();
        }
    }

    // ===== SHOOTING POSITION HANDLING (COMMENTED OUT - NOT USED) =====
    // private void handleShootingPositionInput() {
    //     // Move to front shoot position when D-Pad Up is pressed
    //     if (gamepad1.dpad_up) {
    //         moveToFrontShootPosition();
    //     } else if (previousDpadUp && !gamepad1.dpad_up) {
    //         // D-Pad Up was released - reset shooting state to allow gate to close
    //         resetShootingState();
    //     }
    //
    //     // Move to back shoot position when D-Pad Down is pressed
    //     if (gamepad1.dpad_down) {
    //         moveToBackShootPosition();
    //     } else if (previousDpadDown && !gamepad1.dpad_down) {
    //         // D-Pad Down was released - reset shooting state to allow gate to close
    //         resetShootingState();
    //     }
    //
    //     // Update previous D-Pad state for next iteration
    //     previousDpadUp = gamepad1.dpad_up;
    //     previousDpadDown = gamepad1.dpad_down;
    // }

    // private void resetShootingState() {
    //     // Reset all shooting position variables to close gate and stop flywheel
    //     isMovingToShootPosition = false;
    //     currentShootPosition = ShootPosition.NONE;
    //     targetFlywheelRPM = 0;
    // }

    // private void moveToFrontShootPosition() {
    //     if (!isMovingToShootPosition) {
    //         isMovingToShootPosition = true;
    //         currentShootPosition = ShootPosition.FRONT;
    //         targetFlywheelRPM = FRONT_SHOOT_RPM;
    //
    //         // Build a path from current position to front score position
    //         Pose currentPose = follower.getPose();
    //         Pose frontPose = Poses.frontScorePose;
    //
    //         follower.followPath(follower.pathBuilder()
    //             .addPath(new com.pedropathing.geometry.BezierLine(currentPose, frontPose))
    //             .setLinearHeadingInterpolation(currentPose.getHeading(), frontPose.getHeading())
    //             .build());
    //     }
    // }

    // private void moveToBackShootPosition() {
    //     if (!isMovingToShootPosition) {
    //         isMovingToShootPosition = true;
    //         currentShootPosition = ShootPosition.BACK;
    //         targetFlywheelRPM = BACK_SHOOT_RPM;
    //
    //         // Build a path from current position to back score position
    //         Pose currentPose = follower.getPose();
    //         Pose backPose = Poses.backScorePose;
    //
    //         follower.followPath(follower.pathBuilder()
    //             .addPath(new com.pedropathing.geometry.BezierLine(currentPose, backPose))
    //             .setLinearHeadingInterpolation(currentPose.getHeading(), backPose.getHeading())
    //             .build());
    //     }
    // }

    // ===== UPDATE ROBOT POSITION AND VALIDATE ARRIVAL (COMMENTED OUT - NOT NEEDED) =====
    // private void updateRobotPosition() {
    //     // Check if the robot has arrived at the target position
    //     if (isMovingToShootPosition && !follower.isBusy()) {
    //         Pose currentPose = follower.getPose();
    //         double distanceToTarget = 0;
    //
    //         if (currentShootPosition == ShootPosition.FRONT) {
    //             distanceToTarget = calculateDistance(currentPose, Poses.frontScorePose);
    //         } else if (currentShootPosition == ShootPosition.BACK) {
    //             distanceToTarget = calculateDistance(currentPose, Poses.backScorePose);
    //         }
    //
    //         // If within tolerance, consider arrived
    //         if (distanceToTarget < POSITION_TOLERANCE_INCHES) {
    //             isMovingToShootPosition = false;
    //         }
    //     }
    // }

    // private double calculateDistance(Pose pose1, Pose pose2) {
    //     double dx = pose1.getX() - pose2.getX();
    //     double dy = pose1.getY() - pose2.getY();
    //     return Math.sqrt(dx * dx + dy * dy);
    // }

    // ===== FLYWHEEL ONLY CONTROL (NO GATE) =====
    private void handleFlywheelOnly() {
        // Set flywheel speed based on target RPM
        if (targetFlywheelRPM > 0) {
            // Convert RPM to ticks per second for velocity control
            double ticksPerSecond = (targetFlywheelRPM * ENCODER_TICKS_PER_REV) / 60.0;
            flywheel.setVelocity(ticksPerSecond);
        } else {
            flywheel.setPower(0);
        }

        // // GATE CONTROL COMPLETELY REMOVED - SERVO NOT USED IN THIS TEST
        // // All gate-related validation has been removed
        // // Flywheel will spin whenever targetFlywheelRPM > 0
        // if (flywheelAtSpeed && (normalModeReady || testModeReady) && targetFlywheelRPM > 0) {
        //     gate.setPosition(GATE_OPEN_POSITION);
        // } else {
        //     gate.setPosition(GATE_CLOSED_POSITION);
        // }
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

    // ===== TEST SHOOTING MODE (LEFT TRIGGER) - NO POSITION OR SERVO REQUIREMENTS =====
    private void handleTestShootingMode() {
        // Check if left trigger is pressed (> 0.1 threshold)
        if (gamepad1.left_trigger > 0.1) {
            // Enable test shooting mode
            if (!isTestShootingMode) {
                isTestShootingMode = true;
                currentShootPosition = ShootPosition.TEST;
                targetFlywheelRPM = BACK_SHOOT_RPM;  // Default to back shoot RPM for testing
                isMovingToShootPosition = false;  // No movement needed
            }
        } else {
            // Disable test shooting mode when trigger is released
            if (isTestShootingMode) {
                isTestShootingMode = false;
                currentShootPosition = ShootPosition.NONE;
                targetFlywheelRPM = 0;  // Stop flywheel
                // Gate closing is NOT applicable in this test - servo is disabled
            }
        }
    }
}

