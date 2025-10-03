package uk.ac.ed.acp.cw2.dto;

/**
 * Represents a request to check if a position is inside a region.
 * Contains the position to check and the target region.
 */
public record RegionRequest(Position position, Region region) {}
