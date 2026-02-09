# Visual Configuration Guide for Back Shoot Area

## Understanding Your Field

The game field is 144 × 144 units, with (0,0) at the bottom-left corner:

```
Y
^
144 ┌─────────────────────────────────┐
    │                                 │
    │  FRONT SHOOT AREA               │
127.5├─────────────────────────────┐   │
    │       (15.5, 127.5)  ▲      │   │
    │         \           /       │   │
    │          \         /        │   │
    │           \       /         │   │
    │            \     /          │   │
    │             \   /           │   │
    │              \ /            │   │
    │               ▼              │   │
 72 │          (72, 72)            │   │
    │           /   \              │   │
    │          /     \             │   │
    │         /       \            │   │
    │        /         \           │   │
    │       /           \          │   │
    │      ▼             ▼         │   │
    │ (128.5, 127.5) ───────────┘  │   │
    │                              │
  0 └─────────────────────────────────┘
    0        72                   144 → X
```

## Configuring Back Shoot Area: 5 Examples

### Example 1: Back Right Corner
**For shooting from the right side of the field, near the back:**
```
144 ┌─────────────────────────────────┐
    │                     x7(130,100) │
    │                       /\        │
    │                      /  \       │
    │                     /    \      │
    │                    /      \     │
    │                   /        \    │
    │                  /          \   │
    │                 /            \  │
    │    x6(120,50) ◄─              │ 
    │              ────────────────── │ x8(144,50)
  0 └─────────────────────────────────┘
    0                            144

CODE TO USE:
x6 = 120, y6 = 50
x7 = 130, y7 = 100
x8 = 144
```

### Example 2: Back Left Corner
**For shooting from the left side, near the back:**
```
144 ┌─────────────────────────────────┐
    │ x7(18,100)                      │
    │    /\                           │
    │   /  \                          │
    │  /    \                         │
    │ /      \                        │
    │/        \                       │
    ├─ x6(0,50)        x8(24,50) ──┤ 
    │                               │
  0 └─────────────────────────────────┘
    0                            144

CODE TO USE:
x6 = 0, y6 = 50
x7 = 18, y7 = 100
x8 = 24
```

### Example 3: Back Center Line
**For shooting straight back, wide target area:**
```
144 ┌─────────────────────────────────┐
    │          x7(72,144)             │
    │             /\                  │
    │            /  \                 │
    │           /    \                │
    │          /      \               │
    │         /        \              │
    │        /          \             │
    │       /            \            │
    │  x6(40,120) ─────── x8(104,120) │
    │                                 │
  0 └─────────────────────────────────┘
    0                            144

CODE TO USE:
x6 = 40, y6 = 120
x7 = 72, y7 = 144
x8 = 104
```

### Example 4: Back Full Width
**For shooting from anywhere at the back:**
```
144 ┌─────────────────────────────────┐
    │  x6(0,130)  x7(72,144)  (144,130) ← x8 │
    │       \       |       /             │
    │        \      |      /              │
    │         \     |     /               │
    │          \    |    /                │
    │           \   |   /                 │
    │            \  |  /                  │
    │             \ | /                   │
    │              \|/                    │
    │              (72,120)               │
    │                                     │
  0 └─────────────────────────────────────┘
    0                              144

CODE TO USE:
x6 = 0, y6 = 130
x7 = 72, y7 = 144
x8 = 144
```

### Example 5: Diagonal Back Zone
**For shooting from back-left to back-right diagonal:**
```
144 ┌─────────────────────────────────┐
    │            x7(130,140)          │
    │              /\                 │
    │             /  \                │
    │            /    \               │
    │           /      \              │
    │          /        \             │
    │         /          \            │
    │    x6(20,80)        \           │
    │         ─────────────────       │
    │                   x8(110,80)    │
    │                                 │
  0 └─────────────────────────────────┘
    0                            144

CODE TO USE:
x6 = 20, y6 = 80
x7 = 130, y7 = 140
x8 = 110
```

## How to Pick Your Values

### Step 1: Identify Your Target Zone
Where on the field do you want to shoot from?
- Back corner? Back center? Back full width?
- Choose one of the examples above or create your own

### Step 2: Visualize the Triangle
Draw an imaginary triangle on your field with 3 corners

### Step 3: Measure or Estimate Coordinates
For each corner, estimate its X and Y position (0-144 range)

### Step 4: Remember the Rules
- **x6, y6** = First corner (bottom-left of your zone typically)
- **x7, y7** = Second corner (top of your zone typically)
- **x8** = Third corner X coordinate (uses y6 for the Y coordinate)

### Step 5: Update Your Code
```java
// In CoordinateTriangle.java
public double x6 = YOUR_X6_VALUE, y6 = YOUR_Y6_VALUE;
public double x7 = YOUR_X7_VALUE, y7 = YOUR_Y7_VALUE;
public double x8 = YOUR_X8_VALUE;
```

### Step 6: Test
Deploy and move robot through zone to verify it works

## Common Mistakes

❌ **Using coordinates > 144**
```java
public double x7 = 200, y7 = 200;  // TOO BIG!
```
Field is only 144×144, so all values should be 0-144

❌ **Forgetting to set values**
```java
public double x6 = 0, y6 = 0;      // Default (0,0)
public double x7 = 0, y7 = 0;      // Still default
public double x8 = 0;               // Triangle at origin!
```
All three points at (0,0) create a degenerate triangle

❌ **Making all three points collinear (in a line)**
```java
x6 = 0,   y6 = 50
x7 = 72,  y7 = 50    // Same Y as x6! (horizontal line)
x8 = 144             // Also Y=50 when using y6
```
Three points must form a triangle, not a line

✅ **Correct Example**
```java
x6 = 50,  y6 = 80    // Corner 1: (50, 80)
x7 = 100, y7 = 130   // Corner 2: (100, 130)
x8 = 140             // Corner 3: (140, 80)
```
Forms a valid triangle with 3 different points

## Testing Your Zone

After updating the code:

1. Deploy to robot
2. Start FreeSpin OpMode
3. Move robot to where you expect zone to be
4. **If flywheel turns on at 100%** → Zone is working! ✅
5. **If flywheel stays off** → Check telemetry for robot position
6. **If zone is too small/large** → Adjust x6, x7, y7 values

## Helpful Telemetry Values

When testing, watch for:
```
Robot X: 50.5        ← Current X position
Robot Y: 100.2       ← Current Y position
In Front Shoot Area: false
In Back Shoot Area: true    ← If this shows true, zone is working!
Flywheel Power: 1.0         ← Should show 1.0 when in back area
```

---

**Pick an example above, update your code, and you're ready to test!**
