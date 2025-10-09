package uk.ac.ed.acp.cw2.unit_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.acp.cw2.controller.ServiceController;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.service.PositionService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ServiceController}.
 * Tests the controller's endpoints using MockMvc with mocked PositionService.
 */
@WebMvcTest(ServiceController.class)
public class ServiceControllerMvcTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PositionService positionService;

    // Valid requests
    @Test
    void uid_returnsStudentId() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2550230"));
    }

    @Test
    void distanceTo_validRequest_returnsDistance() throws Exception {
        DistanceRequest distanceRequest = new DistanceRequest( // valid request
                new Position(0.0, 0.0),
                new Position(0.0, 0.0)
        );

        // Mock the service methods that the controller calls
        when(positionService.validateDistance(any())).thenReturn(null); // valid
        when(positionService.calculateDistance(any())).thenReturn(1.0); // mock result

        mockMvc.perform(post("/api/v1/distanceTo") // perform the request
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(distanceRequest)))
                // assert the response
                .andExpect(status().isOk())
                .andExpect(result -> {
                    double value = Double.parseDouble(result.getResponse().getContentAsString());
                    assertEquals(1.0, value, 1e-6);
                });
    }

    @Test
    void isCloseTo_validRequest_returnsTrue() throws Exception {
        DistanceRequest distanceRequest = new DistanceRequest(
                new Position(0.0, 0.0),
                new Position(0.0, 0.0)
        );

        when(positionService.validateDistance(any())).thenReturn(null);
        when(positionService.isCloseTo(any(), any(Double.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(distanceRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    boolean value = Boolean.parseBoolean(result.getResponse().getContentAsString());
                    assertTrue(value);
                });
    }

    @Test
    void nextPosition_validRequest_returnsPosition() throws Exception {
        NextPositionRequest nextPositionRequest = new NextPositionRequest(
                new Position(0.0, 0.0),
                0.0
        );
        Position expectedNext = new Position(0.001, 0.0);

        when(positionService.validateNextPosition(any())).thenReturn(null);
        when(positionService.calculateNextPosition(any())).thenReturn(expectedNext);

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nextPositionRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    Position value = objectMapper.readValue(result.getResponse().getContentAsString(), Position.class);
                    assertEquals(expectedNext.lng(), value.lng(), 1e-6);
                    assertEquals(expectedNext.lat(), value.lat(), 1e-6);
                });
    }

    @Test
    void isInRegion_validRequest_returnsTrue() throws Exception {
        Position pos = new Position(0.5, 0.5);
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 1.0),
                new Position(0.0, 0.0)
        );
        Region region = new Region("square", vertices);
        RegionRequest regionRequest = new RegionRequest(pos, region);

        when(positionService.validateRegion(any())).thenReturn(null);
        when(positionService.isInRegion(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regionRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    boolean value = Boolean.parseBoolean(result.getResponse().getContentAsString());
                    assertTrue(value);
                });
    }

    // Invalid requests
    @Test
    void distanceTo_invalidRequest_returnsBadRequest() throws Exception {
        DistanceRequest invalidRequest = new DistanceRequest(null, null);

        // Mock service to simulate validation failure
        when(positionService.validateDistance(any())).thenReturn("Invalid positions");

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isCloseTo_invalidRequest_returnsBadRequest() throws Exception {
        DistanceRequest invalidRequest = new DistanceRequest(null, null);
        when(positionService.validateDistance(any())).thenReturn("Invalid positions");
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nextPosition_invalidRequest_returnsBadRequest() throws Exception {
        NextPositionRequest invalidRequest = new NextPositionRequest(null, 15.0);
        when(positionService.validateNextPosition(any())).thenReturn("Invalid angle");
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isInRegion_invalidRequest_returnsBadRequest() throws Exception {
        RegionRequest invalidRequest = new RegionRequest(null, null);
        when(positionService.validateRegion(any())).thenReturn("Invalid region");
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

}
