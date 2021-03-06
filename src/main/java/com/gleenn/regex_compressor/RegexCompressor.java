package com.gleenn.regex_compressor;

import static com.gleenn.regex_compressor.Trie.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

public final class RegexCompressor {

    public static final String REGEX_THAT_MATCHES_NOTHING = "(?!.*)";

    public static Pattern pattern(List<String> strings) {
        return Pattern.compile(compress(strings));
    }

    public static Pattern wordPattern(List<String> strings) {
        if(strings.isEmpty()) return Pattern.compile(REGEX_THAT_MATCHES_NOTHING);

        StringBuilder result = new StringBuilder("\\b(?:");
        compressStringBuilder(strings, result);
        result.append(")\\b");
        return Pattern.compile(result.toString());
    }

    public static StringBuilder compressStringBuilder(List<String> strings, StringBuilder result) {
        Trie trie = new Trie();
        for(String string : strings) {
            trie.addWord(string);
        }
        buildRegex(trie, result);
        return result;
    }

    public static String compress(List<String> strings) {
        if(strings.isEmpty()) return REGEX_THAT_MATCHES_NOTHING;

        return compressStringBuilder(strings, new StringBuilder()).toString();
    }

    public static void buildRegex(final Trie trie, final StringBuilder result) {
        if(trie == null) throw new RuntimeException("Trie cannot be null");
        Character character = trie.getCharacter();
        if(character != null) {
            result.append(escape(character));
        }
        LinkedHashMap<Character, Trie> childrenTries = trie.getChildren();

        if(childrenTries.isEmpty()) return;

        if(hasOnlyChild(trie) && (hasNoChildren(getOnlyChild(trie)) || !trie.isTerminal())) {
            for(Trie child : childrenTries.values()) buildRegex(child, result);
        } else {
            boolean allOnlyChildren = true;
            for(Trie child : childrenTries.values()) {
                allOnlyChildren &= hasNoChildren(child) && child.isTerminal();
            }

            if(allOnlyChildren) {
                result.append("[");
                for(Trie child : childrenTries.values()) buildRegex(child, result);
                result.append("]");
            } else {
                if(character != null) result.append("(?:");
                for(Trie child : childrenTries.values()) {
                    buildRegex(child, result);
                    result.append("|");
                }
                result.deleteCharAt(result.length() - 1);
                if(character != null) result.append(")");
            }
        }

        if(trie.isTerminal()) result.append("?");
    }

    public static String escape(char c) {
        switch(c) {
            case '(': return "\\(";
            case ')': return "\\)";
            case '{': return "\\{";
            case '}': return "\\}";
            case '[': return "\\[";
            case ']': return "\\]";
            case '.': return "\\.";
            case '+': return "\\+";
            case '*': return "\\*";
            case '?': return "\\?";
            case '^': return "\\^";
            case '<': return "\\<";
            case '>': return "\\>";
            case '-': return "\\-";
            case '=': return "\\=";
            case '!': return "\\!";
            case '$': return "\\$";
            case '\\': return "\\\\";
            case '|': return "\\|";
            default: return c + "";
        }
    }
}
