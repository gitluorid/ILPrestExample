package uk.ac.ed.acp.cw2.unit_tests;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.service.PositionService;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PositionServiceTest {
    private final PositionService service = new PositionService();

    @Nested
    class ValidateDistanceTests {
        @Test
        void withValidPositions_returnsNull() {
            DistanceRequest request = new DistanceRequest(
                    new Position(0.123, 0.456),
                    new Position(0.789, 1.234)
            );
            assertNull(service.validateDistance(request));
        }

        @Test
        void withNullPosition1_returnsError() {
            DistanceRequest request = new DistanceRequest(null, new Position(0.0, 0.0));
            String error = service.validateDistance(request);
            assertNotNull(error);
            assertTrue(error.contains("position1"));
        }

        @Test
        void withNullPosition2_returnsError() {
            DistanceRequest request = new DistanceRequest(new Position(0.0, 0.0), null);
            String error = service.validateDistance(request);
            assertNotNull(error);
            assertTrue(error.contains("position2"));
        }
    }

    @Nested
    class CalculateDistanceTests {
        @Test
        void withKnownPoints_returnsCorrectDistance() {
            Position p1 = new Position(-3.192473, 55.94600);
            Position p2 = new Position(-3.192473, 55.94400);
            DistanceRequest request = new DistanceRequest(p1, p2);

            double expectedDistance = 0.002;
            double distance = service.calculateDistance(request);

            assertEquals(expectedDistance, distance, 1e-6);
        }

        @Test
        void isCloseTo_withThreshold_returnsCorrectBoolean() {
            DistanceRequest request = new DistanceRequest(
                    new Position(0.0, 0.0), new Position(0.001, 0.001)
            );
            assertTrue(service.isCloseTo(request, 0.002));
            assertFalse(service.isCloseTo(request, 0.0005));
        }
    }

    @Nested
    class NextPositionTests {
        @Test
        void validateNextPosition_withValidAngle_returnsNull() {
            NextPositionRequest request = new NextPositionRequest(new Position(0.0,0.0), 22.5);
            assertNull(service.validateNextPosition(request));
        }

        @Test
        void validateNextPosition_withInvalidAngle_returnsError() {
            NextPositionRequest request = new NextPositionRequest(new Position(0.0,0.0), 15.0);
            String error = service.validateNextPosition(request);
            assertNotNull(error);
            assertTrue(error.contains("multiple of 22.5"));
        }

        @Test
        void calculateNextPosition_returnsCorrectPosition() {
            Position start = new Position(0.0, 0.0);
            NextPositionRequest request = new NextPositionRequest(start, 0.0); // East
            Position next = service.calculateNextPosition(request);
            assertTrue(next.lng() > start.lng());
            assertEquals(start.lat(), next.lat());
        }
    }

    @Nested
    class ValidateRegionTests {
        @Test
        void withValidPolygon_returnsNull() {
            Position pos = new Position(0.5, 0.5);
            List<Position> vertices = new LinkedList<>(Arrays.asList(
                    new Position(0.0,0.0), new Position(0.0,1.0),
                    new Position(1.0,1.0), new Position(1.0,0.0), new Position(0.0,0.0)
            ));
            Region region = new Region("square", vertices);
            RegionRequest regionRequest = new RegionRequest(pos, region);

            String errorMsg = service.validateRegion(regionRequest);
            assertNull(errorMsg);
        }

        @Test
        void withUnclosedPolygon_returnsError() {
            Position pos = new Position(0.5, 0.5);
            List<Position> vertices = new LinkedList<>(Arrays.asList(
                    new Position(0.0,0.0), new Position(0.0,1.0),
                    new Position(1.0,1.0), new Position(1.0,0.0) // missing closing vertex
            ));
            Region region = new Region("square", vertices);
            RegionRequest regionRequest = new RegionRequest(pos, region);

            String errorMsg = service.validateRegion(regionRequest);
            assertNotNull(errorMsg);
            assertTrue(errorMsg.contains("Polygon not closed"));
        }
    }

    @Nested
    class IsInRegionTests {
        @Test
        void pointInsidePolygon_returnsTrue() {
            Position pos = new Position(0.5, 0.5);
            List<Position> vertices = new LinkedList<>(Arrays.asList(
                    new Position(0.0,0.0), new Position(0.0,1.0),
                    new Position(1.0,1.0), new Position(1.0,0.0), new Position(0.0,0.0)
            ));
            Region region = new Region("square", vertices);
            RegionRequest regionRequest = new RegionRequest(pos, region);

            assertTrue(service.isInRegion(regionRequest));
        }

        @Test
        void pointOutsidePolygon_returnsFalse() {
            Position pos = new Position(1.5, 1.5);
            List<Position> vertices = new LinkedList<>(Arrays.asList(
                    new Position(0.0,0.0), new Position(0.0,1.0),
                    new Position(1.0,1.0), new Position(1.0,0.0), new Position(0.0,0.0)
            ));
            Region region = new Region("square", vertices);
            RegionRequest regionRequest = new RegionRequest(pos, region);

            assertFalse(service.isInRegion(regionRequest));
        }

        @Test
        void pointOnEdge_returnsTrue() {
            Position pos = new Position(0.5, 0.0);
            List<Position> vertices = new LinkedList<>(Arrays.asList(
                    new Position(0.0,0.0), new Position(0.0,1.0),
                    new Position(1.0,1.0), new Position(1.0,0.0), new Position(0.0,0.0)
            ));
            Region region = new Region("square", vertices);
            RegionRequest regionRequest = new RegionRequest(pos, region);

            assertTrue(service.isInRegion(regionRequest));
        }
    }

}
