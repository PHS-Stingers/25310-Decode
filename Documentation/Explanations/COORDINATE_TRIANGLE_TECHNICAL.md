# CoordinateTriangle & Target-Facing Implementation - Technical Reference

## Overview

The system implements **constant-time point-in-triangle detection** combined with **proportional heading control** for FTC robot localization and automated target acquisition within bounded game field coordinates (0,0 to 144,144).

## Current Configuration

### Default Triangle Vertices

The CoordinateTriangle class is configured with the following default values:

#### Front Shoot Area Triangle
```java
public double x1 = 15.5, y1 = 127.5;   // Vertex 1: (15.5, 127.5)
public double x2 = 72, y2 = 72;        // Vertex 2: (72, 72)
public double x3 = 128.5;              // Vertex 3: (128.5, 127.5) [uses y1]
```

**Coordinates:** Triangle(A: 15.5,127.5 | B: 72,72 | C: 128.5,127.5)
- **Area:** ~3,075 square units
- **Location:** Upper-center field region
- **Power Scale:** 0.5 (SHORT_SHOT_SCALE)
- **Purpose:** Close-range shooting zone

#### Back Shoot Area Triangle
```java
public double x6 = 51, y6 = 15;        // Vertex 1: (51, 15)
public double x7 = 72, y7 = 23;        // Vertex 2: (72, 23)
public double x8 = 93;                 // Vertex 3: (93, 15) [uses y6]
```

**Coordinates:** Triangle(A: 51,15 | B: 72,23 | C: 93,15)
- **Area:** ~168 square units
- **Location:** Bottom-center field region
- **Power Scale:** 1.0 (FULL_SHOT_SCALE)
- **Purpose:** Long-range shooting zone

## Mathematical Foundation

### Problem Statement

Given:
- Dual triangles defined by vertices for front and back shooting zones
- Current robot pose (x, y, θ) from Pedro Pathing follower
- Target coordinates for goal (T_x, T_y) - alliance-specific
- Constraints: Field bounded to [0,144] × [0,144]

Determine:
1. Is robot within front/back shooting zones? (binary classification)
2. What is the optimal heading to face target? (angle computation)
3. What rotation power should be applied? (proportional control)

### Solution 1: Area-Based Point-in-Triangle Test

**Principle:**
```
If P ∈ Triangle(ABC), then:
  Area(PAB) + Area(PBC) + Area(PCA) = Area(ABC)

If P ∉ Triangle(ABC), then:
  Area(PAB) + Area(PBC) + Area(PCA) > Area(ABC)
```

**Shoelace Formula (Cross Product Method):**
```
Area(x₁, y₁, x₂, y₂, x₃, y₃) = |((x₂ - x₁) × (y₃ - y₁) - (x₃ - x₁) × (y₂ - y₁))| / 2
```

**Time Complexity:** O(1)  
**Space Complexity:** O(1)

### Solution 2: Proportional Heading Control

**Desired Heading Calculation:**
```
Vector to Target: v = (T_x - R_x, T_y - R_y)
Desired Heading: θ_d = atan2(T_y - R_y, T_x - R_x)
```

**Heading Error Normalization:**
```
θ_e = θ_d - θ_c
Normalize to (-π, π]:
  while θ_e > π: θ_e -= 2π
  while θ_e ≤ -π: θ_e += 2π
```

**Proportional Control Law:**
```
u = K_p × θ_e
u ∈ [-1.0, 1.0]  (motor power range)

Where K_p = 0.5 (current proportional gain)
```

## Implementation Details

### Class: CoordinateTriangle

```java
// Front Shoot Area vertices
public double x1 = 15.5, y1 = 127.5;
public double x2 = 72, y2 = 72;
public double x3 = 128.5;

// Back Shoot Area vertices
public double x6 = 0, y6 = 0;
public double x7 = 0, y7 = 0;
public double x8 = 0;

// State flags
public boolean isRobotInFrontShootArea = false;
public boolean isRobotInBackShootArea = false;
```

### Method: checkIfRobotInFrontShootArea(double robotX, double robotY)

