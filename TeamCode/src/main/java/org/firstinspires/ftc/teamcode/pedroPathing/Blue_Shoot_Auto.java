package org.firstinspires.ftc.teamcode.pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name = "Blue_Shoot_Auto", group = "Competition")
public class Blue_Shoot_Auto extends LinearOpMode {

    // Declare drive motor objects
    public DcMotorEx frontLeft;
    public DcMotorEx frontRight;
    public DcMotorEx backLeft;
    public DcMotorEx backRight;
    private ElapsedTime runtime = new ElapsedTime();
    //Added fly wheel
    public  Servo gate;

    public DcMotor intake;

    private DcMotorEx flywheel;

    // ===== DISTANCE-TO-TIME CALIBRATION RATIOS (EDITABLE) =====
    // These values represent the time (in milliseconds) needed to travel 1 centimeter at full power
    // Calibrate by measuring actual distance traveled and adjusting these values
    // Calibration Reference: Robot travels 106cm in 750ms
    private static final double FORWARD_MS_PER_CM = 7.075;    // Time in ms to travel 1 cm forward (750ms / 106cm)
    private static final double STRAFE_MS_PER_CM = 7.075;     // Time in ms to travel 1 cm strafe (sideways)
    private static final double ROTATE_MS_PER_DEGREE = 10.0; // Time in ms to rotate 1 degree


    @Override
    public void runOpMode() throws InterruptedException {
        frontLeft = hardwareMap.get(DcMotorEx.class, "leftFront");
        frontRight = hardwareMap.get(DcMotorEx.class, "rightFront");
        backLeft = hardwareMap.get(DcMotorEx.class, "leftRear");
        backRight = hardwareMap.get(DcMotorEx.class, "rightRear");
        intake = hardwareMap.get(DcMotor.class, "intake");
        flywheel = hardwareMap.get(DcMotorEx.class, "output");
        gate = hardwareMap.get(Servo.class, "gate");

        // Reverse the left-side motors so they spin in the correct direction
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        backRight.setDirection(DcMotorSimple.Direction.FORWARD);

        intake.setDirection(DcMotorSimple.Direction.REVERSE);
        flywheel.setDirection(DcMotorEx.Direction.REVERSE);

        frontLeft.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();
        flywheel.setPower(1);


        driveDistance(-70);
        gate.setPosition(150);
        intake.setPower(1);

        sleep(2000);

        intake.setPower(0);
        gate.setPosition(0);
strafeDistance(-104);
intake.setPower(1);
driveDistance(91.5);
        intake.setPower(0);
        driveDistance(-91.5);
strafeDistance(104);
gate.setPosition(150);
intake.setPower(1);
sleep(2000);
intake.setPower(0);
sleep(28000);




    }

    // Method to control the robot with mecanum drive inputs
    public void drive(double y, double x, double rx) {
        // The y-stick is not inverted in this case
        y = y;
        // This factor can be used to counteract imperfect strafing
        x = x * 1.1;
        // Apply rotational power scale to rotation input
        rx = rx * Constants.RotationalPowerScale / Constants.PowerScale;

        // Calculate the power for each wheel
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
        double frontLeftPower = (y + x + rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;

        // Apply PowerScale from Constants to all motor outputs
        frontLeft.setPower(frontLeftPower * Constants.PowerScale);
        backLeft.setPower(backLeftPower * Constants.PowerScale);
        frontRight.setPower(frontRightPower * Constants.PowerScale);
        backRight.setPower(backRightPower * Constants.PowerScale);
    }

    /**
     * Drive forward for a specific distance at full power.
     * Uses the FORWARD_MS_PER_CM calibration ratio to calculate required time.
     *
     * @param distanceCM The distance to travel in centimeters (positive = forward, negative = backward)
     */
    public void driveDistance(double distanceCM) {
        long timeMS = (long) (Math.abs(distanceCM) * FORWARD_MS_PER_CM);
        double direction = distanceCM >= 0 ? 1.0 : -1.0;
        drive(direction, 0, 0);
        sleep(timeMS);
        drive(0, 0, 0);
    }

    /**
     * Strafe (move sideways) for a specific distance at full power.
     * Uses the STRAFE_MS_PER_CM calibration ratio to calculate required time.
     *
     * @param distanceCM The distance to strafe in centimeters (positive = right, negative = left)
     */
    public void strafeDistance(double distanceCM) {
        long timeMS = (long) (Math.abs(distanceCM) * STRAFE_MS_PER_CM);
        double direction = distanceCM >= 0 ? 1.0 : -1.0;
        drive(0, direction, 0);
        sleep(timeMS);
        drive(0, 0, 0);
    }

    /**
     * Rotate the robot for a specific angle at full power.
     * Uses the ROTATE_MS_PER_DEGREE calibration ratio to calculate required time.
     *
     * @param angleDegrees The angle to rotate in degrees (positive = counterclockwise, negative = clockwise)
     */
    public void rotateAngle(double angleDegrees) {
        long timeMS = (long) (Math.abs(angleDegrees) * ROTATE_MS_PER_DEGREE);
        double direction = angleDegrees >= 0 ? 1.0 : -1.0;
        drive(0, 0, direction);
        sleep(timeMS);
        drive(0, 0, 0);
    }

    /**
     * Perform a combined movement with distance and rotation.
     * Useful for diagonal movements or curved paths.
     *
     * @param distanceCM The forward/backward distance in centimeters
     * @param strafeCM The lateral distance in centimeters (positive = right, negative = left)
     * @param angleDegrees The rotation angle in degrees
     */
    public void driveCombined(double distanceCM, double strafeCM, double angleDegrees) {
        long timeMS = (long) Math.max(
            Math.max(Math.abs(distanceCM) * FORWARD_MS_PER_CM, Math.abs(strafeCM) * STRAFE_MS_PER_CM),
            Math.abs(angleDegrees) * ROTATE_MS_PER_DEGREE
        );

        double yDirection = distanceCM >= 0 ? 1.0 : -1.0;
        double xDirection = strafeCM >= 0 ? 1.0 : -1.0;
        double rxDirection = angleDegrees >= 0 ? 1.0 : -1.0;

        drive(yDirection, xDirection, rxDirection);
        sleep(timeMS);
        drive(0, 0, 0);
    }

}
