# Quick Reference: Flywheel & Target Control

## How Your Flywheel Now Works

### Automatic Zone-Based Control
Your robot has **location-aware shooting zones** with automatic target-facing:

```
FRONT SHOOT AREA
─────────────────
Triangle vertices:
• (15.5, 127.5)
• (72, 72)
• (128.5, 127.5)

↓ Robot detected inside ↓

Flywheel Power = 0.5 (50% power)
Robot Heading = AUTO-FACES TARGET
💡 Short shot - less power, faces goal


BACK SHOOT AREA
───────────────
Triangle vertices:
• (x6, y6)        ← YOU SET THESE
• (x7, y7)        ← YOU SET THESE
• (x8, y6)        ← YOU SET THESE

↓ Robot detected inside ↓

Flywheel Power = 1.0 (100% power)
Robot Heading = AUTO-FACES TARGET
💡 Full shot - maximum power, faces goal


OUTSIDE ALL ZONES
─────────────────
Robot not in any shooting area

↓ Not in any zone ↓

Flywheel Power = 0.0 (OFF)
Robot Heading = MANUAL CONTROL
🛑 Safety feature - prevents accidental shots
```

## Target Coordinates

### FreeSpinRed
```
Target: (131.5, 134.5)
Alliance: RED
When in zones: Automatically faces RED goal
```

### FreeSpinBlue
```
Target: (12.5, 134.5)
Alliance: BLUE
When in zones: Automatically faces BLUE goal
```

## Code You Can Modify

### 1. Change Shot Power (in FreeSpinRed/FreeSpinBlue)
```java
private static final double SHORT_SHOT_SCALE = 0.5;  // Change this
private static final double FULL_SHOT_SCALE = 1.0;   // Or this
```

### 2. Change Target Coordinates
```java
private static final double TARGET_X = 131.5;  // Or 12.5 for blue
private static final double TARGET_Y = 134.5;
```

### 3. Adjust Rotation Speed
```java
double rotationPower = Math.max(-1.0, Math.min(1.0, headingError * 0.5));
                                                           // ^^^
                                                  0.3 = slow
                                                  0.5 = medium
                                                  0.7 = fast
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
**What it does:** 
- Shoots at 50% power
- Automatically faces target
- For close-range shots

### Back Shoot Area (You configure)
**Your Triangle:**
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
**What it does:** 
- Shoots at 100% power
- Automatically faces target
- For long-range shots

### No Zone (Safety)
**Everywhere else on the field:**
```
Flywheel = OFF (0% power)
Rotation = MANUAL (uses right stick)
```
**Why:** Prevents accidental shots when you're not aiming

## Button Controls

| Button | Action |
|--------|--------|
| **Left Stick** | Drive (up/down, left/right) |
| **Right Stick X** | Rotate (when NOT in zones) |
| **Right Stick X** | Locked to target (when IN zones) |
| **Left Bumper** | Auto-align with HuskyLens |
| **Right Bumper** | Reverse intake ONLY |
| **Right Trigger** | Spin intake forward |
| **D-Pad Down** | Force flywheel 100% |

## Example Scenarios

### Scenario 1: Short Shot (Front Zone)
```
1. Move robot into FRONT SHOOT AREA
   └─ Telemetry: "In Front Shoot Area: true"
2. Flywheel automatically spins at 50%
3. Robot automatically rotates to face target
4. Pull right trigger to intake
5. Shoot when ready
```

### Scenario 2: Long Shot (Back Zone)
```
1. Move robot into BACK SHOOT AREA
   └─ Telemetry: "In Back Shoot Area: true"
2. Flywheel automatically spins at 100%
3. Robot automatically rotates to face target
4. Pull right trigger to intake
5. Shoot with full power
```

### Scenario 3: Outside Zones
```
1. Robot is outside both zones
   └─ Telemetry: Both show "false"
2. Flywheel is OFF (safety)
3. You have manual rotation control
4. Use right stick to rotate freely
```

## Key Takeaway

✨ **Your system is now FULLY AUTOMATIC in shooting zones:**
- Flywheel knows the distance (front vs back)
- Robot automatically faces the goal
- You just need to position and shoot!
- Outside zones? Everything is manual (safe)
