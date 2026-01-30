package org.firstinspires.ftc.teamcode.pedroPathing;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * OTOS-based Autonomous OpMode for DECODE (2025-2026)
 * Uses SparkFun OTOS sensor for localization and drives to preset positions
 *
 * @author Team 20077 The Indubitables
 * @version 1.0, 2026
 */
@Autonomous(name = "OTOS Auto", group = "Competition")
public class Blue_Otos_Auto extends LinearOpMode {

    // ===================== HARDWARE OBJECTS =====================
    private SparkFunOTOS otos;
    private DcMotorEx frontLeftMotor, frontRightMotor, backLeftMotor, backRightMotor;
    private DcMotorEx flywheel;
    private DcMotor intake;
    private Servo gate;

    // ===================== POSITION VARIABLES (EDITABLE) =====================
    // Position 1: Starting position for shooting
    private double POS1_X = 50.0;
    private double POS1_Y = 94.0;

    // Position 2: First pickup location
    private double POS2_X = 50.0;
    private double POS2_Y = 84.0;

    // Position 3: Intermediate position between 2 and 1
    private double POS3_X = 22.0;
    private double POS3_Y = 84.0;

    // Position 4: Second pickup location
    private double POS4_X = 50.0;
    private double POS4_Y = 60.0;

    // Position 5: Intermediate position between 4 and 1
    private double POS5_X = 22.0;
    private double POS5_Y = 60.0;

    // Position 6: Reserved for future use
    private double POS6_X = 0.0;
    private double POS6_Y = 0.0;

    // ===================== MOTOR/MECHANISM VARIABLES =====================
    private double FLYWHEEL_SPEED = 0.85; // Adjustable flywheel speed (0.0 - 1.0)
    private double DRIVE_SPEED = 1.0; // Full drive speed
    private double SLOW_DRIVE_SPEED = 0.5; // 50% drive speed
    private int INTAKE_DURATION_MS = 5000; // 5 seconds for shooting 3 artifacts
    private double INTAKE_SPEED = 1.0;

    // ===================== SERVO POSITIONS =====================
    private double GATE_OPEN = 0.9; // 90 degrees
    private double GATE_CLOSED = 0.0; // 0 degrees

    // ===================== OTOS CONSTANTS =====================
    private double OTOS_TOLERANCE = 2.0; // inches
    private static final String OTOS_NAME = "otos";

    // Simple proportional gain for heading correction when a target heading is provided
    private static final double HEADING_KP = 0.02;

    // ===================== STATE VARIABLES =====================
    private boolean flywheelAtSpeed = false;
    private ElapsedTime intakeTimer = new ElapsedTime();
    private ElapsedTime flywheelTimer = new ElapsedTime();

    @Override
    public void runOpMode() {
        initializeHardware();
        initializeOTOS();

        telemetry.addData("Status", "Ready to Start");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            executeAutonomous();
        }

