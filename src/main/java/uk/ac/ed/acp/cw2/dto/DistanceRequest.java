package uk.ac.ed.acp.cw2.dto;

/**
 * Represents a request to calculate the distance between two positions.
 */
public record DistanceRequest(Position position1, Position position2) {}
