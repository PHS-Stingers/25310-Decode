package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.follower.Follower;

/**
 * CoordinateTriangle class for detecting if the robot is within defined shooting zones.
 * Uses the area-based point-in-triangle algorithm for O(1) constant-time detection.
 *
 * Two zones are defined:
 * - Front Shoot Area: Closer to the center, uses reduced flywheel power
 * - Back Shoot Area: Further from center, uses full flywheel power
 *
 * The algorithm works by comparing the sum of three sub-triangles formed by the robot position
 * to the main triangle area. If they match (within epsilon tolerance), the robot is inside.
 */
public class CoordinateTriangle {

    // ===== FRONT SHOOT AREA VERTICES =====
    // Triangle formed by three points: (x1, y1), (x2, y2), (x3, y1)
    // Note: x3 uses y1 as its Y-coordinate (shared horizontal line)
    public double x1 = 15.5, y1 = 127.5;      // Top-left vertex
    public double x2 = 72, y2 = 72;            // Bottom vertex (center)
    public double x3 = 128.5;                  // Top-right vertex (uses y1 for Y)

    // ===== BACK SHOOT AREA VERTICES =====
    // Triangle formed by three points: (x6, y6), (x7, y7), (x8, y6)
    // Note: x8 uses y6 as its Y-coordinate (shared horizontal line)
    // Default values of 0 create a degenerate triangle - configure these for your field!
    public double x6 = 51, y6 = 1.5;              // Bottom-left vertex
    public double x7 = 72, y7 = 23;              // Top vertex
    public double x8 = 93;                      // Bottom-right vertex (uses y6 for Y)

    // ===== STATE FLAGS =====
    // These flags are updated by the check methods and can be queried by OpModes
    public boolean isRobotInFrontShootArea = false;
    public boolean isRobotInBackShootArea = false;

    // ===== EPSILON TOLERANCE =====
    // Used for floating-point comparison to handle rounding errors
    // IEEE 754 double precision requires tolerance for cumulative calculations
    private static final double EPSILON = 1e-9;

    /**
     * Calculates the area of a triangle using the Shoelace formula (cross product method).
     * Formula: Area = |((x2 - x1) × (y3 - y1) - (x3 - x1) × (y2 - y1))| / 2
     *
     * @param x1 X-coordinate of first vertex
     * @param y1 Y-coordinate of first vertex
     * @param x2 X-coordinate of second vertex
     * @param y2 Y-coordinate of second vertex
     * @param x3 X-coordinate of third vertex
     * @param y3 Y-coordinate of third vertex
     * @return Absolute area of the triangle
     */
    private double calculateTriangleArea(double x1, double y1, double x2, double y2, double x3, double y3) {
        return Math.abs((x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1)) / 2.0;
    }

    /**
     * Checks if the robot is currently within the Front Shoot Area triangle.
     * Updates the isRobotInFrontShootArea flag.
     *
     * Algorithm:
     * 1. Calculate the main triangle area
     * 2. Calculate three sub-triangles formed by robot position and triangle edges
     * 3. If sum of sub-triangles ≈ main triangle (within epsilon), robot is inside
     *
     * @param follower PedroPathing Follower object to get current robot position
     * @return true if robot is inside the front shoot area, false otherwise
     */
    public boolean checkIfRobotInFrontShootArea(Follower follower) {
        double robotX = follower.getPose().getX();
        double robotY = follower.getPose().getY();

        // Calculate the main triangle area
        double mainArea = calculateTriangleArea(x1, y1, x2, y2, x3, y1);

        // If main area is zero, we have a degenerate triangle (all points on a line)
        if (mainArea < EPSILON) {
            isRobotInFrontShootArea = false;
            return false;
        }

        // Calculate three sub-triangles formed by robot position
        double area1 = calculateTriangleArea(robotX, robotY, x2, y2, x3, y1);
        double area2 = calculateTriangleArea(x1, y1, robotX, robotY, x3, y1);
        double area3 = calculateTriangleArea(x1, y1, x2, y2, robotX, robotY);

        // Sum of sub-triangles
        double sumArea = area1 + area2 + area3;

        // Check if sum matches main area (within epsilon tolerance)
        isRobotInFrontShootArea = Math.abs(sumArea - mainArea) < EPSILON;

        return isRobotInFrontShootArea;
    }

    /**
     * Checks if the robot is currently within the Back Shoot Area triangle.
     * Updates the isRobotInBackShootArea flag.
     *
     * Algorithm:
     * 1. Calculate the main triangle area
     * 2. Calculate three sub-triangles formed by robot position and triangle edges
     * 3. If sum of sub-triangles ≈ main triangle (within epsilon), robot is inside
     *
     * @param follower PedroPathing Follower object to get current robot position
     * @return true if robot is inside the back shoot area, false otherwise
     */
    public boolean checkIfRobotInBackShootArea(Follower follower) {
        double robotX = follower.getPose().getX();
        double robotY = follower.getPose().getY();

        // Calculate the main triangle area
        double mainArea = calculateTriangleArea(x6, y6, x7, y7, x8, y6);

        // If main area is zero, we have a degenerate triangle (all points on a line)
        if (mainArea < EPSILON) {
            isRobotInBackShootArea = false;
            return false;
        }

        // Calculate three sub-triangles formed by robot position
        double area1 = calculateTriangleArea(robotX, robotY, x7, y7, x8, y6);
        double area2 = calculateTriangleArea(x6, y6, robotX, robotY, x8, y6);
        double area3 = calculateTriangleArea(x6, y6, x7, y7, robotX, robotY);

        // Sum of sub-triangles
        double sumArea = area1 + area2 + area3;

        // Check if sum matches main area (within epsilon tolerance)
        isRobotInBackShootArea = Math.abs(sumArea - mainArea) < EPSILON;

        return isRobotInBackShootArea;
    }

    /**
     * Convenience method to check if robot is in the front shoot area.
     * @return Current state of isRobotInFrontShootArea flag
     */
    public boolean isInFrontShootArea() {
        return isRobotInFrontShootArea;
    }

    /**
     * Convenience method to check if robot is in the back shoot area.
     * @return Current state of isRobotInBackShootArea flag
     */
    public boolean isInBackShootArea() {
        return isRobotInBackShootArea;
    }
}

