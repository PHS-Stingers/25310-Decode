# Decode Shooting System Documentation Index

Welcome! This folder contains all documentation for the FTC 25310-Decode shooting system with intelligent zone detection and automatic target facing.

## Quick Navigation

### 📚 Start Here
- **OpModes/FREESPIN_RED_BLUE_GUIDE.md** - How to use FreeSpinRed and FreeSpinBlue OpModes

### 🎯 Operation Guides
- **OpModes/FREESPIN_RED_BLUE_GUIDE.md**
  - How to deploy and use alliance-specific OpModes
  - Button mappings
  - Target coordinates
  - Troubleshooting

### ⚙️ Configuration
- **Configuration/ZONE_COORDINATES_GUIDE.md**
  - How to set back shoot area coordinates
  - 5 visual examples with diagrams
  - Common mistakes and solutions

### 📖 Learning Materials
- **Explanations/COORDINATE_TRIANGLE_BEGINNERS.md** - Simple explanation for anyone
- **Explanations/COORDINATE_TRIANGLE_TECHNICAL.md** - Deep technical reference

### 📋 Quick Reference
- **Reference/QUICK_REFERENCE_GUIDE.md** - Fast lookup for controls and settings

## System Overview

### What It Does

Your robot now has **two alliance-specific OpModes** with automatic zone detection and target-facing:

```
FRONT SHOOT AREA → 50% Power + Auto-faces goal
BACK SHOOT AREA  → 100% Power + Auto-faces goal
OUTSIDE ZONES    → Flywheel OFF + Manual control
```

### Key Features

✨ FreeSpinRed (Red alliance - target 131.5, 134.5)
✨ FreeSpinBlue (Blue alliance - target 12.5, 134.5)
✨ Automatic zone detection with infinite precision
✨ Smooth proportional rotation control
✨ Right Bumper reverses INTAKE ONLY (no flywheel)
✨ D-Pad Down forces 100% flywheel power

## Quick Start

1. Choose your alliance OpMode (FreeSpinRed or FreeSpinBlue)
2. Deploy to robot
3. Start OpMode in FTC Driver Station
4. Move robot into zones to test

## Documentation Organization

```
Documentation/
├── README.md (this file)
├── OpModes/
│   └── FREESPIN_RED_BLUE_GUIDE.md
├── Configuration/
│   └── ZONE_COORDINATES_GUIDE.md
├── Reference/
│   └── QUICK_REFERENCE_GUIDE.md
└── Explanations/
    ├── COORDINATE_TRIANGLE_BEGINNERS.md
    └── COORDINATE_TRIANGLE_TECHNICAL.md
```

## Customization Options

### Change Shot Power
Edit FreeSpinRed.java or FreeSpinBlue.java:
```java
SHORT_SHOT_SCALE = 0.5     // Front zone power (change to 0.3-0.7)
FULL_SHOT_SCALE = 1.0      // Back zone power (change to 0.8-1.0)
```

### Configure Back Shoot Area
Edit CoordinateTriangle.java:
```java
x6 = ???, y6 = ???    // See Configuration guide for help
x7 = ???, y7 = ???
x8 = ???
```

### Adjust Rotation Speed
Edit FreeSpinRed/Blue.java:
```java
headingError * 0.5    // 0.3=slow, 0.5=medium, 0.7=fast
```

## Need Help?

- **How do I use it?** → Read OpModes/FREESPIN_RED_BLUE_GUIDE.md
- **How do I set zones?** → Read Configuration/ZONE_COORDINATES_GUIDE.md
- **How does it work?** → Read Explanations/COORDINATE_TRIANGLE_BEGINNERS.md
- **Want the math?** → Read Explanations/COORDINATE_TRIANGLE_TECHNICAL.md
- **Need a quick lookup?** → Read Reference/QUICK_REFERENCE_GUIDE.md

## Source Code

All implementation files are in `pedroPathing/`:
- **FreeSpinRed.java** - Red alliance OpMode
- **FreeSpinBlue.java** - Blue alliance OpMode
- **CoordinateTriangle.java** - Zone detection engine

---

**Choose your alliance and deploy!** 🚀
