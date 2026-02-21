package org.firstinspires.ftc.teamcode.pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "December22 Shoot Test", group = "Testing")
public class December22ShootTest extends LinearOpMode {

    // ===== HARDWARE DECLARATIONS =====
    private DcMotor intake;
    private DcMotorEx flywheel;
    private Servo gate;
    private MecanumDrive drive;

    @Override
    public void runOpMode() {
        // --- INITIALIZATION PHASE ---
        drive = new MecanumDrive(hardwareMap);

        // Map the intake motor from the hardware configuration
        intake = hardwareMap.get(DcMotor.class, "intake");

        // Map the flywheel motor
        flywheel = hardwareMap.get(DcMotorEx.class, "output");

        // Map the gate servo
        gate = hardwareMap.get(Servo.class, "gate");

        // Set the direction of the intake motor
        intake.setDirection(DcMotorSimple.Direction.REVERSE);
        flywheel.setDirection(DcMotorEx.Direction.REVERSE);

        // Configure flywheel motor for brake on zero power
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // --- WAIT FOR START ---
        waitForStart();

        // --- TELEOP LOOP ---
        while (opModeIsActive()) {
            // --- Drive Train Control ---
            double y = gamepad1.left_stick_y;
            //double y = gamepad1.left_trigger;
           double x = gamepad1.left_stick_x;
            //double x = gamepad1.right_trigger;
            double rx = gamepad1.right_stick_x;

            // Call the drive method from our MecanumDrive class
            drive.drive(y, x, rx);

            // --- Intake Control (Right Trigger) ---
//                if (gamepad1.dpad_down) {
//                    intake.setPower(1.0);
//                } else {
//                    intake.setPower(0.0);
//                }

                // --- Flywheel Control ---
                // Left Trigger - Run flywheel at full power
                if (gamepad1.left_trigger > 0.1) {
                    flywheel.setPower(1.0);
                }
                // Right Bumper - Reverse flywheel and intake
//            else if (gamepad1.right_bumper) {
//                flywheel.setPower(-1.0);
//                intake.setPower(-1.0);
//            }
//            // D-Pad Down - Run flywheel at full power (alternative)
//            else if (gamepad1.right_trigger > 0.1) {
//                flywheel.setPower(1.0);
//            }
//            // Default - Flywheel off
            else {
                flywheel.setPower(0.0);
            }

            // --- Gate Control ---
            if (gamepad1.dpad_left) {
                gate.setPosition(90);
            } else if (gamepad1.dpad_right) {
                gate.setPosition(0);
            }

            // --- Telemetry ---
            telemetry.addData("Status", "Running");
            telemetry.addData("Left Trigger", gamepad1.left_trigger);
            telemetry.addData("Right Trigger", gamepad1.right_trigger);
            telemetry.addData("Intake Power", intake.getPower());
            telemetry.addData("Flywheel Power", flywheel.getPower());
            telemetry.addData("Gate Position", gate.getPosition());
            telemetry.update();
        }
    }
}


