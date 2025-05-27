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

/**
 * Команда, которая выполняется только по точному соответствию имени
 */
public class LiteralCommand extends AbstractCommand {
    
    /**
     * Создает новую литеральную команду
     * @param name имя команды
     */
    public LiteralCommand(String name) {
        this(name, "", "/bluemap " + name, null);
    }
    
    /**
     * Создает новую литеральную команду
     * @param name имя команды
     * @param description описание команды
     */
    public LiteralCommand(String name, String description) {
        this(name, description, "/bluemap " + name, null);
    }
    
    /**
     * Создает новую литеральную команду
     * @param name имя команды
     * @param description описание команды
     * @param usage использование команды
     */
    public LiteralCommand(String name, String description, String usage) {
        this(name, description, usage, null);
    }
    
    /**
     * Создает новую литеральную команду
     * @param name имя команды
     * @param description описание команды
     * @param usage использование команды
     * @param permission разрешение для команды
     */
    public LiteralCommand(String name, String description, String usage, String permission) {
        super(name, description, usage, permission);
    }
    
    @Override
    protected CommandResult executeCommand(CommandSource source, String[] args) {
        // Литеральная команда по умолчанию делегирует выполнение подкомандам
        // Если аргументы пусты или не соответствуют подкомандам, возвращает справку
        StringBuilder helpMessage = new StringBuilder();
        helpMessage.append("Available commands:\n");
        
        for (Command subcommand : getSubcommands()) {
            if (subcommand.hasPermission(source)) {
                helpMessage.append("- ").append(subcommand.getName()).append(": ")
                    .append(subcommand.getDescription()).append("\n")
                    .append("  Usage: ").append(subcommand.getUsage()).append("\n");
            }
        }
        
        return CommandResult.success(helpMessage.toString());
    }
} 