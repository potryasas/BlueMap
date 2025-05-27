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
package de.bluecolored.bluemap.common.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.bluecolored.bluemap.common.plugin.Plugin;
import de.bluecolored.bluemap.common.serverinterface.CommandSource;

/**
 * Обработчик команд для Brigadier
 */
public class BrigadierExecutionHandler extends CommandExecutor {
    private static final Message DEFAULT_FAILURE_MESSAGE = new Message() {
        @Override
        public String getString() {
            return "Unknown or incomplete command!";
        }
    };

    public BrigadierExecutionHandler(Plugin plugin) {
        super(plugin);
    }

    /**
     * Обрабатывает команду для Brigadier
     * @param input входная строка команды
     * @param context источник команды
     * @return результат выполнения команды
     * @throws CommandSyntaxException если команда невалидна
     */
    public int handle(String input, CommandSource context) throws CommandSyntaxException {
        ExecutionResult executionResult = this.execute(input, context);
        if (executionResult.parseFailure())
            return parseFailure(input);
        return executionResult.resultCode();
    }

    private int parseFailure(String input) throws CommandSyntaxException {
        throw new CommandSyntaxException(
                new SimpleCommandExceptionType(DEFAULT_FAILURE_MESSAGE),
                DEFAULT_FAILURE_MESSAGE,
                input,
                0
        );
    }
}
