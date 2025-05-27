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
import java.util.List;

/**
 * Интерфейс для всех команд, заменяющий интерфейс из библиотеки bluecommands
 */
public interface Command {
    
    /**
     * Выполняет команду
     * @param source источник команды
     * @param args аргументы команды
     * @return результат выполнения
     */
    CommandResult execute(CommandSource source, String[] args);
    
    /**
     * Возвращает подкоманды этой команды
     * @return список подкоманд
     */
    List<Command> getSubcommands();
    
    /**
     * Добавляет подкоманду
     * @param command подкоманда
     */
    void addSubCommand(Command command);
    
    /**
     * Получает имя команды
     * @return имя команды
     */
    String getName();
    
    /**
     * Получает описание команды
     * @return описание команды
     */
    String getDescription();
    
    /**
     * Получает использование команды
     * @return использование команды
     */
    String getUsage();
    
    /**
     * Проверяет, имеет ли пользователь разрешение на использование этой команды
     * @param source источник команды
     * @return true, если пользователь имеет разрешение
     */
    boolean hasPermission(CommandSource source);
    
    /**
     * Получает список предложений для автодополнения
     * @param source источник команды
     * @param args аргументы команды
     * @return список предложений
     */
    List<String> getSuggestions(CommandSource source, String[] args);
} 