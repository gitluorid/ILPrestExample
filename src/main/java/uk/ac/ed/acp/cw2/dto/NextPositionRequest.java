package uk.ac.ed.acp.cw2.dto;

/**
 * Represents a request to calculate the next drone position from a start position and angle.
 */
public record NextPositionRequest(Position start, Double angle) {}

