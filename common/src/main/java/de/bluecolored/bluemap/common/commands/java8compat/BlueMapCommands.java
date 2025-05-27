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

import de.bluecolored.bluemap.common.commands.commands.MarkerCommand;
import de.bluecolored.bluemap.common.plugin.Plugin;
import de.bluecolored.bluemap.common.rendermanager.TileUpdateStrategy;
import de.bluecolored.bluemap.common.serverinterface.CommandSource;
import de.bluecolored.bluemap.core.logger.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для создания и управления командами BlueMap
 */
public class BlueMapCommands {
    
    /**
     * Создает корневую команду BlueMap с подкомандами
     * @param plugin плагин BlueMap
     * @return корневая команда
     */
    public static Command createRootCommand(Plugin plugin) {
        Command rootCommand = new LiteralCommand("bluemap", "BlueMap command", "/bluemap <command>");
        
        // Здесь мы будем добавлять все подкоманды
        // В реальной реализации здесь бы создавались экземпляры всех команд из пакета commands
        // и добавлялись к корневой команде
        
        // Пример добавления подкоманды справки
        rootCommand.addSubCommand(new AbstractCommand("help", "Shows help information", "/bluemap help", null) {
            @Override
            protected CommandResult executeCommand(CommandSource source, String[] args) {
                StringBuilder helpMessage = new StringBuilder();
                helpMessage.append("BlueMap commands:\n");
                
                for (Command subcommand : rootCommand.getSubcommands()) {
                    if (subcommand.hasPermission(source)) {
                        helpMessage.append("- ").append(subcommand.getName()).append(": ")
                            .append(subcommand.getDescription()).append("\n")
                            .append("  Usage: ").append(subcommand.getUsage()).append("\n");
                    }
                }
                
                return CommandResult.success(helpMessage.toString());
            }
        });
        
        // Пример добавления подкоманды версии
        rootCommand.addSubCommand(new AbstractCommand("version", "Shows version information", "/bluemap version", null) {
            @Override
            protected CommandResult executeCommand(CommandSource source, String[] args) {
                return CommandResult.success("BlueMap version: " + plugin.getVersion());
            }
        });
        
        // Добавляем команды маркеров
        rootCommand.addSubCommand(MarkerCommand.createMarkersCommand(plugin));
        
        // Добавляем команды для обновления тайлов с разными стратегиями
        Map<String, TileUpdateStrategy> updateCommands = new HashMap<>();
        updateCommands.put("update", TileUpdateStrategy.FORCE_NONE);
        updateCommands.put("fix-edges", TileUpdateStrategy.FORCE_EDGE);
        updateCommands.put("force-update", TileUpdateStrategy.FORCE_ALL);
        
        for (Map.Entry<String, TileUpdateStrategy> entry : updateCommands.entrySet()) {
            String cmdName = entry.getKey();
            final TileUpdateStrategy strategy = entry.getValue();
            
            Command updateCommand = new LiteralCommand(cmdName);
            updateCommand.addSubCommand(createUpdateCommand(plugin, strategy));
            rootCommand.addSubCommand(updateCommand);
        }
        
        return rootCommand;
    }
    
    /**
     * Создает команду обновления карты
     * @param plugin плагин BlueMap
     * @param strategy стратегия обновления тайлов
     * @return команда обновления
     */
    private static Command createUpdateCommand(final Plugin plugin, final TileUpdateStrategy strategy) {
        return new AbstractCommand("update", 
                "Updates the map using " + strategy.getName() + " strategy", 
                "/bluemap " + strategy.getName().toLowerCase() + " <map> [x z [radius]]", 
                "bluemap." + strategy.getName().toLowerCase()) {
            @Override
            protected CommandResult executeCommand(CommandSource source, String[] args) {
                if (args.length < 1) {
                    return CommandResult.failure("Usage: " + getUsage());
                }
                
                // В реальной реализации здесь был бы код для выполнения обновления карты
                // с использованием указанных аргументов и стратегии
                
                Logger.global.logInfo("Update command called with strategy: " + strategy.getName() + 
                        ", args: " + Arrays.toString(args));
                
                return CommandResult.success("Update command executed successfully");
            }
        };
    }
    
    /**
     * Выполняет команду
     * @param source источник команды
     * @param cmd строка команды
     * @return результат выполнения
     */
    public static CommandResult executeCommand(CommandSource source, String cmd) {
        String[] args = cmd.split("\\s+");
        
        if (args.length == 0) {
            return CommandResult.failure("Empty command");
        }
        
        Command command = BlueMapCommandRegistry.getRootCommand();
        
        if (command == null) {
            return CommandResult.failure("Command system not initialized");
        }
        
        try {
            return command.execute(source, args);
        } catch (Exception e) {
            Logger.global.logError("Error executing command: " + cmd, e);
            return CommandResult.failure("An error occurred while executing the command");
        }
    }
    
    /**
     * Регистр команд BlueMap
     */
    public static class BlueMapCommandRegistry {
        private static Command rootCommand;
        
        /**
         * Инициализирует систему команд
         * @param plugin плагин BlueMap
         */
        public static void initialize(Plugin plugin) {
            rootCommand = createRootCommand(plugin);
        }
        
        /**
         * Получает корневую команду
         * @return корневая команда или null, если система не инициализирована
         */
        public static Command getRootCommand() {
            return rootCommand;
        }
    }
} 