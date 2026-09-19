package com.beamillionaire.domain;

public enum Category {
    AI_FUNDAMENTALS("Artificial Intelligence Fundamentals"),
    KNOWLEDGE_AND_PROBLEM_REPRESENTATION("Knowledge and Problem Representation"),
    SEARCH_AND_GAME_PLAYING("Search and Game Playing"),
    MACHINE_LEARNING("Machine Learning"),
    NEURAL_NETWORKS("Neural Networks"),
    DEEP_LEARNING("Deep Learning"),
    FUTURE_OF_AI("Future of AI"),
    RESEARCH_IN_AI("Research in AI");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
