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
- Position: (59, 92) with 45° heading
- Requires robot to reach position and stop before gate opens

#### D-Pad Down → Move to Back Shoot Position
- Sets target RPM to 6000 (BACK_SHOOT_RPM)
- Uses PedroPathing to autonomously navigate to `Poses.backScorePose`
- Position: (70, 20) with 120° heading
- Requires robot to reach position and stop before gate opens

#### Left Trigger → Stationary Test Shooting Mode
- **Press & Hold Left Trigger** (value > 0.1):
  - Activates test shooting mode immediately
  - Sets target RPM to 6000 (BACK_SHOOT_RPM) for testing
  - **Does NOT require robot to move** - works from any position
  - Perfect for bench testing and tuning without navigation
  
- **Release Left Trigger**:
  - Deactivates test mode
  - Stops flywheel and closes gate

### 4. Gate Opening Validation System

The gate will ONLY open when **BOTH conditions are satisfied**. There are two operating modes:

#### Mode 1: Normal Shooting Mode (D-Pad Navigation)

**Condition 1: Flywheel Speed Validation**
```java
private boolean isFlywheelAtTargetSpeed() {
    if (targetFlywheelRPM <= 0) return false;
    
    double currentRPM = getFlywheelRPM();
    double difference = Math.abs(currentRPM - targetFlywheelRPM);
    
    return difference <= RPM_TOLERANCE;  // Default tolerance: 200 RPM
}
```

**Condition 2: Robot Position Validation**
```java
boolean normalModeReady = !isMovingToShootPosition &&
                          (currentShootPosition == ShootPosition.FRONT ||
                           currentShootPosition == ShootPosition.BACK);
```

The robot must be:
- Solidly stopped at the target shooting position (within 3 inches)
- Not currently moving to a new position
- At either the front or back shooting position

#### Mode 2: Test Shooting Mode (Left Trigger)

**Condition 1: Flywheel Speed Validation** (Same as above)
- Flywheel must be within 200 RPM of target

**Condition 2: Test Mode Active**
```java
boolean testModeReady = isTestShootingMode;
```

Requirements:
- Left trigger must be pressed and held (value > 0.1)
- **No position requirement** - works from anywhere on the field
- Only needs flywheel at correct speed

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
    BACK,   // At back shooting position
    TEST    // In test shooting mode
}
```

### State Variables:
- `targetFlywheelRPM`: Current target flywheel speed
- `isMovingToShootPosition`: Movement status flag (false = robot stopped)
- `currentShootPosition`: Current or target shooting position
- `isTestShootingMode`: Test mode active flag (set by left trigger)

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

## Test Mode Implementation (Left Trigger)

### Code Location
- **Lines 254-273** in `Decode_TeleOp.java`
- Method: `handleTestShootingMode()`

### How It Works
```java
private void handleTestShootingMode() {
    // Check if left trigger is pressed (> 0.1 threshold)
    if (gamepad1.left_trigger > 0.1) {
        // Enable test shooting mode
        if (!isTestShootingMode) {
            isTestShootingMode = true;
            currentShootPosition = ShootPosition.TEST;
            targetFlywheelRPM = BACK_SHOOT_RPM;  // 6000 RPM
            isMovingToShootPosition = false;     // No movement needed
        }
    } else {
        // Disable test shooting mode when trigger is released
        if (isTestShootingMode) {
            isTestShootingMode = false;
            currentShootPosition = ShootPosition.NONE;
            targetFlywheelRPM = 0;  // Stop flywheel
        }
    }
}
```

### Test Mode Features
- **Trigger Threshold**: 0.1 (sensitivity to prevent accidental activation)
- **Default RPM**: 6000 (BACK_SHOOT_RPM) for testing
- **Position Independence**: Works from any location on the field
- **Auto-Reset**: Automatically stops when trigger is released
- **State Tracking**: Prevents duplicate activation with `!isTestShootingMode` check

## D-Pad Release Behavior (Gate Closing)

### How Gate Closes on D-Pad Release

When D-Pad Up or D-Pad Down is released, the system automatically resets the shooting state:

```java
private void handleShootingPositionInput() {
    // Move to front shoot position when D-Pad Up is pressed
    if (gamepad1.dpad_up) {
        moveToFrontShootPosition();
    } else if (previousDpadUp && !gamepad1.dpad_up) {
        // D-Pad Up was released - reset shooting state to allow gate to close
        resetShootingState();
    }

    // Move to back shoot position when D-Pad Down is pressed
    if (gamepad1.dpad_down) {
        moveToBackShootPosition();
    } else if (previousDpadDown && !gamepad1.dpad_down) {
        // D-Pad Down was released - reset shooting state to allow gate to close
        resetShootingState();
    }

    // Update previous D-Pad state for next iteration
    previousDpadUp = gamepad1.dpad_up;
    previousDpadDown = gamepad1.dpad_down;
}

private void resetShootingState() {
    // Reset all shooting position variables to close gate and stop flywheel
    isMovingToShootPosition = false;
    currentShootPosition = ShootPosition.NONE;
    targetFlywheelRPM = 0;
}
```

### State Tracking Variables
- `previousDpadUp`: Tracks previous frame's D-Pad Up state
- `previousDpadDown`: Tracks previous frame's D-Pad Down state

### Gate Closure Flow
1. **D-Pad button is released** → `previousDpadX && !gamepad1.dpad_X` evaluates to true
2. **resetShootingState() is called** → Sets `targetFlywheelRPM = 0`
## Usage Flow

### Normal Shooting Mode (D-Pad Navigation)
1. **Press D-Pad Up or Down** → Robot navigates to target position
2. **Flywheel accelerates** → Motor ramps up to target RPM
3. **Robot stops** → Position arrival confirmed
4. **Gate opens automatically** → Ball can now be shot through flywheel
5. **Release D-Pad button** → `resetShootingState()` is called
6. **Flywheel stops & Gate closes to 180°** → Ball loading stops, gate returns to closed position

### Test Shooting Mode (Left Trigger)
1. **Press & Hold Left Trigger** → Test mode activates immediately
2. **Flywheel accelerates** → Motor ramps up to 6000 RPM
3. **Robot stays in place** → No navigation required
4. **Gate opens automatically** → When flywheel at speed
5. **Release Left Trigger** → Test mode deactivates
6. **Flywheel stops & Gate closes to 180°** → `targetFlywheelRPM` set to 0, gate closes automatically

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

## Quick Reference: Controller Inputs

| Input | Action | Flywheel RPM | Requires Position Stop | Use Case |
|-------|--------|--------------|------------------------|----------|
| **D-Pad Up** | Navigate to Front Shoot | 3000 | Yes | Score from front during match |
| **D-Pad Down** | Navigate to Back Shoot | 6000 | Yes | Score from back during match |
| **Left Trigger (Hold)** | Test Shooting | 6000 | No | Bench testing & tuning |
| **Right Trigger (Hold)** | Run Intake | N/A | N/A | Pick up balls |

## Shooting Mode Comparison

| Aspect | Normal Mode (D-Pad) | Test Mode (Left Trigger) |
|--------|-------------------|------------------------|
| **Activation** | Press D-Pad Up/Down | Press & hold Left Trigger |
| **Navigation** | Autonomous movement required | Works from any position |
| **Position Check** | Must be within 3 inches of target | No position requirement |
| **Flywheel RPM** | 3000 (Front) or 6000 (Back) | 6000 (fixed for testing) |
| **Gate Opens When** | Position reached + RPM valid | RPM valid only |
| **Ideal For** | Match gameplay | Testing & diagnostics |
| **Deactivation** | Automatic after scoring cycle | Release trigger |

