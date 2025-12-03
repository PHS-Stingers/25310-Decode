package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;

/**
 * Hardware Configuration for FTC Decode Season 2025
 *
 * This class centralizes all hardware initialization and provides
 * a single source of truth for hardware configuration that can be
 * used by both autonomous and teleop programs.
 *
 * Hardware includes:
 * - 4 Mecanum drive motors
 * - 1 Intake/collector motor
 * - 1 Launcher motor
 * - IMU for field-centric driving
 * - SparkFun OTOS sensor for Pedro Pathing localization
 */
public class HardwareConfig {

    // Drive Motors
    public DcMotor frontLeftDrive = null;
    public DcMotor frontRightDrive = null;
    public DcMotor backLeftDrive = null;
    public DcMotor backRightDrive = null;

    // Mechanism Motors
    public DcMotor intakeMotor = null;
    public DcMotor launcherMotor = null;

    // Sensors
    public IMU imu = null;
    public SparkFunOTOS otos = null;

    // Pedro Pathing
    public Follower follower = null;

    // Hardware device names (configure these in your Robot Configuration on the Control Hub)
    public static final String FRONT_LEFT_DRIVE = "front_left_drive";
    public static final String FRONT_RIGHT_DRIVE = "front_right_drive";
    public static final String BACK_LEFT_DRIVE = "back_left_drive";
    public static final String BACK_RIGHT_DRIVE = "back_right_drive";
    public static final String INTAKE_MOTOR = "intake_motor";
    public static final String LAUNCHER_MOTOR = "launcher_motor";
    public static final String IMU_NAME = "imu";
    public static final String OTOS_NAME = "otos";

    // Drive Constants
    public static final double DRIVE_SPEED = 1.0;
    public static final double TURN_SPEED = 0.8;
    public static final double SLOW_MODE_MULTIPLIER = 0.5;

    // Mechanism Constants
    public static final double INTAKE_SPEED = 0.8;
    public static final double LAUNCHER_SPEED = 1.0;

    // IMU Configuration (adjust based on your hub mounting)
    public static final RevHubOrientationOnRobot.LogoFacingDirection LOGO_DIRECTION =
            RevHubOrientationOnRobot.LogoFacingDirection.UP;
    public static final RevHubOrientationOnRobot.UsbFacingDirection USB_DIRECTION =
            RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

    // Local OpMode members
    private HardwareMap hwMap = null;
    private ElapsedTime period = new ElapsedTime();

    // Flags
    private boolean usePedroPathing = false;

    /**
     * Initialize all hardware devices
     * @param ahwMap The hardware map from the OpMode
     */
    public void init(HardwareMap ahwMap) {
        init(ahwMap, false);
    }

    /**
     * Initialize all hardware devices with option to enable Pedro Pathing
     * @param ahwMap The hardware map from the OpMode
     * @param enablePedroPathing Whether to initialize Pedro Pathing follower
     */
    public void init(HardwareMap ahwMap, boolean enablePedroPathing) {
        hwMap = ahwMap;
        usePedroPathing = enablePedroPathing;

        // Initialize drive motors
        frontLeftDrive = hwMap.get(DcMotor.class, FRONT_LEFT_DRIVE);
        frontRightDrive = hwMap.get(DcMotor.class, FRONT_RIGHT_DRIVE);
        backLeftDrive = hwMap.get(DcMotor.class, BACK_LEFT_DRIVE);
        backRightDrive = hwMap.get(DcMotor.class, BACK_RIGHT_DRIVE);

        // Initialize mechanism motors
        intakeMotor = hwMap.get(DcMotor.class, INTAKE_MOTOR);
        launcherMotor = hwMap.get(DcMotor.class, LAUNCHER_MOTOR);

        // Set motor directions
        // Left side motors need to be reversed for mecanum drive
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.FORWARD);

        // Set mechanism motor directions (adjust as needed)
        intakeMotor.setDirection(DcMotor.Direction.FORWARD);
        launcherMotor.setDirection(DcMotor.Direction.FORWARD);

