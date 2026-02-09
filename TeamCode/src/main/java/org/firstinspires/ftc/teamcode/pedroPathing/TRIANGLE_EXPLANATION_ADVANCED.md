# CoordinateTriangle Implementation - Technical Documentation

## Overview

The `CoordinateTriangle` class implements a **constant-time point-in-triangle test** for FTC robot localization within bounded game field coordinates (0,0 to 144,144). Rather than iterate through discrete grid coordinates, this implementation uses the **area comparison method** (barycentric variant) to provide O(1) performance regardless of coordinate precision.

## Mathematical Foundation

### Problem Statement

Given:
- A triangle defined by three 2D vertices: P₁(x₁, y₁), P₂(x₂, y₂), P₃(x₃, y₃)
- A query point Q(qx, qy) representing robot position
- Coordinates as double-precision floating-point values

Determine: Is Q contained within the triangle's interior or boundary?

### Solution: Area-Based Point-in-Triangle Test

The algorithm is based on the following principle:

**If a point P is inside triangle ABC, then:**
```
Area(PAB) + Area(PBC) + Area(PCA) = Area(ABC)
```

**If P is outside the triangle:**
```
Area(PAB) + Area(PBC) + Area(PCA) > Area(ABC)
```

This works because:
1. For any point inside the triangle, the three sub-triangles perfectly partition the main triangle
2. For any point outside, the sum of areas exceeds the main triangle's area
3. Floating-point comparison uses epsilon tolerance to handle rounding errors

### Area Calculation Formula

The implementation uses the **shoelace formula** (cross product method):

```
Area(x₁, y₁, x₂, y₂, x₃, y₃) = |((x₂ - x₁) × (y₃ - y₁) - (x₃ - x₁) × (y₂ - y₁))| / 2
```

**Derivation:**
```
Vector AB = (x₂ - x₁, y₂ - y₁)
Vector AC = (x₃ - x₁, y₃ - y₁)

Cross Product Magnitude = |(x₂ - x₁) × (y₃ - y₁) - (x₃ - x₁) × (y₂ - y₁)|
Area = |Cross Product| / 2
```

This is computationally efficient and requires only 6 multiplications, 4 subtractions, and 1 division per area calculation.

## Implementation Details

### Class Structure

```java
public class CoordinateTriangle {
    public double x1 = 15.5, y1 = 127.5;
    public double x2 = 72, y2 = 72;
    public double x3 = 128.5;
    public boolean isRobotInTriangle = false;
    
    // ... methods
}
```

**Design Considerations:**
- Public members for configuration flexibility (modifying vertices at runtime)
- Boolean flag maintains state for asynchronous telemetry/logging
- Field scope selected for accessibility in FTC opmode contexts

### Core Algorithm: `checkIfRobotInTriangle(double robotX, double robotY)`

```
1. mainArea ← calculateTriangleArea(x₁, y₁, x₂, y₂, x₃, y₁)
2. IF mainArea = 0 THEN return degenerate triangle (OUTSIDE)
3. area₁ ← calculateTriangleArea(robotX, robotY, x₂, y₂, x₃, y₁)
4. area₂ ← calculateTriangleArea(x₁, y₁, robotX, robotY, x₃, y₁)
5. area₃ ← calculateTriangleArea(x₁, y₁, x₂, y₂, robotX, robotY)
6. sumArea ← area₁ + area₂ + area₃
7. isRobotInTriangle ← |sumArea - mainArea| < ε
8. return isRobotInTriangle
```

**Time Complexity:** O(1)  
**Space Complexity:** O(1)

### Floating-Point Precision Handling

```java
double epsilon = 1e-9;
isRobotInTriangle = Math.abs(sumArea - mainArea) < epsilon;
```

**Rationale:**
- IEEE 754 double precision provides ~15-17 significant decimal digits
- Cumulative rounding errors from 4 area calculations can accumulate
- ε = 10⁻⁹ represents ~9 decimal places of precision
- Provides robustness against rounding errors while maintaining geometric accuracy

**Alternative Approaches Considered:**
- Relative epsilon: `Math.abs(sumArea - mainArea) / mainArea < epsilon` (preferred for extreme coordinates)
- Ulp-based comparison: `Math.abs(sumArea - mainArea) < Math.ulp(mainArea) * K`

## Method Signatures

### Primary Method
```java
public boolean checkIfRobotInTriangle(double robotX, double robotY)
```
- **Parameters:** Robot's X and Y coordinates (doubles)
- **Returns:** Boolean indicating inclusion
- **Side Effects:** Updates `isRobotInTriangle` member

### Overloaded Method (Pedro Pathing Integration)
```java
public boolean checkIfRobotInTriangle(Follower follower)
```
- **Parameters:** Pedro Pathing Follower instance
- **Returns:** Boolean indicating inclusion
- **Behavior:** Extracts pose via `follower.getPose()`, delegates to primary method
- **Exception Handling:** Potential NPE if follower is null (not guarded)

### Helper Method
```java
private double calculateTriangleArea(double x1, double y1, double x2, double y2, double x3, double y3)
```
- **Parameters:** Coordinates of three triangle vertices
- **Returns:** Non-negative area as double
- **Implementation:** Shoelace formula with absolute value
- **Visibility:** Private (internal use only)

