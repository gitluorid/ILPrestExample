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
    private boolean isInvalidPosition(Position pos) {
        return pos == null || pos.lng() == null || pos.lat() == null; // Invalid if any of the attributes are null
    }

    /**
     * Checks if a DistanceRequest is invalid.
     * A DistanceRequest is invalid if either position1 or position2 is null or invalid.
     * @param distanceRequest the DistanceRequest to validate
     * @return true if invalid, false otherwise
     */
    public boolean isInvalidDistance(DistanceRequest distanceRequest) {
        if (isInvalidPosition(distanceRequest.position1())) return true;
        return isInvalidPosition(distanceRequest.position2());
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
     * @return true if invalid, false otherwise
     */
    public boolean isInvalidNextPosition(NextPositionRequest positionRequest) {
        if (positionRequest == null || isInvalidPosition(positionRequest.start())) return true;
        if (positionRequest.angle() == null) {return true;}
        double angle = positionRequest.angle();
        if (angle < 0 || angle >= 360) {return true;}
        return angle % 22.5 != 0;
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
        double endLng = positionRequest.start().lng() + STEP_SIZE * Math.sin(radians); // sin(angle) adjusts longitude
        double endLat = positionRequest.start().lat() + STEP_SIZE * Math.cos(radians); // cos(angle) adjusts latitude
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
     * @return true if the region request is invalid, false otherwise
     */
    public boolean isInvalidRegion(RegionRequest regionRequest) {
        if (regionRequest == null || isInvalidPosition(regionRequest.position())) return true;
        if (regionRequest.region() == null || regionRequest.region().name() == null) return true;
        if (regionRequest.region().vertices() == null || regionRequest.region().vertices().isEmpty()) return true;
        List<Position> vertices = regionRequest.region().vertices();
        for (Position vertex : vertices) {if (isInvalidPosition(vertex)) return true;} // Invalid if any vertex is invalid
        // Polygon must be closed: last == first
        if (vertices.size() < 4) return true; // at least 3 + closing vertex
        Position firstVertex = vertices.getFirst(), lastVertex = vertices.getLast();
        return !firstVertex.lng().equals(lastVertex.lng()) || !firstVertex.lat().equals(lastVertex.lat());
    }

    public boolean isInRegion(RegionRequest rReq) {
        // the rReq.vertices() is a list of vertices to make a region
        // the rReq.position() is the position to check if it is in the region
        // the rReq.region().name() is the name of the region
        return false; // TODO implement algorithm.
    }
}
