# Quick Reference: Flywheel Control Logic

## How Your Flywheel Now Works

### Automatic Zone-Based Control
Your robot now has **location-aware shooting zones** that automatically control the flywheel:

```
FRONT SHOOT AREA
─────────────────
Triangle vertices:
• (15.5, 127.5)
• (72, 72)
• (128.5, 127.5)

↓ Robot detected inside ↓

Flywheel Power = 0.5 (50% power)
💡 Short shot - less power


BACK SHOOT AREA
───────────────
Triangle vertices:
• (x6, y6)        ← YOU SET THESE
• (x7, y7)        ← YOU SET THESE
• (x8, y6)        ← YOU SET THESE

↓ Robot detected inside ↓

Flywheel Power = 1.0 (100% power)
💡 Full shot - maximum power


OUTSIDE ALL ZONES
─────────────────
Robot not in any shooting area

↓ Not in any zone ↓

Flywheel Power = 0.0 (OFF)
🛑 Safety feature - prevents accidental shots
```

## Code You Can Modify

### 1. Change Shot Power (in FreeSpin.java)
```java
private static final double SHORT_SHOT_SCALE = 0.5;  // Change this number
private static final double FULL_SHOT_SCALE = 1.0;   // Change this number
```

Examples:
- `SHORT_SHOT_SCALE = 0.3` → weaker short shots
- `SHORT_SHOT_SCALE = 0.7` → stronger short shots
- `FULL_SHOT_SCALE = 0.9` → slightly weaker full shots

### 2. Configure Back Shoot Area (in CoordinateTriangle.java)
```java
// Change these values to your desired coordinates
public double x6 = 0, y6 = 0;       // First corner
public double x7 = 0, y7 = 0;       // Second corner
public double x8 = 0;               // Third corner X (Y = y6 automatically)
```

## The Three Zones Explained

### Front Shoot Area (Default)
**Current Triangle:**
```
        (15.5, 127.5)
              /\
             /  \
            /    \
           /      \
          /        \
         /          \
    (128.5,127.5) (72,72)
```
**What it does:** Shoots at 50% power when robot is inside
**Why:** Short-distance shots need less power to avoid over-shooting

### Back Shoot Area (You configure)
**Your Triangle (starts at 0,0):**
```
        (x6, y6)
            /\
           /  \
          /    \
         /      \
        /        \
       /          \
   (x8, y6)  (x7, y7)
```
**What it does:** Shoots at 100% power when robot is inside
**Why:** Long-distance shots need full power to reach the target

### No Zone (Safety)
**Everywhere else on the field:**
```
Flywheel = OFF (0% power)
```
**Why:** Prevents accidental shots when you're not ready to shoot

## Example: Configuring Your Back Zone

### On a 144×144 field:

**For a back corner zone:**
```java
public double x6 = 120, y6 = 10;    // Bottom-right point
public double x7 = 130, y7 = 50;    // Another point
public double x8 = 144;              // Far right edge (uses y6=10)
```

**For a back line zone:**
```java
public double x6 = 0, y6 = 134;     // Back-left corner
public double x7 = 72, y7 = 144;    // Back-center
public double x8 = 144;              // Back-right (uses y6=134)
```

## How to Find Good Coordinates

1. **Estimate** where you want the zone
2. **Test** by moving your robot around
3. **Watch telemetry** showing current X, Y position
4. **Note** the coordinates when flywheel should turn on
5. **Update** CoordinateTriangle.java with those values
6. **Rebuild** and redeploy
7. **Repeat** until zones match your desired shooting areas

## Testing Checklist

- [ ] Front area: Robot inside → Flywheel 50% ✓
- [ ] Front area: Robot outside → Flywheel 0% ✓
- [ ] Back area: Robot inside → Flywheel 100% ✓
- [ ] Back area: Robot outside → Flywheel 0% ✓
- [ ] D-Pad Down: Always 100% (override) ✓
- [ ] Right Bumper: Always reverse (override) ✓
- [ ] Intake: Right trigger still works ✓
- [ ] Telemetry: Shows position and zone status ✓

## Emergency Stop

If you need to disable zone detection temporarily:
```java
// In FreeSpin.java, comment out these lines:
// shootingZones.checkIfRobotInFrontShootArea(follower);
// shootingZones.checkIfRobotInBackShootArea(follower);

// And disable the automatic flywheel control section
```

## Key Takeaway

✨ **Your flywheel is now SMART:**
- It knows where you are on the field
- It automatically adjusts power for different zones
- It turns off when you're not ready to shoot
- You can still manually override it anytime