### Utility Methods
```java
public boolean isInTriangle()
public void printTriangleInfo()
```

## Edge Cases and Limitations

### Degenerate Triangles
```java
if (mainArea == 0) {
    isRobotInTriangle = false;
    return false;
}
```
A triangle with zero area (collinear vertices) is invalid. The algorithm correctly returns false for any query point.

### Boundary Conditions
- Points **exactly on edges** are correctly classified as inside (due to area equivalence)
- Points **exactly at vertices** are classified as inside
- These behaviors are mathematically consistent with the area method

### Floating-Point Limitations
- Extreme coordinates near Double.MAX_VALUE may cause overflow in intermediate calculations
- For FTC use (0-144 range), this is not a concern
- If extending to arbitrary precision, consider BigDecimal alternative

### Thread Safety
The class is **not thread-safe** due to shared mutable state (`isRobotInTriangle`). For concurrent access:
- Use synchronized accessors
- Or return values directly instead of storing state

## Performance Analysis

**Per-call operations:**
- 4 area calculations (4 × 6 multiplications, 4 × 4 subtractions, 4 × 1 division)
- 3 additions + 1 subtraction for area sum
- 1 absolute value + 1 comparison

**Total:** ~50 floating-point operations, O(1) **guaranteed**

**Comparison to alternatives:**
- **Ray casting:** O(N) where N = triangle vertices (here N=3, but scales poorly for polygons)
- **Barycentric coordinates:** Similar O(1), slightly higher memory footprint
- **Grid-based:** O(field_resolution²), infeasible for continuous coordinates

## Integration with Pedro Pathing

The Follower integration assumes:
```java
Pose robotPose = follower.getPose();
return checkIfRobotInTriangle(robotPose.getX(), robotPose.getY());
```

**Requirements:**
- Follower must be initialized and tracking
- Pose must return valid (non-NaN, non-infinite) coordinates
- Robot's localization must be reasonably accurate

**Typical Usage Context:**
```java
public class Decode_TeleOp extends LinearOpMode {
    private CoordinateTriangle shootingZone;
    private Follower follower;
    
    @Override
    public void runOpMode() throws InterruptedException {
        follower = createFollower(hardwareMap);
        shootingZone = new CoordinateTriangle();
        // Triangle vertices automatically set to predefined values
        
        waitForStart();
        while (opModeIsActive()) {
            shootingZone.checkIfRobotInTriangle(follower);
            if (shootingZone.isInTriangle()) {
                // Execute shooting sequence
            }
            follower.update();
        }
    }
}
```

## Testing Recommendations

### Unit Test Cases

```java
@Test
public void testRobotInside() {
    CoordinateTriangle tri = new CoordinateTriangle();
    assertTrue(tri.checkIfRobotInTriangle(72, 100)); // Centroid vicinity
}

@Test
public void testRobotOutside() {
    CoordinateTriangle tri = new CoordinateTriangle();
    assertFalse(tri.checkIfRobotInTriangle(0, 0)); // Well outside
}

@Test
public void testRobotOnVertex() {
    CoordinateTriangle tri = new CoordinateTriangle();
    assertTrue(tri.checkIfRobotInTriangle(15.5, 127.5)); // On P1
}

@Test
public void testRobotOnEdge() {
    CoordinateTriangle tri = new CoordinateTriangle();
    // Midpoint of P1-P2: ((15.5+72)/2, (127.5+72)/2) = (43.75, 99.75)
    assertTrue(tri.checkIfRobotInTriangle(43.75, 99.75));
}
```

### Integration Testing
- Verify against known coordinate sequences
- Compare with visual field overlay
- Monitor epsilon sensitivity

## Future Enhancements

1. **Null Safety:** Guard against null Follower in overloaded method
2. **Relative Epsilon:** Use scaled epsilon for extreme coordinate ranges
3. **Multiple Regions:** Create `CoordinatePolygon` for N-sided regions
4. **Listener Pattern:** Implement `OnZoneChangeListener` for state transitions
5. **Performance Metrics:** Add instrumentation for telemetry

## Mathematical Proofs

### Proof of Correctness

**Theorem:** For point P and triangle ABC, P ∈ Triangle if and only if:
```
Area(PAB) + Area(PBC) + Area(PCA) = Area(ABC)
```

**Proof Sketch:**
- **Forward (P inside ⟹ areas sum):** If P is inside, the three sub-triangles partition ABC without overlap or gap
- **Reverse (areas sum ⟹ P inside):** If areas sum, P cannot be outside (would create excess area)

**Geometric Intuition:** The three sub-triangles "tile" the main triangle if and only if the query point is interior.

## References

- **Point in Polygon Algorithms:** Weiler-Atherton, ray casting, barycentric methods
- **Computational Geometry:** de Berg, van Kreveld, Overmars, Schwarzkopf
- **IEEE 754 Floating-Point:** Standard for binary floating-point arithmetic
- **FTC Pedro Pathing:** https://github.com/pedropathing/Pedro-Pathing

## Conclusion

The `CoordinateTriangle` implementation provides a robust, efficient, and mathematically sound solution for point-in-triangle queries in FTC robot localization. The area-based method elegantly avoids discrete grid iteration while maintaining O(1) performance and handling arbitrary floating-point precision without specialized data structures.
