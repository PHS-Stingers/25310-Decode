# Pedro Pathing Integration Guide

## Overview
Your HardwareConfig class now integrates Pedro Pathing with the SparkFun OTOS sensor for precise autonomous path following. This guide explains how to use it effectively.

## What is Pedro Pathing?
Pedro Pathing is a path-following library for FTC robots that provides:
- Accurate localization using the SparkFun OTOS sensor
- Smooth path following with Bezier curves
- Field-centric control
- Real-time position tracking

## Hardware Configuration on Control Hub

### Required Devices
Configure these devices in your Robot Configuration:

1. **Drive Motors** (all 4 mecanum wheels):
   - `front_left_drive`
   - `front_right_drive`
   - `back_left_drive`
   - `back_right_drive`

2. **Mechanism Motors**:
   - `intake_motor`
   - `launcher_motor`

3. **Sensors**:
   - `imu` (Built-in IMU)
   - `otos` (SparkFun OTOS sensor on I2C port)

## Using HardwareConfig

### For Autonomous (with Pedro Pathing)
```java
@Autonomous(name = "My Autonomous")
public class MyAutonomous extends LinearOpMode {
    HardwareConfig robot = new HardwareConfig();
    
    @Override
    public void runOpMode() {
        // Initialize with Pedro Pathing enabled
        robot.init(hardwareMap, true);
        
        // Set starting position (in inches)
        robot.setStartingPose(new Pose(0, 0, 0));
        
        waitForStart();
        
        // Create a path
        Path myPath = new BezierLine(
            new Pose(0, 0, 0),    // Start
            new Pose(24, 24, 0)   // End
        );
        
        // Follow the path
        robot.follower.followPath(myPath);
        while (opModeIsActive() && robot.follower.isBusy()) {
            robot.updateFollower();  // IMPORTANT: Call this in loop
        }
    }
}
```

### For TeleOp (without Pedro Pathing)
```java
@TeleOp(name = "My TeleOp")
public class MyTeleOp extends OpMode {
    HardwareConfig robot = new HardwareConfig();
    
    @Override
    public void init() {
        // Initialize without Pedro Pathing
        robot.init(hardwareMap);
    }
    
    @Override
    public void loop() {
        // Use built-in drive methods
        double forward = -gamepad1.left_stick_y;
        double strafe = gamepad1.left_stick_x;
        double rotate = gamepad1.right_stick_x;
        
        robot.driveFieldRelative(forward, strafe, rotate);
    }
}
```

### For TeleOp (with Pedro Pathing localization)
```java
@TeleOp(name = "My TeleOp with OTOS")
public class MyAdvancedTeleOp extends OpMode {
    HardwareConfig robot = new HardwareConfig();
    
    @Override
    public void init() {
        // Initialize WITH Pedro Pathing for localization
        robot.init(hardwareMap, true);
        robot.follower.startTeleopDrive();
    }
    
    @Override
    public void loop() {
        double forward = -gamepad1.left_stick_y;
        double strafe = gamepad1.left_stick_x;
        double rotate = gamepad1.right_stick_x;
        
        // Drive methods automatically use Pedro Pathing when enabled
        robot.driveFieldRelative(forward, strafe, rotate);
        robot.updateFollower();  // Updates position tracking
        
        // Display position
        Pose pose = robot.getPose();
        telemetry.addData("X", pose.getX());
        telemetry.addData("Y", pose.getY());
        telemetry.addData("Heading", Math.toDegrees(pose.getHeading()));
    }
}
```

## Path Types

### 1. Straight Line (BezierLine)
```java
Path straightPath = new BezierLine(
    new Pose(0, 0, 0),      // Start position
    new Pose(48, 0, 0)      // End position
);
```

### 2. Curved Path (BezierCurve)
```java
Path curvedPath = new BezierCurve(
    new Pose(0, 0, 0),           // Start
    new Pose(24, 12, 0),         // Control point
    new Pose(48, 24, Math.toRadians(90))  // End
);
```

### 3. Multi-Point Path
```java
Path complexPath = new BezierCurve(
    new Pose(0, 0, 0),
    new Pose(12, 12, 0),
    new Pose(24, 12, 0),
    new Pose(36, 0, 0),
    new Pose(48, 0, Math.toRadians(90))
);
```

## Coordinate System

Pedro Pathing uses inches and radians:
- **X**: Right is positive
- **Y**: Forward is positive
- **Heading**: Counterclockwise is positive (0 = facing right)

```
     Y (Forward)
     ^
     |
     |
     +-----> X (Right)
    
Heading = 0° (right)
Heading = 90° (forward)
Heading = 180° (left)
Heading = 270° (backward)
```

## Tuning Your Robot

### Step 1: Run Localization Test
1. Select "Tuning" OpMode
2. Choose "Localization" → "Localization Test"
3. Drive the robot around and verify position tracking is accurate
4. The robot position should match the field coordinates

