package org.firstinspires.ftc.teamcode.pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Flywheel Test", group = "Testing")
public class FlywheelTest extends LinearOpMode {

    private DcMotorEx flyWheel;
    private Servo feeder;

    // Configuration
    private final double TARGET_RPM = 3000.0; // Adjust this to your desired RPM
    private final double RPM_TOLERANCE = 50.0; // RPM tolerance (within this range = "at speed")

    // State tracking
    private boolean flywheelSpinning = false;
    private boolean atTargetSpeed = false;
    private boolean ballFed = false;
    private ElapsedTime spinupTimer = new ElapsedTime();
    private ElapsedTime monitorTimer = new ElapsedTime();

    // Data collection
    private double spinupTime = 0.0;
    private double rpmBeforeLaunch = 0.0;
    private double rpmAfterLaunch = 0.0;
    private double minRpmDuringLaunch = Double.MAX_VALUE;
    private boolean aButtonPressed = false;
    private boolean bButtonPressed = false;

    @Override
    public void runOpMode() {
        // Initialize hardware
        flyWheel = hardwareMap.get(DcMotorEx.class, "Output");
        feeder = hardwareMap.get(Servo.class, "feeder");

        // Configure flywheel motor
        flyWheel.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        flyWheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        flyWheel.setDirection(DcMotorEx.Direction.REVERSE);

        // Set feeder to starting position
        feeder.setPosition(0.0);

        telemetry.addData("Status", "Ready to test flywheel");
        telemetry.addData("Instructions", "Press A to spin up flywheel");
        telemetry.addData("Target RPM", TARGET_RPM);
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Get current RPM
            double currentRPM = getCurrentRPM();

            // Handle A button - Spin up flywheel
            if (gamepad1.a && !aButtonPressed) {
                aButtonPressed = true;
                startFlywheel();
            } else if (!gamepad1.a) {
                aButtonPressed = false;
            }

            // Handle B button - Stop flywheel and reset
            if (gamepad1.b && !bButtonPressed) {
                bButtonPressed = true;
                stopFlywheel();
            } else if (!gamepad1.b) {
                bButtonPressed = false;
            }

            // Handle X button - Feed ball (manual trigger)
            if (gamepad1.x && atTargetSpeed && !ballFed) {
                feedBall();
            }

            // Monitor flywheel state
            if (flywheelSpinning) {
                // Check if we've reached target speed
                if (!atTargetSpeed && isAtTargetSpeed(currentRPM)) {
                    atTargetSpeed = true;
                    spinupTime = spinupTimer.seconds();
                }

                // Monitor for RPM drop (indicating ball launch)
                if (atTargetSpeed && !ballFed) {
                    if (currentRPM < rpmBeforeLaunch - RPM_TOLERANCE * 2) {
                        // Detected significant RPM drop - ball was launched
                        ballFed = true;
                        monitorTimer.reset();
                    } else {
                        rpmBeforeLaunch = currentRPM;
                    }
                }

                // Track minimum RPM during launch
                if (ballFed && monitorTimer.seconds() < 2.0) {
                    if (currentRPM < minRpmDuringLaunch) {
                        minRpmDuringLaunch = currentRPM;
                    }
                    if (currentRPM > rpmBeforeLaunch - RPM_TOLERANCE) {
                        // RPM has recovered, capture the data
                        rpmAfterLaunch = currentRPM;
                    }
                }
            }

            // Display telemetry
            updateTelemetry(currentRPM);
        }
    }

    private void startFlywheel() {
        flywheelSpinning = true;
        atTargetSpeed = false;
        ballFed = false;
        rpmBeforeLaunch = 0.0;
        rpmAfterLaunch = 0.0;
        minRpmDuringLaunch = Double.MAX_VALUE;
        spinupTimer.reset();

        flyWheel.setPower(1.0);

        telemetry.addData("Status", "Spinning up...");
        telemetry.update();
    }

    private void stopFlywheel() {
        flyWheel.setPower(0.0);
        feeder.setPosition(0.0);
        flywheelSpinning = false;
        atTargetSpeed = false;
        ballFed = false;

        telemetry.addData("Status", "Flywheel stopped");
        telemetry.update();
    }

    private void feedBall() {
        rpmBeforeLaunch = getCurrentRPM();
        feeder.setPosition(1.0);
        sleep(500);
        feeder.setPosition(0.0);
        ballFed = true;
        monitorTimer.reset();
    }

    private double getCurrentRPM() {
        double ticksPerRevolution = flyWheel.getMotorType().getTicksPerRev();
        double velocity = flyWheel.getVelocity(); // ticks per second
        return (velocity / ticksPerRevolution) * 60.0; // convert to RPM
    }

    private boolean isAtTargetSpeed(double currentRPM) {
        return Math.abs(currentRPM - TARGET_RPM) <= RPM_TOLERANCE;
    }

    private void updateTelemetry(double currentRPM) {
        telemetry.addData("===== FLYWHEEL TEST =====", "");
        telemetry.addData("Target RPM", "%.0f", TARGET_RPM);
        telemetry.addData("Current RPM", "%.0f", currentRPM);
        telemetry.addData("Flywheel Status", flywheelSpinning ? "RUNNING" : "STOPPED");

        if (flywheelSpinning) {
            if (!atTargetSpeed) {
                telemetry.addData("Status", "SPINNING UP... (%.1f sec)", spinupTimer.seconds());
                double percentOfTarget = (currentRPM / TARGET_RPM) * 100.0;
                telemetry.addData("Progress", "%.1f%%", percentOfTarget);
            } else if (!ballFed) {
                telemetry.addData("Status", "*** READY TO LAUNCH ***");
                telemetry.addData("Action", "Press X to feed ball");
                telemetry.addData("Spinup Time", "%.2f seconds", spinupTime);
            } else {
                telemetry.addData("Status", "MONITORING LAUNCH");
                telemetry.addData("Time Since Launch", "%.2f sec", monitorTimer.seconds());
            }
        }

        telemetry.addData("", "");
        telemetry.addData("===== PERFORMANCE DATA =====", "");

        if (spinupTime > 0) {
            telemetry.addData("Time to Target Speed", "%.2f seconds", spinupTime);
        }

        if (ballFed) {
            double rpmDrop = rpmBeforeLaunch - minRpmDuringLaunch;
            double percentDrop = (rpmDrop / rpmBeforeLaunch) * 100.0;

            telemetry.addData("RPM Before Launch", "%.0f", rpmBeforeLaunch);
            telemetry.addData("Minimum RPM", "%.0f", minRpmDuringLaunch);
            telemetry.addData("RPM Drop", "%.0f (%.1f%%)", rpmDrop, percentDrop);

            if (rpmAfterLaunch > 0) {
                telemetry.addData("Recovery RPM", "%.0f", rpmAfterLaunch);
            }
        }

        telemetry.addData("", "");
        telemetry.addData("===== CONTROLS =====", "");
        telemetry.addData("A Button", "Start/Restart Flywheel");
        telemetry.addData("B Button", "Stop Flywheel");
        telemetry.addData("X Button", "Feed Ball (Manual)");

        telemetry.update();
    }
}

