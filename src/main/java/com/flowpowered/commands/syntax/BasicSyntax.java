package com.flowpowered.commands.syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.flowpowered.commons.StringUtil;

public class BasicSyntax implements Syntax {
    private final Pattern quoteStart, unescape, sepPattern, escapeMatch;
    private final String quoteEnd, separator, escapeReplace;
    private final Map<String, Pattern> quoteEnds = new ConcurrentHashMap<>();

    public BasicSyntax(String quoteStart, String quoteEnd, String separator, String separatorPattern, String unescape, String escapeMatch, String escapeReplace) {
        this.quoteStart = Pattern.compile(quoteStart);
        this.quoteEnd = quoteEnd;
        this.unescape = Pattern.compile(unescape);
        this.separator = separator;
        this.sepPattern = Pattern.compile(separatorPattern);
        this.escapeMatch = Pattern.compile(escapeMatch);
        this.escapeReplace = escapeReplace;
    }

    @Override
    public String split(String input, List<String> output) {
        List<String> args = output;
        String unclosedQuote = null;
        args.add("");
        Matcher startMatcher = quoteStart.matcher(input);
        int index = 0;
        while (startMatcher.find(index)) {
            int start = startMatcher.start(1);
            String quote = startMatcher.group(1);
            String before = input.substring(index, start);
            List<String> split = splitIgnoreQuotes(before, sepPattern, 1);
            args.add(args.remove(args.size() - 1) + split.get(0));
            args.addAll(split.subList(1, split.size()));
            Pattern endPattern = Pattern.compile(quoteEnd.replaceFirst("%s", StringUtil.escapeRegex(quote)));
            Matcher endMatcher = endPattern.matcher(input);
            if (endMatcher.find(start + 1)) {
                index = endMatcher.end(1);
            } else {
                index = input.length(); // Assume it's quoted all the way till the end
                unclosedQuote = quote;
            }
            String quoted = input.substring(start, index);
            args.add(args.remove(args.size() - 1) + quoted);
        }
        if (index < input.length()) {
            List<String> split = splitIgnoreQuotes(input.substring(index), sepPattern, 1);
            args.add(args.remove(args.size() - 1) + split.get(0));
            args.addAll(split.subList(1, split.size()));
        }
        return unclosedQuote;
    }

    @Override
    public String unescape(String input) {
        StringBuffer buf = new StringBuffer(input.length());
        Matcher startMatcher = quoteStart.matcher(input);
        int index = 0;
        while (startMatcher.find(index)) {
            int endOfStart = startMatcher.end(1);
            String quote = StringUtil.escapeRegex(startMatcher.group(1));
            Pattern endPattern = Pattern.compile(quoteEnd.replaceFirst("%s", quote));
            startMatcher.appendReplacement(buf, startMatcher.group().replaceFirst(quote, ""));
            Matcher endMatcher = endPattern.matcher(input);
            if (endMatcher.find(endOfStart)) {
                buf.append(input, endOfStart, endMatcher.start(1));
                index = endMatcher.end(1);
            } else {
                startMatcher.appendTail(buf);
                index = input.length();
            }
        }
        if (index < input.length()) {
            buf.append(input, index, input.length());
        }
        return unescape.matcher(buf).replaceAll("$1");
    }

    @Override
    public String escape(String input) {
        return escapeMatch.matcher(input).replaceAll(escapeReplace);
    }

    @Override
    public String getSeparator() {
        return separator;
    }

    protected Pattern getQuoteEndPattern(String quote) {
        Pattern pattern = quoteEnds.get(quote);
        if (pattern == null) {
            pattern = Pattern.compile(quoteEnd.replaceFirst("%s", quote));
            quoteEnds.put(quote, pattern);
        }
        return pattern;
    }

    protected static List<String> splitIgnoreQuotes(String input, Pattern separator, int group) {
        List<String> result = new ArrayList<>();
        int index = 0;
        Matcher m = separator.matcher(input);
        while (m.find(index)) {
            result.add(input.substring(index, m.start(group)));
            index = m.end(group);
        }
        result.add(input.substring(index, input.length()));
        return result;
    }

    public static BasicSyntax SPOUT_SYNTAX = new BasicSyntax("(?:^| )(['\"])", // Quote start
            "[^\\\\](%s)(?: |$)", // Quote end
            " ", // Separator
            "( )", // Separator regex
            "\\\\([\"'])", // Unescape
            "['\"]", // Escape match
            "\\\\$0" // Escape replace
    );
}