        // Set all motors to brake when power is zero
        setDriveMotorBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        launcherMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Initialize motors with encoders
        setDriveMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);

        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        launcherMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Set all motors to zero power
        stopAllMotors();

        // Initialize IMU
        imu = hwMap.get(IMU.class, IMU_NAME);
        RevHubOrientationOnRobot orientationOnRobot =
                new RevHubOrientationOnRobot(LOGO_DIRECTION, USB_DIRECTION);
        imu.initialize(new IMU.Parameters(orientationOnRobot));
        imu.resetYaw();

        // Initialize OTOS sensor
        otos = hwMap.get(SparkFunOTOS.class, OTOS_NAME);

        // Initialize Pedro Pathing if requested
        if (enablePedroPathing) {
            follower = Constants.createFollower(hwMap);
        }
    }

    /**
     * Set the starting pose for Pedro Pathing
     * @param startPose The starting pose of the robot
     */
    public void setStartingPose(Pose startPose) {
        if (follower != null) {
            follower.setStartingPose(startPose);
        }
    }

    /**
     * Update Pedro Pathing follower (call this in loop)
     */
    public void updateFollower() {
        if (follower != null) {
            follower.update();
        }
    }

    /**
     * Get the current pose from Pedro Pathing
     * @return Current robot pose
     */
    public Pose getPose() {
        if (follower != null) {
            return follower.getPose();
        }
        return new Pose(0, 0, 0);
    }

    /**
     * Set the run mode for all drive motors
     */
    public void setDriveMode(DcMotor.RunMode mode) {
        frontLeftDrive.setMode(mode);
        frontRightDrive.setMode(mode);
        backLeftDrive.setMode(mode);
        backRightDrive.setMode(mode);
    }

    /**
     * Set the zero power behavior for all drive motors
     */
    public void setDriveMotorBehavior(DcMotor.ZeroPowerBehavior behavior) {
        frontLeftDrive.setZeroPowerBehavior(behavior);
        frontRightDrive.setZeroPowerBehavior(behavior);
        backLeftDrive.setZeroPowerBehavior(behavior);
        backRightDrive.setZeroPowerBehavior(behavior);
    }

    /**
     * Stop all motors
     */
    public void stopAllMotors() {
        frontLeftDrive.setPower(0);
        frontRightDrive.setPower(0);
        backLeftDrive.setPower(0);
        backRightDrive.setPower(0);
        intakeMotor.setPower(0);
        launcherMotor.setPower(0);
    }

    /**
     * Mecanum drive method - Robot Relative
     * @param forward Forward/backward movement (-1.0 to 1.0)
     * @param right Strafe left/right movement (-1.0 to 1.0)
     * @param rotate Rotation (-1.0 to 1.0)
     */
    public void drive(double forward, double right, double rotate) {
        // If using Pedro Pathing in teleop mode, use its drive method
        if (usePedroPathing && follower != null) {
            follower.setTeleOpDrive(forward, right, rotate, false);
            return;
        }

        // Calculate power for each wheel
        double frontLeftPower = forward + right + rotate;
        double frontRightPower = forward - right - rotate;
        double backRightPower = forward + right - rotate;
        double backLeftPower = forward - right + rotate;

        // Normalize powers to ensure none exceed 1.0
        double maxPower = Math.max(1.0, Math.abs(frontLeftPower));
        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
        maxPower = Math.max(maxPower, Math.abs(backRightPower));
        maxPower = Math.max(maxPower, Math.abs(backLeftPower));

        // Set motor powers
        frontLeftDrive.setPower(frontLeftPower / maxPower);
        frontRightDrive.setPower(frontRightPower / maxPower);
        backLeftDrive.setPower(backLeftPower / maxPower);
        backRightDrive.setPower(backRightPower / maxPower);
    }

    /**
     * Mecanum drive method - Field Relative
     * Drives relative to the field rather than robot orientation
     * @param forward Forward/backward movement (-1.0 to 1.0)
     * @param right Strafe left/right movement (-1.0 to 1.0)
     * @param rotate Rotation (-1.0 to 1.0)
     */
    public void driveFieldRelative(double forward, double right, double rotate) {
        // If using Pedro Pathing in teleop mode, use its drive method
        if (usePedroPathing && follower != null) {
            follower.setTeleOpDrive(forward, right, rotate, true);
            return;
        }

        // Convert to polar coordinates
        double theta = Math.atan2(forward, right);
        double r = Math.hypot(right, forward);

        // Rotate angle by robot heading
        theta = AngleUnit.normalizeRadians(theta -
                imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));

        // Convert back to cartesian
        double newForward = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);

        // Drive with adjusted values
        drive(newForward, newRight, rotate);
    }

    /**
     * Reset the IMU yaw to zero
     */
    public void resetYaw() {
        imu.resetYaw();
    }

    /**
     * Get the current heading of the robot in degrees
     */
    public double getHeading() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    /**
     * Get the current heading of the robot in radians
     */
    public double getHeadingRadians() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
    }

    /**
     * Control the intake motor
     * @param power Power level (-1.0 to 1.0)
     */
    public void setIntakePower(double power) {
        intakeMotor.setPower(power);
    }

    /**
     * Control the launcher motor
     * @param power Power level (-1.0 to 1.0)
     */
    public void setLauncherPower(double power) {
        launcherMotor.setPower(power);
    }

    /**
     * Run intake to collect game pieces
     */
    public void runIntake() {
        intakeMotor.setPower(INTAKE_SPEED);
    }

    /**
     * Run intake in reverse to eject game pieces
     */
    public void reverseIntake() {
        intakeMotor.setPower(-INTAKE_SPEED);
    }

    /**
     * Stop the intake
     */
    public void stopIntake() {
        intakeMotor.setPower(0);
    }

    /**
     * Run launcher at full speed
     */
    public void runLauncher() {
        launcherMotor.setPower(LAUNCHER_SPEED);
    }

    /**
     * Stop the launcher
     */
    public void stopLauncher() {
        launcherMotor.setPower(0);
    }
}

