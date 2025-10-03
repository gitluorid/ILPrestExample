package uk.ac.ed.acp.cw2.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.dto.DistanceRequest;
import uk.ac.ed.acp.cw2.dto.NextPositionRequest;
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

    private final PositionService positionService;

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Value("${ilp.service.url}")
    public URL serviceUrl;

    /**
     * GET endpoint for the index page.
     * @return a simple HTML page with a welcome message and the service URL
     */
    @GetMapping("/")
    public String index() {
        return "<html><body>" +
                "<h1>Welcome from ILP</h1>" +
                "<h4>ILP-REST-Service-URL:</h4> <a href=\"" + serviceUrl + "\" target=\"_blank\"> " + serviceUrl+ " </a>" +
                "</body></html>";
    }

    /**
     * GET endpoint that returns the student UUID.
     * @return a static student ID string
     */
    @GetMapping("/uid")
    public String uid() {
        return "s2550230";
    }

    /**
     * POST endpoint to calculate the Euclidean distance between two positions.
     * @param distanceRequest a DistanceRequest containing position1 and position2
     * @return 200 OK with the distance if valid, or 400 Bad Request if input is invalid
     */
    @PostMapping("/distanceTo")
    public ResponseEntity<?> distanceTo(@RequestBody DistanceRequest distanceRequest) {
        if (positionService.isInvalidDistance(distanceRequest)) {return ResponseEntity.badRequest().build();}
        return ResponseEntity.ok().body(positionService.calculateDistance(distanceRequest));
    }

    /**
     * POST endpoint to check if two positions are close to each other within a threshold.
     * @param distanceRequest a DistanceRequest containing position1 and position2
     * @return 200 OK with true/false if valid, or 400 Bad Request if input is invalid
     */
    @PostMapping("/isCloseTo")
    public ResponseEntity<?> isCloseTo(@RequestBody DistanceRequest distanceRequest) {
        if (positionService.isInvalidDistance(distanceRequest)) {return ResponseEntity.badRequest().build();}
        return ResponseEntity.ok().body(positionService.isCloseTo(distanceRequest,0.00015));
    }

    /**
     * POST endpoint to calculate the next drone position from a start position and angle.
     * @param positionRequest a NextPositionRequest containing the start position and angle
     * @return 200 OK with the next Position if valid, or 400 Bad Request if input is invalid
     */
    @PostMapping("/nextPosition")
    public ResponseEntity<?> nextPosition(@RequestBody NextPositionRequest positionRequest) {
        if (positionService.isInvalidNextPosition(positionRequest)) {return ResponseEntity.badRequest().build();}
        return ResponseEntity.ok().body(positionService.calculateNextPosition(positionRequest));
    }

    /**
     * POST endpoint to check whether a position is inside a specified region.
     * @param regionRequest a RegionRequest containing the position and region vertices
     * @return 200 OK with true/false if valid, or 400 Bad Request if input is invalid
     */
    @PostMapping("/isInRegion")
    public ResponseEntity<?> isInRegion(@RequestBody RegionRequest regionRequest) {
        if (positionService.isInvalidRegion(regionRequest)) {return ResponseEntity.badRequest().build();}
        return ResponseEntity.ok().body(positionService.isInRegion(regionRequest));
    }
}
