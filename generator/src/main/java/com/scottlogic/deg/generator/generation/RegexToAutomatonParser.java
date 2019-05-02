package com.scottlogic.deg.generator.generation;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexToAutomatonParser {
    private static final Map<String, String> PREDEFINED_CHARACTER_CLASSES;

    static {
        Map<String, String> characterClasses = new HashMap<>();
        characterClasses.put("\\\\d", "[0-9]");
        characterClasses.put("\\\\D", "[^0-9]");
        characterClasses.put("\\\\s", "[ \t\n\f\r]");
        characterClasses.put("\\\\S", "[^ \t\n\f\r]");
        characterClasses.put("\\\\w", "[a-zA-Z_0-9]");
        characterClasses.put("\\\\W", "[^a-zA-Z_0-9]");
        PREDEFINED_CHARACTER_CLASSES = Collections.unmodifiableMap(characterClasses);
    }

    public static Automaton parse(String regexString, boolean matchFullString) {
        final String anchoredStr = convertEndAnchors(regexString, matchFullString);
        final String requotedStr = escapeCharacters(anchoredStr);
        final RegExp bricsRegExp = expandShorthandClasses(requotedStr);

        Automaton generatedAutomaton = bricsRegExp.toAutomaton();
        generatedAutomaton.expandSingleton();
        return generatedAutomaton;
    }

    private static String convertEndAnchors(String regexString, boolean matchFullString) {
        final Matcher startAnchorMatcher = Pattern.compile("^\\^").matcher(regexString);

        if (startAnchorMatcher.find()) {
            regexString = startAnchorMatcher.replaceAll(""); // brics.RegExp doesn't use anchors - they're treated as literal ^/$ characters
        } else if (!matchFullString) {
            regexString = ".*" + regexString; // brics.RegExp only supports full string matching, so add .* to simulate it
        }

        final Matcher endAnchorMatcher = Pattern.compile("\\$$").matcher(regexString);

        if (endAnchorMatcher.find()) {
            regexString = endAnchorMatcher.replaceAll("");
        } else if (!matchFullString) {
            regexString = regexString + ".*";
        }

        return regexString;
    }

    private static String escapeCharacters(String regexString) {
        final Pattern patternRequoted = Pattern.compile("\\\\Q(.*?)\\\\E");
        final Pattern patternSpecial = Pattern.compile("[.^$*+?(){|\\[\\\\@]");
        StringBuilder sb = new StringBuilder(regexString);
        Matcher matcher = patternRequoted.matcher(sb);
        while (matcher.find()) {
            sb.replace(matcher.start(), matcher.end(), patternSpecial.matcher(matcher.group(1)).replaceAll("\\\\$0"));
        }
        return sb.toString();
    }

    /*
     * As the Briks regex parser doesn't recognise shorthand classes we need to convert them to character groups
     */
    private static RegExp expandShorthandClasses(String regex) {
        String finalRegex = regex;
        for (Map.Entry<String, String> charClass : PREDEFINED_CHARACTER_CLASSES.entrySet()) {
            finalRegex = finalRegex.replaceAll(charClass.getKey(), charClass.getValue());
        }
        return new RegExp(finalRegex);
    }
}
