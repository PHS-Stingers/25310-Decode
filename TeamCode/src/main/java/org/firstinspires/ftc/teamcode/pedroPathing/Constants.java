package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import static org.firstinspires.ftc.teamcode.pedroPathing.Constants.createFollower;

import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.pedropathing.ftc.localization.constants.OTOSConstants;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class Constants {

    // ===== DRIVETRAIN POWER SCALE =====
    public static final double PowerScale = 1;  // Scales all mecanum motor outputs to 70%
    public static final double RotationalPowerScale = PowerScale * 0.70;  // Rotation speed at 70% of PowerScale

    public static class Paths {

        public PathChain Path1;
        public PathChain Path2;
        public PathChain PathPickup1;
        public PathChain Path3;
        public PathChain Path4;
        public PathChain PathPickup2;
        public PathChain Path5;
        public PathChain Path6;
        public PathChain PathPickup3;
        public PathChain Path7;
        public PathChain Path8;

        public Paths(Follower follower) {
            Path1 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(56.000, 10.000), new Pose(70.000, 20.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(60))
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(70.000, 20.000), new Pose(40.000, 85.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(60), Math.toRadians(180))
                    .build();

            PathPickup1 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(40.000, 85.000), new Pose(25.000, 85.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            Path3 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(25.000, 85.000), new Pose(47.000, 95.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(45))
                    .build();

            Path4 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(59.000, 92.000), new Pose(40.000, 60.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(45), Math.toRadians(180))
                    .build();

            PathPickup2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(40.000, 60.000), new Pose(25.000, 60.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            Path5 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(25.000, 60.000), new Pose(47.000, 95.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(45))
                    .build();

            Path6 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(59.000, 92.000), new Pose(40.000, 35.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(45), Math.toRadians(180))
                    .build();
            PathPickup3 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(40.000, 35.000), new Pose(25.000, 35.000))
                    )
                    // Add the missing heading interpolation
                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    // Add the final .build() call and the semicolon
                    .build();

            Path7 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(25.000, 35.000), new Pose(47.000, 95.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(45))
                    .build();

            Path8 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(59.000, 92.000), new Pose(20.000, 70.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(45), Math.toRadians(270))
                    .build();
        }
    }
    public static FollowerConstants followerConstants = new FollowerConstants()
            .mass(8); //mass of robot in KG
      //      .forwardZeroPowerAcceleration(-288.49743224779564)
//            .translationalPIDFCoefficients(new PIDFCoefficients(0.037, 0, 0.05, .033))
//            .headingPIDFCoefficients(new PIDFCoefficients(0, 0, 0, .01));
    public static class Poses { // <-- Corrected line
        // Alliance-specific starting poses
        public static final Pose startPoseRed = new Pose(18.2, 115.5, Math.toRadians(0));      // Red alliance: (18.2, 115.5) at 0 degrees
        public static final Pose startPoseBlue = new Pose(125.8, 115.5, Math.toRadians(180)); // Blue alliance: (125.8, 115.5) at 180 degrees

        // Legacy pose for backward compatibility
        public static final Pose startPose = new Pose(18.2, 119, Math.toRadians(90));

        public static final Pose backScorePose = new Pose(70, 20, Math.toRadians(120));
        public static final Pose pickup1Pose = new Pose(39,35, Math.toRadians(180));
        public static final Pose pickup2Pose = new Pose(39, 60, Math.toRadians(180));
        public static final Pose pickup3Pose = new Pose(39, 85, Math.toRadians(180));
        public static final Pose frontScorePose = new Pose(47, 95, Math.toRadians(135));
        public static final Pose GatePose = new Pose(20, 70, Math.toRadians(180));
    }
    public static MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1)
            .rightFrontMotorName("rightFront")
            .rightRearMotorName("rightRear")
            .leftRearMotorName("leftRear")
            .leftFrontMotorName("leftFront")
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .xVelocity(53.7588839593842274)
            .yVelocity(36.00413405050443);


    public static PathConstraints pathConstraints = new PathConstraints(0.99, 100, 1, 1);
    public static OTOSConstants localizerConstants = new OTOSConstants()
            .hardwareMapName("otos")
            .linearUnit(DistanceUnit.INCH)
            .angleUnit(AngleUnit.DEGREES)
            .linearScalar(-1.88)
            .angularScalar(0.0172)

            .offset(new SparkFunOTOS.Pose2D(0,-7.874,90));
    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pathConstraints(pathConstraints)
                .OTOSLocalizer(localizerConstants)
                .mecanumDrivetrain(driveConstants)


                /* other builder steps */
                .build();

    }
}
