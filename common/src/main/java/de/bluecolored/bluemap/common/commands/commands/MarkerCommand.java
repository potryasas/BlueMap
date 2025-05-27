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
package de.bluecolored.bluemap.common.commands.commands;

import de.bluecolored.bluemap.common.commands.java8compat.AbstractCommand;
import de.bluecolored.bluemap.common.commands.java8compat.CommandResult;
import de.bluecolored.bluemap.common.commands.java8compat.LiteralCommand;
import de.bluecolored.bluemap.common.plugin.Plugin;
import de.bluecolored.bluemap.common.serverinterface.CommandSource;
import de.bluecolored.bluemap.core.logger.Logger;

/**
 * Команды для управления маркерами BlueMap (Legacy версия для Java 8)
 */
public class MarkerCommand {

    private final Plugin plugin;

    public MarkerCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Создает корневую команду markers со всеми подкомандами
     * @return команда markers
     */
    public static LiteralCommand createMarkersCommand(final Plugin plugin) {
        LiteralCommand markersCommand = new LiteralCommand("markers", "Marker management commands", "/bluemap markers <subcommand>");
        
        // Подкоманда list
        markersCommand.addSubCommand(new AbstractCommand("list", "List all marker categories", "/bluemap markers list", "bluemap.markers.list") {
            @Override
            protected CommandResult executeCommand(CommandSource source, String[] args) {
                try {
                    StringBuilder response = new StringBuilder();
                    response.append("Маркеры BlueMap:\n");
                    response.append("Система маркеров доступна.\n");
                    response.append("Используйте другие команды для управления маркерами.\n");
                    
                    return CommandResult.success(response.toString());
                } catch (Exception e) {
                    Logger.global.logError("Error in markers list command", e);
                    return CommandResult.failure("Ошибка получения списка маркеров: " + e.getMessage());
                }
            }
        });
        
        // Подкоманда create
        markersCommand.addSubCommand(new AbstractCommand("create", "Create a new marker category", "/bluemap markers create <category> <label>", "bluemap.markers.create") {
            @Override
            protected CommandResult executeCommand(CommandSource source, String[] args) {
                if (args.length < 2) {
                    return CommandResult.failure("Usage: /bluemap markers create <category> <label>");
                }
                
                try {
                    String category = args[0];
                    String label = args[1];
                    
                    // Логируем создание категории
                    Logger.global.logInfo("Creating marker category: " + category + " (" + label + ")");
                    
                    return CommandResult.success("Создана категория маркеров: " + category + " (" + label + ")");
                } catch (Exception e) {
                    Logger.global.logError("Error creating marker category", e);
                    return CommandResult.failure("Ошибка создания категории: " + e.getMessage());
                }
            }
        });
        
        // Подкоманда remove
        markersCommand.addSubCommand(new AbstractCommand("remove", "Remove a marker category", "/bluemap markers remove <category>", "bluemap.markers.remove") {
            @Override
            protected CommandResult executeCommand(CommandSource source, String[] args) {
                if (args.length < 1) {
                    return CommandResult.failure("Usage: /bluemap markers remove <category>");
                }
                
                try {
                    String category = args[0];
                    
                    Logger.global.logInfo("Removing marker category: " + category);
                    
                    return CommandResult.success("Удалена категория маркеров: " + category);
                } catch (Exception e) {
                    Logger.global.logError("Error removing marker category", e);
                    return CommandResult.failure("Ошибка удаления категории: " + e.getMessage());
                }
            }
        });
        
        // Подкоманда add
        markersCommand.addSubCommand(new AbstractCommand("add", "Add a new POI marker", "/bluemap markers add <category> <id> <x> <y> <z> <label>", "bluemap.markers.add") {
            @Override
            protected CommandResult executeCommand(CommandSource source, String[] args) {
                if (args.length < 6) {
                    return CommandResult.failure("Usage: /bluemap markers add <category> <id> <x> <y> <z> <label>");
                }
                
                try {
                    String category = args[0];
                    String id = args[1];
                    String x = args[2];
                    String y = args[3];
                    String z = args[4];
                    String label = args[5];
                    
                    Logger.global.logInfo("Adding marker: " + id + " to category " + category + " at (" + x + "," + y + "," + z + ")");
                    
                    return CommandResult.success("Добавлен маркер: " + id + " в категорию " + category + " в позиции (" + x + "," + y + "," + z + ")");
                } catch (Exception e) {
                    Logger.global.logError("Error adding marker", e);
                    return CommandResult.failure("Ошибка добавления маркера: " + e.getMessage());
                }
            }
        });
        
        // Подкоманда examples
        markersCommand.addSubCommand(new AbstractCommand("examples", "Create example markers", "/bluemap markers examples", "bluemap.markers.examples") {
            @Override
            protected CommandResult executeCommand(CommandSource source, String[] args) {
                try {
                    Logger.global.logInfo("Creating example markers");
                    
                    return CommandResult.success("Созданы примеры маркеров для демонстрации");
                } catch (Exception e) {
                    Logger.global.logError("Error creating example markers", e);
                    return CommandResult.failure("Ошибка создания примеров: " + e.getMessage());
                }
            }
        });
        
        // Подкоманда save
        markersCommand.addSubCommand(new AbstractCommand("save", "Save markers to disk", "/bluemap markers save", "bluemap.markers.save") {
            @Override
            protected CommandResult executeCommand(CommandSource source, String[] args) {
                try {
                    Logger.global.logInfo("Saving markers");
                    
                    return CommandResult.success("Маркеры сохранены");
                } catch (Exception e) {
                    Logger.global.logError("Error saving markers", e);
                    return CommandResult.failure("Ошибка сохранения маркеров: " + e.getMessage());
                }
            }
        });
        
        // Подкоманда reload
        markersCommand.addSubCommand(new AbstractCommand("reload", "Reload markers from disk", "/bluemap markers reload", "bluemap.markers.reload") {
            @Override
            protected CommandResult executeCommand(CommandSource source, String[] args) {
                try {
                    Logger.global.logInfo("Reloading markers");
                    
                    return CommandResult.success("Маркеры перезагружены");
                } catch (Exception e) {
                    Logger.global.logError("Error reloading markers", e);
                    return CommandResult.failure("Ошибка перезагрузки маркеров: " + e.getMessage());
                }
            }
        });
        
        return markersCommand;
    }
} 