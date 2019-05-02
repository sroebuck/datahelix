package com.scottlogic.deg.generator.generation;

/**
 *
 */
public interface StringRepresentation {

    /**
     * Returns the intersection of this string representation and another.
     */
    StringRepresentation intersect(StringRepresentation representation);

    /**
     * Returns the complement (negated form) of this string representation.
     */
    StringRepresentation complement();

    /**
     * Returns a value indicating if the string representation has a finite number of possible values or not.
     */
    boolean isFinite();

    /**
     * If the representation is finite, returns the number of values the string representation can represent.
     */
    long getValueCount();

    /**
     * Returns a value indicating if the candidate value is included in this string representation.
     */
    boolean match(String subject);
}
