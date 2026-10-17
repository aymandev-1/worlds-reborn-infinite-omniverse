package com.worldsreborn.core.model;

public enum StepType {
    /** Completed by a specific game action (talk, use, enter...). */
    ACTION,
    /** Completed automatically when the player reaches the step's target location. */
    REACH,
    /** Completed after {@code required} increments. */
    COUNT
}
