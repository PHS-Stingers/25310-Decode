package org.firstinspires.ftc.teamcode.pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "Driver Controlled", group = "Competition")
public class Decode_TeleOp extends LinearOpMode {

    // Declare your hardware variables
    private DcMotor intake;
    // Add your drive motors here as well for a complete TeleOp
    // private DcMotor frontLeft, frontRight, backLeft, backRight;

    @Override
    public void runOpMode() throws InterruptedException {
        // --- INITIALIZATION PHASE ---
        // Map the intake motor from the hardware configuration
        intake = hardwareMap.get(DcMotor.class, "intake");

        // Set the direction of the motor if needed.
        // For example, if it runs backwards, uncomment the next line.
        // intake.setDirection(DcMotorSimple.Direction.REVERSE);

        // Add telemetry to show that initialization is complete
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // --- WAIT FOR START ---
        waitForStart();

        // --- TELEOP LOOP ---
        while (opModeIsActive()) {
            // Check the state of the right trigger on gamepad 1
            if (gamepad1.right_trigger > 0.1) {
                // If the trigger is pressed, run the intake motor at full power (1.0)
                intake.setPower(1.0);
            } else {
                // If the trigger is not pressed, stop the motor
                intake.setPower(0.0);
            }

            // You can add telemetry to see the trigger value and motor power
            telemetry.addData("Right Trigger", gamepad1.right_trigger);
            telemetry.addData("Intake Power", intake.getPower());
            telemetry.update();
        }
    }
}
