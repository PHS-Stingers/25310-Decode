package org.firstinspires.ftc.teamcode.pedroPathing;

import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.createFollower;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
//import com.qualcomm.hardware.dfrobot.HuskyLens;

//import org.firstinspires.ftc.robotcore.internal.system.Deadline;

import java.util.concurrent.TimeUnit;

@TeleOp(name = "FreeSpinRed", group = "Testing")
public class FreeSpinRed extends LinearOpMode {

    // ===== FLYWHEEL POWER CONSTANTS =====
    private static final double SHORT_SHOT_SCALE = 0.5;  // Scale for short shot (front area)
    private static final double FULL_SHOT_SCALE = 1.0;   // Full power for back area

    // ===== SHOOTING DISTANCE CONFIGURATION =====
    private static final double MAX_FRONT_SHOOT_DISTANCE = 152.4;  // Maximum distance robot can shoot from front zone (centimeters) - EDITABLE

    // ===== SHOOTING ZONE TARGET HEADING =====
    private static final double TARGET_X = 131.5;  // Red alliance target X
    private static final double TARGET_Y = 134.5;  // Red alliance target Y

    //===== GATE SERVO POSITIONS (normalized 0-1) - SERVO DISABLED =====
    private static final double GATE_CLOSED_POSITION = 0.5;    // 180 degrees
    private static final double GATE_OPEN_POSITION = 140.0 / 180.0;    // 140 degrees (approx 0.778)

    // ===== FLYWHEEL SPEED VALIDATION =====
    private static final double RPM_TOLERANCE = 200;  // RPM tolerance for speed validation
    private static final double ENCODER_TICKS_PER_REV = 28;  // REV HD Hex Motor encoder ticks
    private static final double POSITION_TOLERANCE_INCHES = 3.0;  // Position tolerance

    // ===== HARDWARE DECLARATIONS =====

    private final int READ_PERIOD = 1;

//    private HuskyLens huskyLens;

    private DcMotor intake;
    private DcMotorEx flywheel;

    private Servo gate;  // SERVO DISABLED - NOT USED IN THIS TEST

    private Follower follower;
    private MecanumDrive drive;
    private CoordinateTriangle shootingZones;

    // ===== SHOOTING SYSTEM STATE VARIABLES =====

