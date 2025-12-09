package org.firstinspires.ftc.teamcode.pedroPathing;



import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.Poses.startPose;
//import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.MecanumConstants;

import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.Paths;
import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.createFollower;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;


@Autonomous(name = "Decode Auto", group = "Competition")
public class Decode_Auto extends LinearOpMode {



    private Follower follower;
    private Timer pathTimer, actionTimer, opmodeTimer;
    private int pathState;
    private DcMotor intake;
    private DcMotor flyWheel;
public static class shoot(double shooterPower) {

}








    public void runOpMode() {
        // Initialize the follower. This is a crucial first step.
        // Make sure your hardware is mapped correctly in your Follower's constructor if needed.
        follower = createFollower(hardwareMap);

        // Set the robot's starting position.


        // Now that the follower is initialized, create an instance of your Paths class.
        // This will build all 8 of your paths.
        Paths paths = new Paths(follower);

        // Wait for the start button to be pressed on the Driver Station.
        waitForStart();

        // The OpMode is now running.
        if (opModeIsActive()) {
            // Follow each path in numerical order.
            // The 'follower.followPath()' method is blocking, meaning it will wait
            // until the path is complete before moving to the next line.

            telemetry.addData("Status", "Following Path 1");
            telemetry.update();
            follower.followPath(paths.Path1);


            telemetry.addData("Status", "Following Path 2");
            telemetry.update();
            follower.followPath(paths.Path2);

           // MecanumConstants.maxPower(0.5);
            telemetry.addData("Status", "Following Pickup 1");
            telemetry.update();
            follower.followPath(paths.PathPickup1);
            //MecanumConstants.maxPower(1);

            telemetry.addData("Status", "Following Path 3");
            telemetry.update();
            follower.followPath(paths.Path3);

            telemetry.addData("Status", "Following Path 4");
            telemetry.update();
            follower.followPath(paths.Path4);

           // MecanumConstants.maxPower(0.5);
            telemetry.addData("Status", "Following Pickup 2");
            telemetry.update();
            follower.followPath(paths.PathPickup2);
            //MecanumConstants.maxPower(1);

            telemetry.addData("Status", "Following Path 5");
            telemetry.update();
            follower.followPath(paths.Path5);

            telemetry.addData("Status", "Following Path 6");
            telemetry.update();
            follower.followPath(paths.Path6);

            //MecanumConstants.maxPower(0.5);
            telemetry.addData("Status", "Following Pickup 3");
            telemetry.update();
            follower.followPath(paths.PathPickup3);
            //MecanumConstants.maxPower(1);

            telemetry.addData("Status", "Following Path 7");
            telemetry.update();
            follower.followPath(paths.Path7);

            telemetry.addData("Status", "Following Path 8");
            telemetry.update();
            follower.followPath(paths.Path8);

            telemetry.addData("Status", "Autonomous Finished");
            telemetry.update();
        }
    }




    }





