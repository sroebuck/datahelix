package com.scottlogic.deg.generator.generation;

import dk.brics.automaton.Automaton;

public class RegexStringRepresentation implements StringRepresentation {
    private final Automaton automaton;
    private final String regexString;

    public RegexStringRepresentation(String regexString, boolean matchFullString) {
        this.automaton = RegexToAutomatonParser.parse(regexString, matchFullString);
        this.regexString = regexString;
    }

    private RegexStringRepresentation(Automaton automaton, String regexString) {
        this.automaton = automaton;
        this.regexString = regexString;
    }

    @Override
    public StringRepresentation intersect(StringRepresentation representation) {
        if (representation instanceof RegexStringRepresentation) {
            Automaton mergedAutomaton = automaton.intersection(((RegexStringRepresentation) representation).automaton);
            String mergedRegex = String.format("%s ∩ %s", regexString, ((RegexStringRepresentation) representation).regexString);
            return new RegexStringRepresentation(mergedAutomaton, mergedRegex);
        }

        throw new RuntimeException("Unable to intersect string representations.");
    }

    @Override
    public StringRepresentation complement() {
        return new RegexStringRepresentation(automaton.complement(), String.format("¬(%s)", regexString));
    }v

    @Override
    public boolean isFinite() {
        return automaton.isFinite();
    }

    @Override
    public long getValueCount() {
        return automaton.
    }

    @Override
    public boolean match(String subject) {
        return false;
    }
}
