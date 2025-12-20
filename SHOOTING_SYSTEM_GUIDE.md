# Ball Shooting System Implementation Guide

## Overview
This document describes the ball shooting authorization system implemented in `Decode_TeleOp.java` for the FTC Decode 2025-2026 season.

## Key Features

### 1. Flywheel RPM Constants
```java
private static final double FRONT_SHOOT_RPM = 3000;   // Front shooting position RPM
private static final double BACK_SHOOT_RPM = 6000;    // Back shooting position RPM
```

### 2. Gate Servo Control
- **Closed Position**: 180° (1.0 normalized)
- **Open Position**: 140° (0.778 normalized)

The gate servo enables the ball to freely move through the flywheel when conditions are met.

### 3. Shooting Position Navigation

#### D-Pad Up → Move to Front Shoot Position
- Sets target RPM to 3000 (FRONT_SHOOT_RPM)
- Uses PedroPathing to autonomously navigate to `Poses.frontScorePose`
- Position: (51, 92) with 135° heading

#### D-Pad Down → Move to Back Shoot Position
- Sets target RPM to 6000 (BACK_SHOOT_RPM)
- Uses PedroPathing to autonomously navigate to `Poses.backScorePose`
- Position: (70, 20) with 120° heading

### 4. Gate Opening Validation System

The gate will ONLY open when BOTH conditions are satisfied:

#### Condition 1: Flywheel Speed Validation
```java
private boolean isFlywheelAtTargetSpeed() {
    if (targetFlywheelRPM <= 0) return false;
    
    double currentRPM = getFlywheelRPM();
    double difference = Math.abs(currentRPM - targetFlywheelRPM);
    
    return difference <= RPM_TOLERANCE;  // Default tolerance: 200 RPM
}
```

#### Condition 2: Robot Position Validation
```java
boolean robotStopped = !isMovingToShootPosition && 
                       (currentShootPosition == ShootPosition.FRONT || 
                        currentShootPosition == ShootPosition.BACK);
```

The robot must be:
- Solidly stopped at the target shooting position (within 3 inches)
- Not currently moving to a new position
- At either the front or back shooting position

### 5. Flywheel Control

The flywheel is controlled using velocity-based commands:
```java
// Convert RPM to ticks per second
double ticksPerSecond = (targetFlywheelRPM * ENCODER_TICKS_PER_REV) / 60.0;
flywheel.setVelocity(ticksPerSecond);
```

**Motor Specifications:**
- Hardware: REV HD Hex Motor
- Encoder Ticks Per Revolution: 28
- Control Mode: RUN_USING_ENCODER

### 6. Configuration Constants

| Constant | Value | Purpose |
|----------|-------|---------|
| FRONT_SHOOT_RPM | 3000 | Flywheel speed at front position |
| BACK_SHOOT_RPM | 6000 | Flywheel speed at back position |
| RPM_TOLERANCE | 200 | Acceptable RPM variance |
| ENCODER_TICKS_PER_REV | 28 | Motor encoder resolution |
| POSITION_TOLERANCE_INCHES | 3.0 | Position arrival tolerance |

## Hardware Mapping

### Required Hardware:
1. **Intake Motor** - hardware name: "intake"
2. **Flywheel Motor** - hardware name: "Output" (DcMotorEx)
3. **Gate Servo** - hardware name: "Gate"
4. **Drive Motors** - standard mecanum drive setup

## State Management

The system uses an enum to track shooting positions:
```java
private enum ShootPosition {
    NONE,   // Not at a shooting position
    FRONT,  // At front shooting position
    BACK    // At back shooting position
}
```

### State Variables:
- `targetFlywheelRPM`: Current target flywheel speed
- `isMovingToShootPosition`: Movement status flag
- `currentShootPosition`: Current or target shooting position

## Telemetry Display

The system provides real-time telemetry including:
- Current shooting position
- Target and actual flywheel RPM
- Gate servo position (0-1)
- Robot X and Y coordinates
- Movement status

## Initialization

The system automatically:
1. Initializes all hardware on OpMode start
2. Sets flywheel to use encoder-based control
3. Enables brake mode on flywheel
4. Positions gate to closed position

## Usage Flow

1. **Press D-Pad Up or Down** → Robot navigates to target position
2. **Flywheel accelerates** → Motor ramps up to target RPM
3. **Robot stops** → Position arrival confirmed
4. **Gate opens automatically** → Ball can now be shot through flywheel
5. **Gate closes** → When not in shooting position or flywheel stops

## Customization

To adjust performance:
- Modify `FRONT_SHOOT_RPM` and `BACK_SHOOT_RPM` constants
- Adjust `RPM_TOLERANCE` for tighter/looser speed control
- Change `POSITION_TOLERANCE_INCHES` for stricter/looser positioning
- Modify gate positions by changing servo degree values

## Safety Features

- ✓ Gate remains closed until conditions are met
- ✓ Flywheel must be at correct speed before gate opens
- ✓ Robot must be completely stopped at a shooting position
- ✓ Encoder-based speed control for consistency
- ✓ Brake mode prevents uncontrolled spinning

## Integration Notes

This code integrates with:
- **MecanumDrive.java** - Drive system
- **Constants.java** - Pose and path definitions
- **PedroPathing** - Autonomous positioning library

