package uk.ac.ed.acp.cw2.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.dto.DistanceRequest;
import uk.ac.ed.acp.cw2.dto.NextPositionRequest;
import uk.ac.ed.acp.cw2.dto.Position;
import uk.ac.ed.acp.cw2.dto.RegionRequest;

import java.util.List;

/**
 * Service class providing functionality for:
 * - Validating positions, distances, and regions
 * - Calculating distances between positions
 * - Calculating next drone positions
 */
@Service
public class PositionService {
    // Helper method to check if a position is invalid
    private String validatePosition(Position pos) {
        if (pos == null) return "position is missing.";
        if (pos.lng() == null || pos.lat() == null) return "lng or lat is missing.";
        return null;
    }

    /**
     * Checks if a DistanceRequest is invalid.
     * A DistanceRequest is invalid if either position1 or position2 is null or invalid.
     * @param distanceRequest the DistanceRequest to validate
     * @return String: null if valid, or error message if invalid
     */
    public String validateDistance(DistanceRequest distanceRequest) {
        if (distanceRequest == null) return "Distance Request is missing";
        String errorMsg1 = validatePosition(distanceRequest.position1());
        if (errorMsg1 != null) return "position1: " + errorMsg1;
        String errorMsg2 = validatePosition(distanceRequest.position2());
        if (errorMsg2 != null) return "position2: " + errorMsg2;
        return null;
    }

    /**
     * Calculates the Euclidean distance between two positions in degrees.
     * @param distanceRequest: the DistanceRequest containing position1 and position2
     * @return double: the distance between the two positions
     */
    public double calculateDistance(DistanceRequest distanceRequest) {
        double lngDistance = distanceRequest.position1().lng() - distanceRequest.position2().lng(); // The longitude difference
        double latDistance = distanceRequest.position1().lat() - distanceRequest.position2().lat(); // The latitude difference
        return Math.sqrt(lngDistance * lngDistance + latDistance * latDistance);
    }

    /**
     * Checks if the distance between two positions is less than a given threshold.
     * @param distanceRequest the DistanceRequest containing position1 and position2
     * @param threshold the distance threshold
     * @return true if the distance is less than threshold, false otherwise
     */
    public boolean isCloseTo(DistanceRequest distanceRequest, double threshold) {
        double distance = calculateDistance(distanceRequest);
        return distance < threshold;
    }

    /**
     * Checks if a NextPositionRequest is invalid.
     * A request is invalid if:
     * - The request itself or its start position is null
     * - The angle is null, negative, >= 360, or not a multiple of 22.5 degrees
     * @param positionRequest the NextPositionRequest to validate
     * @return String: null if valid, or error message if invalid
     */
    public String validateNextPosition(NextPositionRequest positionRequest) {
        if (positionRequest == null) return "Position Request is missing.";
        String errorMsg = validatePosition(positionRequest.start());
        if (errorMsg != null) return "Start: " + errorMsg;
        if (positionRequest.angle() == null) return "Angle is missing.";
        double angle = positionRequest.angle();
        if (angle < 0 || angle >= 360) return "Angle out of range: " + angle;
        if (angle % 22.5 != 0) return "Angle not multiple of 22.5: " + angle;
        return null;
    }

    /**
     * Calculates the drone's next position from the start position at a given angle.
     * The step size is 0.00015°. Angles are interpreted as:
     * 0° = East, 90° = North, 180° = West, 270° = South.
     * @param positionRequest the NextPositionRequest containing the start position and angle
     * @return the next Position of the drone
     */
    public Position calculateNextPosition(NextPositionRequest positionRequest) {
        final double STEP_SIZE = 0.00015;
        double radians = Math.toRadians(positionRequest.angle());
        double endLng = positionRequest.start().lng() + STEP_SIZE * Math.cos(radians); // cos(angle) adjusts longitude
        double endLat = positionRequest.start().lat() + STEP_SIZE * Math.sin(radians); // sin(angle) adjusts latitude
        return new Position(endLng, endLat);
    }


