package org.firstinspires.ftc.teamcode.pedroPathing;

import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.createFollower;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.hardware.dfrobot.HuskyLens;

import org.firstinspires.ftc.robotcore.internal.system.Deadline;

import java.util.concurrent.TimeUnit;

@TeleOp(name = "December22 Shoot Test", group = "Testing")
public class December22ShootTest_FreeSpin extends LinearOpMode {

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

    private final int READ_PERIOD = 1;

    private HuskyLens huskyLens;

    private DcMotor intake;
    private DcMotorEx flywheel;

    private Servo gate;  // SERVO DISABLED - NOT USED IN THIS TEST

    private Follower follower;
    private MecanumDrive drive;

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
        huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");
        Deadline rateLimit = new Deadline(READ_PERIOD, TimeUnit.SECONDS);
        rateLimit.expire();

        if (!huskyLens.knock()) {
            telemetry.addData(">>", "Problem communicating with " + huskyLens.getDeviceName());
        } else {
            telemetry.addData(">>", "Press start to continue");
        }

        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);

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

            if (!rateLimit.hasExpired()) {
                continue;
            }
            rateLimit.reset();

            HuskyLens.Block[] blocks = huskyLens.blocks();
            telemetry.addData("Block count", blocks.length);
            for (HuskyLens.Block block : blocks) {
                telemetry.addData("Block", block.toString());


                flywheel.setPower(1);
                // --- Drive Train Control ---
                double y = gamepad1.left_stick_y;
                double x = gamepad1.left_stick_x;
                double rx = gamepad1.right_stick_x;

                // Call the drive method from our MecanumDrive class
                drive.drive(y, x, rx);


                // --- Align Robot based on huskylens ---
                if (gamepad1.left_bumper) {
                    if (block.id != 0) {
                        if (block.x > 215) {
                            //STRAFE LEFT until  blocks[i].x between 205 and 215
                            drive.drive(0, -1, 0);
                        } else if (block.x < 205) {
                            //STRAFE RIGHT until blocks[i].x between 205 and 215
                            drive.drive(0, 1, 0);
                        }

                    } else if (block.id == 2) {
                        if (block.x > 215) {
                            //STRAFE LEFT until  blocks[i].x between 205 and 215
                            drive.drive(0, 1, 0);
                        } else if (block.x < 205) {
                            //STRAFE RIGHT until blocks[i].x between 205 and 215
                            drive.drive(0, -1, 0);
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


                if (gamepad1.dpad_left) {
                    gate.setPosition(90);
                } else if (gamepad1.dpad_right) {
                    gate.setPosition(0);
                }


                // --- Telemetry ---


                telemetry.addData("Left Stick Y", y);
                telemetry.addData("Left Stick X", x);
                telemetry.addData("Right Stick X", rx);

                telemetry.addData("Gate Position", gate.getPosition());  // GATE DISABLED
                telemetry.addData("Robot X", follower.getPose().getX());
                telemetry.addData("Robot Y", follower.getPose().getY());
                telemetry.update();
            }
        }
    }







    // ===== TEST SHOOTING MODE (LEFT TRIGGER) - NO POSITION OR SERVO REQUIREMENTS =====

}