**Algorithm:**
```
1. mainArea ← calculateTriangleArea(x₁, y₁, x₂, y₂, x₃, y₁)
2. IF mainArea = 0 THEN return false (degenerate)
3. area₁ ← calculateTriangleArea(robotX, robotY, x₂, y₂, x₃, y₁)
4. area₂ ← calculateTriangleArea(x₁, y₁, robotX, robotY, x₃, y₁)
5. area₃ ← calculateTriangleArea(x₁, y₁, x₂, y₂, robotX, robotY)
6. sumArea ← area₁ + area₂ + area₃
7. isRobotInFrontShootArea ← |sumArea - mainArea| < ε
8. return isRobotInFrontShootArea
```

**Epsilon Tolerance:**
```
ε = 1e-9
Rationale: IEEE 754 double precision ~15-17 significant digits
Cumulative rounding from 4 area calculations requires tolerance buffer
```

### Classes: FreeSpinRed, FreeSpinBlue

Both OpModes implement the following control loop:

```
while (opModeIsActive()) {
    1. Update follower: follower.update()
    2. Query zones: shootingZones.check{Front,Back}ShootArea(follower)
    
    3. IF in Front OR Back zone:
        a. Calculate target heading: θ_d = atan2(ΔY, ΔX)
        b. Calculate error: θ_e = θ_d - θ_c
        c. Normalize: θ_e ∈ (-π, π]
        d. Compute rotation: u = clamp(K_p × θ_e, [-1, 1])
        e. Override rx ← u  (right stick X overridden)
        f. Update drive: drive.drive(y, x, u)
    
    4. Flywheel Control:
        IF isInFrontShootArea: setPower(0.5)
        ELSE IF isInBackShootArea: setPower(1.0)
        ELSE: setPower(0.0)
    
    5. Manual Override:
        IF dpad_down: flywheel.setPower(1.0)
        IF right_bumper: intake.setPower(-1.0)  [CHANGED]
    
    6. Update telemetry and display
}
```

## Key Changes from FreeSpin

| Feature | FreeSpin | FreeSpinRed/Blue |
|---------|----------|------------------|
| Right Bumper | Reversed both flywheel + intake | Reverses intake ONLY |
| Target Facing | None | Auto-rotates to goal when in zones |
| Alliance | Generic | RED (131.5, 134.5) / BLUE (12.5, 134.5) |
| Rotation Control | Manual only | Manual override + auto in zones |
| Telemetry | Basic | Includes heading, alliance, target |

## Proportional Control Tuning

**Current Gain:** K_p = 0.5

**Parameter:** `headingError * 0.5` in drive section

**Effect of K_p:**
- K_p = 0.3: Slow, smooth rotation (under-damped)
- K_p = 0.5: Medium rotation (balanced)
- K_p = 0.7: Fast, aggressive rotation (over-damped)
- K_p > 1.0: Clamped to 1.0 by motor controller

**Stability Analysis:**
The proportional control is stable for K_p ∈ (0, 1] because:
- Error signal θ_e → 0 as heading approaches desired
- Motor power u → 0 as error decreases
- No oscillation with pure proportional control
- May have steady-state error but acceptable for game requirements

## Target Coordinates

### FreeSpinRed
```
Target: (131.5, 134.5)
- Red alliance goal location
- Top-right region of field
- Used to compute desired heading when in zones
```

### FreeSpinBlue
```
Target: (12.5, 134.5)
- Blue alliance goal location
- Top-left region of field
- Used to compute desired heading when in zones
```

## Edge Cases and Limitations

### Zone Detection
- **Degenerate triangles:** Area = 0 returns false (safe behavior)
- **Points on boundary:** Correctly classified as inside
- **Points at vertices:** Classified as inside
- **Floating-point precision:** ε-based comparison handles rounding

### Heading Control
- **Robot at target:** atan2(0, 0) is undefined; returns 0 rad
- **Heading wrapping:** Normalized to (-π, π] to handle angle discontinuity
- **Motor saturation:** Clamped to [-1.0, 1.0] range
- **Convergence:** Asymptotic approach (never exactly 0)

### Field Constraints
- **Out-of-bounds coordinates:** Pedro Pathing should maintain bounds
- **Localization drift:** Requires periodic OTOS recalibration
- **Control frequency:** 1Hz loop (READ_PERIOD = 1s) adequate for TeleOp

## Performance Analysis

**Per-cycle Operations:**

*Zone Detection (O(1)):*
- 2 triangle area calculations = 12 arithmetic ops
- Area comparison + epsilon check = 3 ops
- Total: ~15 operations/zone × 2 zones = 30 ops

