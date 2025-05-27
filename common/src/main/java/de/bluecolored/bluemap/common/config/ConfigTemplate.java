/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.common.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ConfigTemplate {

    private static final Pattern TEMPLATE_VARIABLE = Pattern.compile("\\$\\{([\\w\\-.]+)}"); // ${variable}
    private static final Pattern TEMPLATE_CONDITIONAL = Pattern.compile("\\$\\{([\\w\\-.]+)<<([\\s\\S]*?)>>}"); // ${conditionid<< ... >>}

    private final String template;

    private final Set<String> enabledConditionals;
    private final Map<String, String> variables;

    public ConfigTemplate(String template) {
        this.template = template;
        this.enabledConditionals = new HashSet<>();
        this.variables = new HashMap<>();
    }

    public ConfigTemplate setConditional(String conditional, boolean enabled) {
        if (enabled) enabledConditionals.add(conditional);
        else enabledConditionals.remove(conditional);
        return this;
    }

    public ConfigTemplate setVariable(String variable, String value) {
        if (value == null) variables.remove(variable);
        else variables.put(variable, replacerEscape(value));

        return this;
    }

    public String build() {
        return build(this.template, new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return enabledConditionals.contains(s);
            }
        }, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return variables.getOrDefault(s, "?");
            }
        });
    }

    private String build(String template, Predicate<? super String> conditionalResolver, Function<? super String, String> variableResolver) {
        // Use Java 8 compatible Matcher.replaceAll approach
        java.util.regex.Matcher conditionalMatcher = TEMPLATE_CONDITIONAL.matcher(template);
        StringBuffer resultBuffer = new StringBuffer();
        while (conditionalMatcher.find()) {
            String condition = conditionalMatcher.group(1);
            String content = conditionalMatcher.group(2);
            String replacement = conditionalResolver.test(condition) ? 
                  replacerEscape(build(content, conditionalResolver, variableResolver)) : "";
            conditionalMatcher.appendReplacement(resultBuffer, replacement.replace("$", "\\$"));
        }
        conditionalMatcher.appendTail(resultBuffer);
        String result = resultBuffer.toString();
        
        // Handle variable replacements
        java.util.regex.Matcher variableMatcher = TEMPLATE_VARIABLE.matcher(result);
        StringBuffer finalBuffer = new StringBuffer();
        while (variableMatcher.find()) {
            String variable = variableMatcher.group(1);
            String replacement = variableResolver.apply(variable);
            variableMatcher.appendReplacement(finalBuffer, replacement.replace("$", "\\$"));
        }
        variableMatcher.appendTail(finalBuffer);
        return finalBuffer.toString();
    }

    private String replacerEscape(String raw) {
        return raw
                .replace("\\", "\\\\")
                .replace("$", "\\$");
    }

}