    private double maxPixel = 50;
    private double intakeScale = 0;
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
        NONE, FRONT, BACK
    }

    @Override
    public void runOpMode() {
        // --- INITIALIZATION PHASE ---
//        huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");
//        Deadline rateLimit = new Deadline(READ_PERIOD, TimeUnit.SECONDS);
//        rateLimit.expire();
//
//        if (!huskyLens.knock()) {
//            telemetry.addData(">>", "Problem communicating with " + huskyLens.getDeviceName());
//        } else {
//            telemetry.addData(">>", "Press start to continue");
//        }
//
//        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);

        // Initialize the MecanumDrive object. This will map and configure all drive motors.
        drive = new MecanumDrive(hardwareMap);

        // Set all drive motors to BRAKE zero power behavior
        drive.frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        drive.backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        drive.frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        drive.backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Initialize PedroPathing follower for autonomous positioning
        follower = createFollower(hardwareMap);


        // Initialize CoordinateTriangle for shooting zone detection
        shootingZones = new CoordinateTriangle();

        // Map the intake motor from the hardware configuration
        intake = hardwareMap.get(DcMotor.class, "intake");

        // Map the flywheel motor (using DcMotorEx for RPM/velocity control)
        flywheel = hardwareMap.get(DcMotorEx.class, "output");

        // // SERVO GATE INITIALIZATION DISABLED - NOT USED IN THIS TEST
        gate = hardwareMap.get(Servo.class, "gate");

        // Set the direction of the intake motor if needed.
        intake.setDirection(DcMotorSimple.Direction.REVERSE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
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
        telemetry.addData("Alliance", "RED");
        telemetry.addData("Target Heading", "(" + TARGET_X + ", " + TARGET_Y + ")");
        telemetry.addData("Front Shoot Area Vertices", "x1=" + shootingZones.x1 + ", y1=" + shootingZones.y1 + ", x2=" + shootingZones.x2 + ", y2=" + shootingZones.y2 + ", x3=" + shootingZones.x3);
        telemetry.addData("Back Shoot Area Vertices", "x6=" + shootingZones.x6 + ", y6=" + shootingZones.y6 + ", x7=" + shootingZones.x7 + ", y7=" + shootingZones.y7 + ", x8=" + shootingZones.x8);
        telemetry.update();

        // --- WAIT FOR START ---
        waitForStart();

        // --- TELEOP LOOP ---
        while (opModeIsActive()) {

//            if (!rateLimit.hasExpired()) {
//                continue;
//            }
//            rateLimit.reset();
//
//            HuskyLens.Block[] blocks = huskyLens.blocks();
//            telemetry.addData("Block count", blocks.length);
//            for (HuskyLens.Block block : blocks) {
//                telemetry.addData("Block", block.toString());

                // --- Drive Train Control ---
                double y = gamepad1.left_stick_y;
                double x = gamepad1.left_stick_x;
                double rx = gamepad1.right_stick_x;

                // Call the drive method from our MecanumDrive class
                drive.drive(y, x, rx);

//                // --- Align Robot based on huskylens ---
//                if (gamepad1.left_bumper) {
//                    if (block.id != 0) {
//                        if (block.x > 215) {
//                            //STRAFE LEFT until  blocks[i].x between 205 and 215
//                            drive.drive(0, -1, 0);
//                        } else if (block.x < 205) {
//                            //STRAFE RIGHT until blocks[i].x between 205 and 215
//                            drive.drive(0, 1, 0);
//                        }
//
//                    } else if (block.id == 2) {
//                        if (block.x > 215) {
//                            //STRAFE LEFT until  blocks[i].x between 205 and 215
//                            drive.drive(0, 1, 0);
//                        } else if (block.x < 205) {
//                            //STRAFE RIGHT until blocks[i].x between 205 and 215
//                            drive.drive(0, -1, 0);
//                        }
//
//                    }
//
//                }

                // --- Update Follower (PedroPathing)
                follower.update();

                // --- Check Shooting Zones ---
                shootingZones.checkIfRobotInFrontShootArea(follower);
                shootingZones.checkIfRobotInBackShootArea(follower);

                // --- Intake Control ---
                if (gamepad1.right_trigger > 0.1) {
                    intake.setPower(1.0);
                } else {
                    intake.setPower(0.0);
                }

                // --- Face Target Heading When in Shooting Zones (only when joysticks are idle) ---
                // Check if both joysticks are not being used (driver is not inputting)
                boolean isLeftStickIdle = Math.abs(gamepad1.left_stick_y) < 0.1 && Math.abs(gamepad1.left_stick_x) < 0.1;
                boolean isRightStickIdle = Math.abs(gamepad1.right_stick_x) < 0.1;

                if ((shootingZones.isInFrontShootArea() || shootingZones.isInBackShootArea()) && isLeftStickIdle && isRightStickIdle) {
                    double robotX = follower.getPose().getX();
                    double robotY = follower.getPose().getY();

                    // Calculate desired heading to face target
                    double deltaX = TARGET_X - robotX;
                    double deltaY = TARGET_Y - robotY;
                    double desiredHeading = Math.atan2(deltaY, deltaX);

                    // Get current heading
                    double currentHeading = follower.getPose().getHeading();

                    // Calculate heading error
                    double headingError = desiredHeading - currentHeading;

                    // Normalize heading error to (-PI, PI]
                    while (headingError > Math.PI) headingError -= 2 * Math.PI;
                    while (headingError <= -Math.PI) headingError += 2 * Math.PI;

                    // Apply proportional control for rotation
                    double rotationPower = Math.max(-1.0, Math.min(1.0, headingError * 0.5));
                    rx = rotationPower;

                    // Update drive with new rotation
                    drive.drive(y, x, rx);
                }

                // --- Flywheel Control Based on Shooting Zones ---
                if (shootingZones.isInFrontShootArea() || shootingZones.isInBackShootArea()) {
                    double robotX = follower.getPose().getX();
                    double robotY = follower.getPose().getY();

                    // Apply appropriate power based on shooting zone
                    if (shootingZones.isInFrontShootArea()) {
                        double frontDistanceScaledPower = calculateFrontShootDistanceScaledPower(robotX, robotY);
                        flywheel.setPower(frontDistanceScaledPower * SHORT_SHOT_SCALE);
                    } else {
                        double scaledPower = calculateDistanceScaledFlywheelPower(robotX, robotY);
                        flywheel.setPower(scaledPower * FULL_SHOT_SCALE);
                    }
                } else {
                    flywheel.setPower(0.0);
                }

                // --- Manual Flywheel Override (if needed for testing) ---
                // Priority 1: D-Pad Down - Run at 100% full power
                if (gamepad1.dpad_down) {
                    flywheel.setPower(1.0);  // Full power (100%)
                }

                // --- D-Pad Up - Update Follower Pose to (129.5, 109.5) at 0 degrees ---
                if (gamepad1.dpad_up) {
                    follower.setStartingPose(new com.pedropathing.geometry.Pose(129.5, 109.5, Math.toRadians(0)));
                }

                // --- Right Bumper - Reverse INTAKE ONLY (not flywheel) ---
                if (gamepad1.right_bumper) {
                    intake.setPower(-1);    // Reverse intake only
                }

                if (gamepad1.a) {
                    gate.setPosition(90);
                } else if (gamepad1.b) {
                    gate.setPosition(0);
                }

                // --- Telemetry ---
                telemetry.addData("Left Stick Y", y);
                telemetry.addData("Left Stick X", x);
                telemetry.addData("Right Stick X", rx);

                telemetry.addData("Gate Position", gate.getPosition());  // GATE DISABLED
                telemetry.addData("Robot X", follower.getPose().getX());
                telemetry.addData("Robot Y", follower.getPose().getY());
                telemetry.addData("Robot Heading (rad)", follower.getPose().getHeading());
                telemetry.addData("In Front Shoot Area", shootingZones.isInFrontShootArea());
                telemetry.addData("In Back Shoot Area", shootingZones.isInBackShootArea());
                telemetry.addData("Flywheel Power", flywheel.getPower());
                telemetry.addData("Intake Power", intake.getPower());
                telemetry.update();
            }
        }


    /**
     * Calculates the distance from the robot to the alliance goal and scales flywheel power accordingly for the front shoot zone.
     * Uses MAX_FRONT_SHOOT_DISTANCE as the maximum distance reference.
     *
     * The scaling is a fraction: current_distance / max_distance
     * This ensures that the closer the robot is, the lower the power, and at maximum distance, full power is applied.
     *
     * For Red alliance:
     * - Target Goal: (131.5, 134.5)
     * - Max Distance Reference: MAX_FRONT_SHOOT_DISTANCE (configurable, currently 60.0 inches)
     *
     * @param robotX The robot's current X coordinate
     * @param robotY The robot's current Y coordinate
     * @return Scaled flywheel power (0.0 to 1.0) based on distance ratio from target
     */
    private double calculateFrontShootDistanceScaledPower(double robotX, double robotY) {
        // Calculate Euclidean distance from robot to target alliance goal
        double deltaX = TARGET_X - robotX;
        double deltaY = TARGET_Y - robotY;
        double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Use configurable maximum front shoot distance
        double maxDistance = MAX_FRONT_SHOOT_DISTANCE;

        // Avoid division by zero
        if (maxDistance == 0) {
            return 1.0;  // Return full power if max distance is zero
        }

        // Calculate distance ratio (current distance / maximum distance)
        double distanceRatio = distanceToTarget / maxDistance;

        // Clamp ratio between 0 and 1 to ensure valid power output
        distanceRatio = Math.max(0.0, Math.min(1.0, distanceRatio));

        return distanceRatio;
    }

    /**
     * Calculates the distance from the robot to the alliance goal and scales flywheel power accordingly.
     * Uses the furthest vertex (x6, y6) of the back shoot area as the maximum distance reference.
     *
     * The scaling is a fraction: current_distance / max_distance
     * This ensures that the closer the robot is, the lower the power, and at maximum distance, full power is applied.
     *
     * For Red alliance:
     * - Target Goal: (131.5, 134.5)
     * - Max Distance Reference Point: (shootingZones.x6, shootingZones.y6)
     *
     * @param robotX The robot's current X coordinate
     * @param robotY The robot's current Y coordinate
     * @return Scaled flywheel power (0.0 to 1.0) based on distance ratio from target
     */
    private double calculateDistanceScaledFlywheelPower(double robotX, double robotY) {
        // Calculate Euclidean distance from robot to target alliance goal
        double deltaX = TARGET_X - robotX;
        double deltaY = TARGET_Y - robotY;
        double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Calculate maximum distance reference using the furthest vertex (x6, y6) for Red
        double maxDeltaX = TARGET_X - shootingZones.x6;
        double maxDeltaY = TARGET_Y - shootingZones.y6;
        double maxDistance = Math.sqrt(maxDeltaX * maxDeltaX + maxDeltaY * maxDeltaY);

        // Avoid division by zero
        if (maxDistance == 0) {
            return 1.0;  // Return full power if max distance is zero
        }

        // Calculate distance ratio (current distance / maximum distance)
        double distanceRatio = distanceToTarget / maxDistance;

        // Clamp ratio between 0 and 1 to ensure valid power output
        distanceRatio = Math.max(0.0, Math.min(1.0, distanceRatio));

        return distanceRatio;
    }

}