    /**
     * Checks whether a given RegionRequest is invalid.
     * A RegionRequest is invalid if:
     * - The request itself or its base position is null or invalid
     * - The region or its name is null
     * - The region has no vertices or vertices contain invalid positions
     * - The polygon is not closed (first and last vertices differ)
     * - The polygon has fewer than 4 vertices (needs at least 3 + closing vertex)
     * @param regionRequest the RegionRequest to validate
     * @return String: null if valid, or error message if invalid
     */
    public String validateRegion(RegionRequest regionRequest) {
        if (regionRequest == null) return "Region Request is missing.";
        String errorMsg = validatePosition(regionRequest.position());
        if (errorMsg != null) return "Region Request position: " + errorMsg;
        if (regionRequest.region() == null) return "Region is missing.";
        if (regionRequest.region().name() == null) return "Region name is missing.";
        List<Position> vertices = regionRequest.region().vertices();
        if (vertices == null || vertices.isEmpty()) return "Region vertices missing.";
        for (int i = 0; i < vertices.size(); i++) {
            String posErrorMsg = validatePosition(vertices.get(i));
            if (posErrorMsg != null) return "Vertex " + i + ": " + posErrorMsg;
        }
        if (vertices.size() < 4) return "Too few vertices.";
        Position first = vertices.getFirst(), last = vertices.getLast();
        if (!first.lng().equals(last.lng()) || !first.lat().equals(last.lat())) {return "Polygon not closed.";}
        return null;
    }

    /**
     * Checks if the position is inside (or on the border) of the region from the regionRequest.
     * Uses the Ray-Casting algorithm (even–odd rule):
     * Reference: Adapted from:
     * [1] <a href="https://www.youtube.com/watch?v=RSXM9bgqxJM">Ray-Casting algorithm</a>
     * [2] <a href="https://www.geeksforgeeks.org/cpp/point-in-polygon-in-cpp/">Point in polygon</a>
     * - Cast a horizontal ray to the right from the point.
     * - Count how many times it crosses polygon edges.
     * - Odd count = inside, even count = outside.
     * Special cases:
     * - If the point matches a vertex = inside.
     * - If the point lies exactly on an edge = inside.
     * @param regionRequest the regionRequest containing the region and position to check
     * @return true if position is inside the region, false otherwise
     */
    public boolean isInRegion(RegionRequest regionRequest) {
        Position position = regionRequest.position();
        List<Position> vertices = regionRequest.region().vertices();
        if (vertices.contains(position)) return true; // If the position is a vertex of the region, it is inside.

        int n = vertices.size();
        double x = position.lng(), y = position.lat();
        int count = 0;
        // Loop through all edges of the polygon
        for (int current = 0, previous = n-1; current < n; previous = current++) {
            double currentX = vertices.get(current).lng(), currentY = vertices.get(current).lat();
            double previousX = vertices.get(previous).lng(), previousY = vertices.get(previous).lat();

            // Check if the point lies exactly on an edge using cross product
            // Use a small epsilon (1e-12) to account for floating-point rounding errors
            double cross = (y - currentY) * (previousX - currentX) - (previousY - currentY) * (x - currentX);
            if (Math.abs(cross) < 1e-12 && // nearly collinear
                    // Check if the point is between the vertices
                    x >= Math.min(currentX, previousX) - 1e-12 && x <= Math.max(currentX, previousX) + 1e-12 &&
                    y >= Math.min(currentY, previousY) - 1e-12 && y <= Math.max(currentY, previousY) + 1e-12) {
                return true; // lies exactly on edge
            }

            // Ray-cast
            boolean rayBetweenEdgeY = (y < currentY) != (y < previousY); // Ray is between the edge's y-coords
            // The x-coordinate where the edge intersects the ray
            double xIntersect = currentX + ((y - currentY) / (previousY - currentY)) * (previousX - currentX);
            boolean leftOfIntersect = (x < xIntersect);
            if (rayBetweenEdgeY && leftOfIntersect) count += 1;
        }
        return count % 2 == 1; // If the count is odd, the point is inside the region
    }
}