        stopAllMotors();
    }

    /**
     * Initialize all hardware devices
     */
    private void initializeHardware() {
        // Drive motors
        frontLeftMotor = hardwareMap.get(DcMotorEx.class, "leftFront");
        frontRightMotor = hardwareMap.get(DcMotorEx.class, "rightFront");
        backLeftMotor = hardwareMap.get(DcMotorEx.class, "leftRear");
        backRightMotor = hardwareMap.get(DcMotorEx.class, "rightRear");

        // Set motor directions
        frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        backRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        // Set zero power behavior
        setZeroPowerBehavior();

        // Mechanism motors and servo
        flywheel = hardwareMap.get(DcMotorEx.class, "output");
        intake = hardwareMap.get(DcMotor.class, "intake");
        gate = hardwareMap.get(Servo.class, "gate");

        // Initialize servo to closed position
        gate.setPosition(GATE_CLOSED);
    }

    /**
     * Initialize the OTOS sensor
     */
    private void initializeOTOS() {
        otos = hardwareMap.get(SparkFunOTOS.class, OTOS_NAME);

        if (otos != null) {
            otos.setLinearUnit(DistanceUnit.INCH);
            otos.setAngularUnit(AngleUnit.DEGREES);

            // Set OTOS offset (adjust based on sensor mounting position)
            otos.setOffset(new SparkFunOTOS.Pose2D(0, -7.25, 90));

            // Calibrate the IMU
            otos.calibrateImu();

            // Set starting position

            // Start heading set to 90 as requested
            otos.setPosition(new SparkFunOTOS.Pose2D(15, 111, 90));
            telemetry.addData("OTOS", "Initialized Successfully (start heading 90)");
        } else {
            telemetry.addData("OTOS", "Failed to Initialize");
        }
        telemetry.update();
    }

    /**
     * Main autonomous routine
     */
    private void executeAutonomous() {
        // Cycle 1: Start -> Shoot (shoot heading = 142)
        moveToPosition(POS1_X, POS1_Y, DRIVE_SPEED, 142);
        shootSequence();

        // Cycle 2: Shoot -> Intake 1 -> Shoot
        // Approach pickup (heading 180), intake pose (heading 180), return to shoot (heading 142)
        moveToPosition(POS2_X, POS2_Y, DRIVE_SPEED, 180);
        moveToPosition(POS3_X, POS3_Y, SLOW_DRIVE_SPEED, true, 180);
        moveToPosition(POS1_X, POS1_Y, DRIVE_SPEED, 142);
        shootSequence();

        // Cycle 3: Shoot -> Intake 2 -> Shoot
        moveToPosition(POS4_X, POS4_Y, DRIVE_SPEED, 180);
        moveToPosition(POS5_X, POS5_Y, SLOW_DRIVE_SPEED, true, 180);
        moveToPosition(POS1_X, POS1_Y, DRIVE_SPEED, 142);
        shootSequence();

        telemetry.addData("Status", "Autonomous Complete");
        telemetry.update();
    }

    /**
     * Move robot to a specific position using OTOS sensor feedback.
     * There are overloads that accept an optional target heading. If a heading is
     * provided, the robot will apply a small P-control to correct heading while
     * translating toward the target.
     *
     * @param targetX Target X position in inches
     * @param targetY Target Y position in inches
     * @param driveSpeed Power level for motors (0.0 - 1.0)
     */
    private void moveToPosition(double targetX, double targetY, double driveSpeed) {
        moveToPosition(targetX, targetY, driveSpeed, false, Double.NaN);
    }

    private void moveToPosition(double targetX, double targetY, double driveSpeed, double targetHeading) {
        moveToPosition(targetX, targetY, driveSpeed, false, targetHeading);
    }

    private void moveToPosition(double targetX, double targetY, double driveSpeed, boolean startIntake) {
        moveToPosition(targetX, targetY, driveSpeed, startIntake, Double.NaN);
    }

    /**
     * Full movement routine: optional intake and optional target heading.
     * If targetHeading is NaN, heading control is ignored.
     */
    private void moveToPosition(double targetX, double targetY, double driveSpeed, boolean startIntake, double targetHeading) {
        if (startIntake) {
            intake.setPower(INTAKE_SPEED);
        }

        boolean reachedTarget = false;

        while (opModeIsActive() && !reachedTarget) {
            SparkFunOTOS.Pose2D currentPos = otos.getPosition();
            double currentX = currentPos.x;
            double currentY = currentPos.y;

            // Translational error
            double errorX = targetX - currentX;
            double errorY = targetY - currentY;

            // Check if target is reached
            if (Math.abs(errorX) < OTOS_TOLERANCE && Math.abs(errorY) < OTOS_TOLERANCE) {
                reachedTarget = true;
                stopDriveMotors();
                break;
            }

            double distance = Math.sqrt(errorX * errorX + errorY * errorY);
            double directionX = errorX / distance;
            double directionY = errorY / distance;

            // Heading correction (if requested)
            double rotate = 0.0;
            if (!Double.isNaN(targetHeading)) {
                double currentHeading = currentPos.heading;
                double headingError = normalizeAngle(targetHeading - currentHeading);
                rotate = headingError * HEADING_KP;
                rotate = Math.max(Math.min(rotate, 1.0), -1.0);
            }

            // Drive toward target with optional heading correction
            driveRobot(directionY * driveSpeed, directionX * driveSpeed, rotate);

            // Telemetry + cooperative multitasking
            updateTelemetry(currentX, currentY, targetX, targetY);
            idle();
        }

        stopDriveMotors();
        if (startIntake) intake.setPower(0);
    }

    private double normalizeAngle(double angle) {
        double a = angle % 360.0;
        if (a > 180.0) a -= 360.0;
        if (a <= -180.0) a += 360.0;
        return a;
    }

    /**
     * Execute shooting sequence:
     * 1. Spin up flywheel
     * 2. Open gate when at speed
     * 3. Run intake for 5 seconds
     * 4. Stop intake and flywheel
     */
    private void shootSequence() {
        telemetry.addData("Status", "Starting Shoot Sequence");
        telemetry.update();

        // Spin up flywheel
        flywheel.setPower(FLYWHEEL_SPEED);
        flywheelTimer.reset();

        // Wait for flywheel to reach speed (adjust time as needed)
        while (opModeIsActive() && flywheelTimer.milliseconds() < 1500) {
            flywheelAtSpeed = flywheelTimer.milliseconds() > 1000;
            telemetry.addData("Flywheel At Speed", flywheelAtSpeed);
            telemetry.addData("Flywheel Timer", flywheelTimer.milliseconds());
            telemetry.update();
        }

        flywheelAtSpeed = true;

        // Gate servo: Move to open position (90 degrees)
        gate.setPosition(GATE_OPEN);

        // Run intake for 5 seconds (shoot 3 artifacts)
        intake.setPower(INTAKE_SPEED);
        intakeTimer.reset();

        while (opModeIsActive() && intakeTimer.milliseconds() < INTAKE_DURATION_MS) {
            telemetry.addData("Intake Timer", intakeTimer.milliseconds());
            telemetry.addData("Status", "Shooting");
            telemetry.update();
        }

        // Stop intake
        intake.setPower(0);

        // Close gate (0 degrees)
        gate.setPosition(GATE_CLOSED);

        // Stop flywheel
        flywheel.setPower(0);
        flywheelAtSpeed = false;

        telemetry.addData("Status", "Shoot Sequence Complete");
        telemetry.update();

        sleep(200); // Brief pause before next sequence
    }

    /**
     * Drive the robot using mecanum drive kinematics
     *
     * @param forward Forward/backward power (-1.0 to 1.0)
     * @param strafe Left/right power (-1.0 to 1.0)
     * @param rotate Rotation power (-1.0 to 1.0)
     */
    private void driveRobot(double forward, double strafe, double rotate) {
        forward = -forward; // Negate for correct orientation
        strafe = strafe * 1.1; // Compensation factor for strafing

        double denominator = Math.max(Math.abs(forward) + Math.abs(strafe) + Math.abs(rotate), 1);
        double frontLeftPower = (forward + strafe + rotate) / denominator;
        double backLeftPower = (forward - strafe + rotate) / denominator;
        double frontRightPower = (forward - strafe - rotate) / denominator;
        double backRightPower = (forward + strafe - rotate) / denominator;

        frontLeftMotor.setPower(frontLeftPower);
        backLeftMotor.setPower(backLeftPower);
        frontRightMotor.setPower(frontRightPower);
        backRightMotor.setPower(backRightPower);
    }

    /**
     * Stop all drive motors
     */
    private void stopDriveMotors() {
        driveRobot(0, 0, 0);
    }

    /**
     * Stop all motors and mechanisms
     */
    private void stopAllMotors() {
        stopDriveMotors();
        intake.setPower(0);
        flywheel.setPower(0);
        gate.setPosition(GATE_CLOSED);
    }

    /**
     * Set zero power behavior for all drive motors
     */
    private void setZeroPowerBehavior() {
        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    /**
     * Update telemetry with current status
     */
    private void updateTelemetry(double currentX, double currentY, double targetX, double targetY) {
        SparkFunOTOS.Pose2D pos = otos.getPosition();

        telemetry.addData("OTOS X", String.format("%.2f", pos.x));
        telemetry.addData("OTOS Y", String.format("%.2f", pos.y));
        telemetry.addData("Target X", String.format("%.2f", targetX));
        telemetry.addData("Target Y", String.format("%.2f", targetY));
        telemetry.addData("Error X", String.format("%.2f", targetX - pos.x));
        telemetry.addData("Error Y", String.format("%.2f", targetY - pos.y));
        telemetry.addData("Flywheel At Speed", flywheelAtSpeed);
        telemetry.update();
    }
}
