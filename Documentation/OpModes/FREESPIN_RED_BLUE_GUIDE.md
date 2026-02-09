# FreeSpinRed & FreeSpinBlue Implementation Guide

## Overview

Your robot now has **two alliance-specific OpModes** with intelligent, location-aware flywheel control and automatic target-facing capabilities.

### Files Created:
- **FreeSpinRed.java** - Red alliance OpMode (targets 131.5, 134.5)
- **FreeSpinBlue.java** - Blue alliance OpMode (targets 12.5, 134.5)

### File Removed:
- **FreeSpin.java** - Replaced by alliance-specific versions

## Key Features

### 1. Right Bumper Behavior (CHANGED)
**Before:** Reversed both flywheel and intake  
**Now:** Reverses intake ONLY
```java
// Right Bumper - Reverse INTAKE ONLY
if (gamepad1.right_bumper) {
    intake.setPower(-1);    // Reverse intake only
}
```

### 2. Automatic Target-Facing
When the robot is in either shooting zone, it automatically faces the target:

**FreeSpinRed:** Faces coordinate (131.5, 134.5)  
**FreeSpinBlue:** Faces coordinate (12.5, 134.5)

**How it works:**
1. Calculates the vector from robot to target
2. Computes desired heading angle
3. Applies proportional control to rotate robot
4. Overrides manual rotation stick input while in zones

```java
if (shootingZones.isInFrontShootArea() || shootingZones.isInBackShootArea()) {
    // Calculate desired heading
    double deltaX = TARGET_X - robotX;
    double deltaY = TARGET_Y - robotY;
    double desiredHeading = Math.atan2(deltaY, deltaX);
    
    // Apply rotation control
    double headingError = desiredHeading - currentHeading;
    double rotationPower = Math.max(-1.0, Math.min(1.0, headingError * 0.5));
    rx = rotationPower;
}
```

### 3. Flywheel Control (UNCHANGED)
- **Front Shoot Area:** 50% power
- **Back Shoot Area:** 100% power
- **Outside zones:** OFF
- **D-Pad Down:** Force 100% power

## Usage

### Deploy FreeSpinRed
1. Use for RED alliance matches
2. Robot automatically faces the RED goal (131.5, 134.5)
3. Flywheel spins at appropriate power based on zone

### Deploy FreeSpinBlue
1. Use for BLUE alliance matches
2. Robot automatically faces the BLUE goal (12.5, 134.5)
3. Flywheel spins at appropriate power based on zone

## Telemetry Output

Both OpModes display:
```
Alliance: RED (or BLUE)
Target Heading: (X, Y)
Robot X: 50.5
Robot Y: 100.2
Robot Heading (rad): 1.57
In Front Shoot Area: false
In Back Shoot Area: true
Flywheel Power: 1.0
Intake Power: 0.0
```

## Configuration

To change target coordinates, edit these lines:

**FreeSpinRed.java:**
```java
private static final double TARGET_X = 131.5;
private static final double TARGET_Y = 134.5;
```

**FreeSpinBlue.java:**
```java
private static final double TARGET_X = 12.5;
private static final double TARGET_Y = 134.5;
```

## Rotation Control Tuning

The proportional gain is currently `0.5`. To adjust rotation speed:

```java
double rotationPower = Math.max(-1.0, Math.min(1.0, headingError * 0.5));
                                                            // ^^^^
                                                       Change this value
```

- **Higher values** (e.g., 0.7) = faster rotation
- **Lower values** (e.g., 0.3) = slower, more controlled rotation
- Values > 1.0 will be clamped to 1.0

## Button Mappings

| Button | Action |
|--------|--------|
| Left Stick | Drive (forward/back, strafe) |
| Right Stick X | Manual rotation (overridden in zones) |
| Left Bumper | Auto-align with HuskyLens target |
| Right Bumper | Reverse intake only |
| Right Trigger | Spin intake forward |
| D-Pad Down | Force flywheel to 100% |
| D-Pad Left/Right | Gate servo (disabled) |

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Robot doesn't face target | Check follower is tracking correctly; verify target coordinates |
| Rotation too slow | Increase the proportional gain (0.5) to 0.7 |
| Rotation too fast | Decrease the proportional gain (0.5) to 0.3 |
| Heading keeps oscillating | Reduce proportional gain to 0.4 |
| Flywheel won't spin | Verify zone detection working; check telemetry |

## Next Steps

1. Test both OpModes on your field
2. Verify target-facing accuracy
3. Adjust proportional gain if needed
4. Fine-tune zone boundaries in CoordinateTriangle.java
5. Adjust SHORT_SHOT_SCALE if needed

---

**Ready to deploy! Choose FreeSpinRed or FreeSpinBlue based on your alliance.** 🎯
