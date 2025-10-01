package uk.ac.ed.acp.cw2.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.RuntimeEnvironment;
import uk.ac.ed.acp.cw2.dto.DistanceRequest;

import java.net.URL;
import java.time.Instant;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
@RequestMapping("/api/v1")
public class ServiceController {

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

    @PostMapping("/distanceTo")
    public ResponseEntity<?> distanceTo(@RequestBody DistanceRequest distanceRequest) {
        if (distanceRequest.position1.lng == null || distanceRequest.position1.lat == null ||
                distanceRequest.position2.lng == null || distanceRequest.position2.lat == null) {
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }

        double lngDistance = distanceRequest.position1.lng - distanceRequest.position2.lng;
        double latDistance = distanceRequest.position1.lat - distanceRequest.position2.lat;
        double distance = Math.sqrt(lngDistance * lngDistance + latDistance * latDistance);
        return  ResponseEntity.ok().body(distance);
    }
}
