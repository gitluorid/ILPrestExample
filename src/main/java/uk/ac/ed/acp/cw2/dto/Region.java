package uk.ac.ed.acp.cw2.dto;

import java.util.List;

/**
 * Represents a polygonal region defined by a name and a list of vertices.
 */
public record Region(String name, List<Position> vertices) {}