*Heading Control (O(1)):*
- Vector computation = 2 subtractions
- atan2 computation = ~50 ops (standard library)
- Angle normalization = ~10 ops
- Clamping = 2 comparisons
- Total: ~65 ops

**Total per-cycle:** ~95-100 floating-point operations  
**Target loop rate:** 1 Hz (READ_PERIOD = 1 second)  
**Actual rate:** ~1000 Hz possible (limited by rate limiter)

## Integration with Pedro Pathing

**Required Follower Methods:**
```
follower.getPose()           → Pose object
pose.getX()                  → double (x-coordinate)
pose.getY()                  → double (y-coordinate)
pose.getHeading()            → double (radians)
```

**Expected Pose Range:**
- X, Y ∈ [0, 144]
- Heading ∈ [-π, π] (normalized)

**Typical Usage:**
```java
follower.update();  // Must be called to update localization
Pose pose = follower.getPose();
double x = pose.getX();
double y = pose.getY();
double heading = pose.getHeading();
```

## Testing Recommendations

### Unit Tests (Point-in-Triangle)
```java
@Test
public void testFrontZoneDetection() {
    CoordinateTriangle tri = new CoordinateTriangle();
    
    // Inside: centroid vicinity
    assertTrue(tri.checkIfRobotInFrontShootArea(72, 100));
    
    // Outside: far corner
    assertFalse(tri.checkIfRobotInFrontShootArea(0, 0));
    
    // Boundary: on vertex
    assertTrue(tri.checkIfRobotInFrontShootArea(15.5, 127.5));
    
    // Boundary: on edge midpoint (15.5+72)/2, (127.5+72)/2 = (43.75, 99.75)
    assertTrue(tri.checkIfRobotInFrontShootArea(43.75, 99.75));
}

@Test
public void testBackZoneConfiguration() {
    CoordinateTriangle tri = new CoordinateTriangle();
    tri.x6 = 50; tri.y6 = 80;
    tri.x7 = 100; tri.y7 = 130;
    tri.x8 = 140;
    
    // Inside configured zone
    assertTrue(tri.checkIfRobotInBackShootArea(90, 100));
    
    // Outside configured zone
    assertFalse(tri.checkIfRobotInBackShootArea(10, 10));
}
```

### Integration Tests (Heading Control)
```java
@Test
public void testHeadingCalculation() {
    // Robot at (50, 100), target at (131.5, 134.5)
    double robotX = 50, robotY = 100;
    double targetX = 131.5, targetY = 134.5;
    
    double deltaX = targetX - robotX;     // 81.5
    double deltaY = targetY - robotY;     // 34.5
    double desiredHeading = Math.atan2(deltaY, deltaX);  // ~0.39 rad
    
    assertTrue(desiredHeading > 0);
    assertTrue(desiredHeading < Math.PI / 2);
}

@Test
public void testHeadingNormalization() {
    // Test angle wrapping
    double angle = 3.5;  // > π
    while (angle > Math.PI) angle -= 2 * Math.PI;  // Should become -0.282
    
    assertTrue(angle > -Math.PI);
    assertTrue(angle <= Math.PI);
}
```

## Future Enhancements

1. **PID Heading Control:** Replace proportional with full PID for faster convergence
2. **Multiple Zones:** Extend to N-sided polygons or arbitrary regions
3. **Adaptive Gain:** Vary K_p based on distance to target
4. **Heading Tolerance:** Allow dead-band around target heading
5. **Listener Pattern:** Callbacks on zone entry/exit
6. **Performance Metrics:** Telemetry for heading error, control effort

## References

- **Computational Geometry:** de Berg et al., "Computational Geometry"
- **Control Theory:** Franklin et al., "Feedback Control of Dynamic Systems"
- **IEEE 754:** IEEE Standard for Floating-Point Arithmetic
- **Pedro Pathing:** https://github.com/pedropathing/Pedro-Pathing
- **FTC Robotics:** https://firstinspires.org/robotics/ftc

## Conclusion

The integrated CoordinateTriangle + Target-Facing system provides:
- ✅ O(1) zone detection with floating-point precision
- ✅ Smooth proportional heading control
- ✅ Alliance-specific goal targeting
- ✅ Safe, automatic aim assistance
- ✅ Manual override for testing
- ✅ Complete telemetry visibility

The design prioritizes robustness, efficiency, and ease of tuning for FTC competition use.
