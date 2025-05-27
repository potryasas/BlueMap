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
package de.bluecolored.bluemap.common.commands.java8compat;

import de.bluecolored.bluemap.common.serverinterface.CommandSource;
import java.util.Collections;
import java.util.List;

/**
 * Java 8 compatible implementation of SimpleArgumentParser
 */
public interface SimpleArgumentParser<T> {
    
    /**
     * Parse input to the target type
     * @param input the input string to parse
     * @param source the command source
     * @return the parsed value
     * @throws CommandParseException if parsing fails
     */
    T parse(String input, CommandSource source) throws CommandParseException;
    
    /**
     * Get suggestions for the current input
     * @param input the current input
     * @param source the command source
     * @return a list of suggestions
     */
    default List<String> getSuggestions(String input, CommandSource source) {
        return Collections.emptyList();
    }
    
    /**
     * Exception thrown when parsing command arguments fails
     */
    public static class CommandParseException extends Exception {
        public CommandParseException(String message) {
            super(message);
        }
        
        public CommandParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * A simple wrapper for String input
     */
    public static class InputReader {
        private final String input;
        private int cursor;
        
        public InputReader(String input) {
            this.input = input;
            this.cursor = 0;
        }
        
        public String getInput() {
            return input;
        }
        
        public int getCursor() {
            return cursor;
        }
        
        public void setCursor(int cursor) {
            this.cursor = cursor;
        }
        
        public String getRemaining() {
            if (cursor >= input.length()) return "";
            return input.substring(cursor);
        }
        
        public boolean hasRemaining() {
            return cursor < input.length();
        }
    }
    
    /**
     * Represents a command suggestion
     */
    public static class Suggestion implements Comparable<Suggestion> {
        private final String text;
        private final String tooltip;
        
        public Suggestion(String text) {
            this(text, null);
        }
        
        public Suggestion(String text, String tooltip) {
            this.text = text;
            this.tooltip = tooltip;
        }
        
        public String getText() {
            return text;
        }
        
        public String getTooltip() {
            return tooltip;
        }
        
        @Override
        public int compareTo(Suggestion other) {
            return this.text.compareTo(other.text);
        }
    }
} 