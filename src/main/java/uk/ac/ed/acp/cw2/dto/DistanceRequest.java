package uk.ac.ed.acp.cw2.dto;

/**
 * Represents a request involving two positions, used for distance calculations.
 */
public record DistanceRequest(Position position1, Position position2) {}
