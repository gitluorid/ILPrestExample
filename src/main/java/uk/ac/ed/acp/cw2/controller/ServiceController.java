package uk.ac.ed.acp.cw2.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.dto.DistanceRequest;
import uk.ac.ed.acp.cw2.dto.NextPositionRequest;
import uk.ac.ed.acp.cw2.dto.Position;
import uk.ac.ed.acp.cw2.dto.RegionRequest;
import uk.ac.ed.acp.cw2.service.PositionService;

import java.net.URL;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ServiceController {

    // Service that handles all position-related calculations and validations
    private final PositionService positionService;

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Value("${ilp.service.url}")
    public URL serviceUrl;

    @GetMapping("/")
    public String index() {
        return "<html><body>" +
                "<h1>Welcome from ILP</h1>" +
                "<h4>ILP-REST-Service-URL:</h4> <a href=\"" + serviceUrl + "\" target=\"_blank\"> " + serviceUrl+ " </a>" +
                "</body></html>";
    }

    @GetMapping("/uid")
    public String uid() {
        return "s2550230";
    }

    /**
     * Helper method to check if a request is invalid.
     * @param requestName the name of the request
     * @param errorMsg the error message if invalid
     * @return true if invalid, false otherwise
     */
    private boolean isInvalidRequest(String requestName, String errorMsg) {
        if (errorMsg != null) {
            logger.warn("Invalid {} request: {}", requestName, errorMsg);
            return true; }
        return false; // If no error message, the request is valid
    }

    /**
     * POST endpoint to calculate the Euclidean distance between two positions.
     * @param distanceRequest a DistanceRequest containing position1 and position2
     * @return 200 OK with the distance if valid, or 400 Bad Request if input is invalid
     */
    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(@RequestBody DistanceRequest distanceRequest) {
        String errorMsg = positionService.validateDistance(distanceRequest);
        if (isInvalidRequest("distanceTo", errorMsg)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(positionService.calculateDistance(distanceRequest));
    }

    /**
     * POST endpoint to check if two positions are close to each other within a threshold.
     * @param distanceRequest a DistanceRequest containing position1 and position2
     * @return 200 OK with true/false if valid, or 400 Bad Request if input is invalid
     */
    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody DistanceRequest distanceRequest) {
        String errorMsg = positionService.validateDistance(distanceRequest);
        if (isInvalidRequest("isCloseTo", errorMsg)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(positionService.isCloseTo(distanceRequest, 0.00015));
    }

    /**
     * POST endpoint to calculate the next drone position from a start position and angle.
     * @param positionRequest a NextPositionRequest containing the start position and angle
     * @return 200 OK with the next Position if valid, or 400 Bad Request if input is invalid
     */
    @PostMapping("/nextPosition")
    public ResponseEntity<Position> nextPosition(@RequestBody NextPositionRequest positionRequest) {
        String errorMsg = positionService.validateNextPosition(positionRequest);
        if (isInvalidRequest("nextPosition", errorMsg)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(positionService.calculateNextPosition(positionRequest));
    }

    /**
     * POST endpoint to check whether a position is inside a specified region.
     * @param regionRequest a RegionRequest containing the position and region vertices
     * @return 200 OK with true/false if valid, or 400 Bad Request if input is invalid
     */
    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody RegionRequest regionRequest) {
        String errorMsg = positionService.validateRegion(regionRequest);
        if (isInvalidRequest("isInRegion", errorMsg)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(positionService.isInRegion(regionRequest));
    }
}
