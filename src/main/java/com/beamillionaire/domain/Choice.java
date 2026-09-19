package com.beamillionaire.domain;

public record Choice(String id, String text) {
    public Choice {
        if (id == null || !id.matches("[A-D]")) {
            throw new IllegalArgumentException("Choice ID must be A, B, C, or D.");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Choice text must not be blank.");
        }
    }
}

