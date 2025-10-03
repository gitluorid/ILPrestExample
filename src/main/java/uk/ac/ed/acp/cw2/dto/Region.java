package uk.ac.ed.acp.cw2.dto;

import java.util.List;

public record Region(String name, List<Position> vertices) {
}
