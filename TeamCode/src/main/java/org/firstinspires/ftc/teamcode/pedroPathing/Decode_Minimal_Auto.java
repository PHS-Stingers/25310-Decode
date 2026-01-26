package org.firstinspires.ftc.teamcode.pedroPathing;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Decode_Minimal_Auto extends LinearOpMode {

    // Declare drive motor objects
    public DcMotor frontLeft;
    public DcMotor frontRight;
    public DcMotor backLeft;
    public DcMotor backRight;
    private ElapsedTime runtime = new ElapsedTime();
    @Override
    public void runOpMode(){
        frontLeft = hardwareMap.get(DcMotor.class, "leftFront");
        frontRight = hardwareMap.get(DcMotor.class, "rightFront");
        backLeft = hardwareMap.get(DcMotor.class, "leftRear");
        backRight = hardwareMap.get(DcMotor.class, "rightRear");

        // Reverse the left-side motors so they spin in the correct direction
        frontLeft.setDirection(DcMotorEx.Direction.REVERSE);
        backLeft.setDirection(DcMotorEx.Direction.REVERSE);
        frontRight.setDirection(DcMotorEx.Direction.REVERSE);
        backRight.setDirection(DcMotorEx.Direction.REVERSE);

        frontLeft.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);

        frontLeft.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        waitForStart();
        drive(1,0,0);
        wait(1000);
        drive(0,0,0);
        while(runtime<30000){

        }
    }
    // Constructor: This runs when you create a new MecanumDrive object
    // Map the motors from the hardware configuration


    // Method to control the robot with joystick inputs
    public void drive(double y, double x, double rx) {
        // The y-stick is inverted, so we negate it
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
}