### Step 2: Tune OTOS Offset (if needed)
In `Constants.java`, adjust the OTOS offset:
```java
.offset(new SparkFunOTOS.Pose2D(0, 0, 0))  // X, Y, Heading offset
```
This compensates for the sensor not being at the robot's center.

### Step 3: Test Path Following
Create a simple test autonomous with a known path and verify the robot follows it accurately.

## Common Patterns

### Pattern 1: Score → Collect → Score
```java
// Drive to scoring position
Pose scoringPose = new Pose(24, 0, 0);
robot.follower.followPath(new BezierLine(startPose, scoringPose));
waitForPath();

// Score
robot.runLauncher();
sleep(2000);
robot.stopLauncher();

// Collect
Pose collectPose = new Pose(48, 24, Math.toRadians(90));
robot.follower.followPath(new BezierCurve(scoringPose, 
    new Pose(36, 12, 0), collectPose));
waitForPath();

robot.runIntake();
sleep(2000);
robot.stopIntake();
```

### Pattern 2: Actions While Moving
```java
// Start following path
robot.follower.followPath(myPath);

// Run intake while driving
robot.runIntake();

// Wait for path to complete
while (opModeIsActive() && robot.follower.isBusy()) {
    robot.updateFollower();
}

robot.stopIntake();
```

### Pattern 3: Path with Heading Control
```java
// Robot will face specific direction at end
Path pathWithHeading = new BezierLine(
    new Pose(0, 0, Math.toRadians(0)),      // Start facing right
    new Pose(24, 24, Math.toRadians(90))    // End facing forward
);
```

## Helper Methods in HardwareConfig

### Initialization
```java
robot.init(hardwareMap);              // Without Pedro Pathing
robot.init(hardwareMap, true);        // With Pedro Pathing
robot.setStartingPose(new Pose(x, y, heading));
```

### Pedro Pathing
```java
robot.updateFollower();               // Update position (call in loop)
robot.getPose();                      // Get current position
robot.follower.followPath(path);     // Start following a path
robot.follower.isBusy();             // Check if path is complete
```

### Drive Control
```java
robot.drive(forward, strafe, rotate);              // Robot-relative
robot.driveFieldRelative(forward, strafe, rotate); // Field-relative
robot.stopAllMotors();
```

### Mechanisms
```java
robot.runIntake();
robot.reverseIntake();
robot.stopIntake();
robot.runLauncher();
robot.stopLauncher();
```

### Sensors
```java
robot.getHeading();           // Degrees
robot.getHeadingRadians();    // Radians
robot.resetYaw();             // Reset IMU
```

## Troubleshooting

### Robot doesn't follow path accurately
1. Run "Localization Test" tuning OpMode
2. Check OTOS sensor mounting and offset in Constants.java
3. Verify motor directions are correct
4. Tune follower constants if needed

### OTOS sensor not found
1. Check I2C connection to Control Hub
2. Verify device name is "otos" in Robot Configuration
3. Ensure SparkFun OTOS library is included in your project

### Robot drives in wrong direction
1. Check motor directions in HardwareConfig.java
2. Verify OTOS offset and orientation in Constants.java
3. Test with Localization Test OpMode

### Position tracking drifts
1. Calibrate OTOS sensor (run tuning OpModes)
2. Check for wheel slippage
3. Verify OTOS sensor is mounted securely

## Advanced Usage

### Custom Path Constraints
In `Constants.java`, adjust path following behavior:
```java
public static PathConstraints pathConstraints = 
    new PathConstraints(
        0.99,  // Max velocity
        100,   // Max acceleration
        1,     // Max angular velocity
        1      // Max angular acceleration
    );
```

### Accessing Raw OTOS Data
```java
// Get OTOS sensor directly
SparkFunOTOS.Pose2D otosPose = robot.otos.getPosition();
telemetry.addData("OTOS X", otosPose.x);
telemetry.addData("OTOS Y", otosPose.y);
```

## Best Practices

1. **Always call `updateFollower()`** in your loop when using Pedro Pathing
2. **Set starting pose** at the beginning of autonomous
3. **Use field coordinates** consistently (measure from a corner)
4. **Test paths incrementally** - start with simple straight lines
5. **Keep mechanisms separate** from path following for clarity
6. **Use telemetry** to debug position and heading

## Next Steps

1. ✅ Configure hardware on Control Hub with device name "otos"
2. ✅ Run "Tuning" → "Localization Test" to verify OTOS works
3. ✅ Create simple autonomous with straight path
4. ✅ Add curved paths and test
5. ✅ Integrate mechanism control (intake/launcher)
6. ✅ Fine-tune path constraints for smooth movement

## Resources

- Pedro Pathing Documentation: https://pedropathing.com/docs/pathing
- SparkFun OTOS: https://pedropathing.com/docs/localization
- FTC Discord: Ask questions in the programming channel

