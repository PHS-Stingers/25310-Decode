package org.firstinspires.ftc.teamcode.pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

// No longer need to import from Constants, we have a dedicated class now.

@TeleOp(name = "Driver Controlled", group = "Competition")
public class Decode_TeleOp extends LinearOpMode {

    // Declare your hardware variables
    private DcMotor intake;

    // Declare our new MecanumDrive object
    private MecanumDrive drive;

    @Override
    public void runOpMode() throws InterruptedException {
        // --- INITIALIZATION PHASE ---

        // Initialize the MecanumDrive object. This will map and configure all drive motors.
        drive = new MecanumDrive(hardwareMap);

        // Map the intake motor from the hardware configuration
        intake = hardwareMap.get(DcMotor.class, "intake");

        // Set the direction of the intake motor if needed.
        intake.setDirection(DcMotorSimple.Direction.REVERSE);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // --- WAIT FOR START ---
        waitForStart();

        // --- TELEOP LOOP ---
        while (opModeIsActive()) {
            // --- Drive Train Control ---
            // Get joystick values from gamepad 1.
            // Note: We no longer need to negate the y-stick here; the MecanumDrive class handles it.
            double y = gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;

            // Call the drive method from our MecanumDrive class. This is now clean and simple.
            drive.drive(y, x, rx);

            // --- Intake Control ---
            if (gamepad1.right_trigger > 0.1) {
                intake.setPower(1.0);
            } else {
                intake.setPower(0.0);
            }

            // --- Telemetry ---
            telemetry.addData("Left Stick Y", y);
            telemetry.addData("Left Stick X", x);
            telemetry.addData("Right Stick X", rx);
            telemetry.addData("Intake Power", intake.getPower());
            telemetry.update();
        }
    }
}