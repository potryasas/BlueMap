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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Базовая реализация команды
 */
public abstract class AbstractCommand implements Command {
    protected final String name;
    protected final String description;
    protected final String usage;
    protected final String permission;
    protected final List<Command> subcommands = new ArrayList<>();

    /**
     * Создает новую команду
     * @param name имя команды
     * @param description описание команды
     * @param usage использование команды
     * @param permission разрешение для команды
     */
    public AbstractCommand(String name, String description, String usage, String permission) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
    }

    @Override
    public List<Command> getSubcommands() {
        return Collections.unmodifiableList(subcommands);
    }

    @Override
    public void addSubCommand(Command command) {
        subcommands.add(command);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        return permission == null || permission.isEmpty() || source.hasPermission(permission);
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Command subcommand : subcommands) {
                if (subcommand.getName().startsWith(args[0]) && subcommand.hasPermission(source)) {
                    suggestions.add(subcommand.getName());
                }
            }
            return suggestions;
        } else if (args.length > 1) {
            for (Command subcommand : subcommands) {
                if (subcommand.getName().equals(args[0]) && subcommand.hasPermission(source)) {
                    String[] subArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                    return subcommand.getSuggestions(source, subArgs);
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public CommandResult execute(CommandSource source, String[] args) {
        if (args.length > 0) {
            for (Command subcommand : subcommands) {
                if (subcommand.getName().equals(args[0])) {
                    if (!subcommand.hasPermission(source)) {
                        return CommandResult.failure("You don't have permission to use this command");
                    }
                    
                    String[] subArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                    return subcommand.execute(source, subArgs);
                }
            }
        }
        
        // Если не найдена подкоманда или аргументы пусты, выполняем эту команду
        if (!hasPermission(source)) {
            return CommandResult.failure("You don't have permission to use this command");
        }
        
        return executeCommand(source, args);
    }
    
    /**
     * Выполняет команду для этой конкретной реализации
     * @param source источник команды
     * @param args аргументы команды
     * @return результат выполнения
     */
    protected abstract CommandResult executeCommand(CommandSource source, String[] args);
} 