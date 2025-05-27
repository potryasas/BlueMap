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

import de.bluecolored.bluemap.common.commands.java8compat.BlueMapCommands;
import de.bluecolored.bluemap.common.commands.java8compat.CommandResult;
import de.bluecolored.bluemap.common.plugin.Plugin;
import de.bluecolored.bluemap.common.serverinterface.CommandSource;
import de.bluecolored.bluemap.core.BlueMap;
import de.bluecolored.bluemap.core.logger.Logger;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.ComponentLike;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static de.bluecolored.bluemap.common.commands.TextFormat.NEGATIVE_COLOR;
import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor
public class CommandExecutor {

    private final Plugin plugin;

    public static class ExecutionResult {
        private final int resultCode;
        private final boolean parseFailure;
        
        public ExecutionResult(int resultCode, boolean parseFailure) {
            this.resultCode = resultCode;
            this.parseFailure = parseFailure;
        }
        
        public int resultCode() {
            return resultCode;
        }
        
        public boolean parseFailure() {
            return parseFailure;
        }
    }

    public ExecutionResult execute(String input, CommandSource context) {
        if (input == null || input.trim().isEmpty()) {
            // check if the plugin is not loaded first
            if (!Commands.checkPluginLoaded(plugin, context))
                return new ExecutionResult(0, false);

            return new ExecutionResult(0, true);
        }

        // Разбиваем ввод на аргументы
        String[] args = input.trim().split("\\s+");
        
        // Если первый аргумент не "bluemap", добавляем его
        if (args.length == 0 || !args[0].equalsIgnoreCase("bluemap")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = "bluemap";
            System.arraycopy(args, 0, newArgs, 1, args.length);
            args = newArgs;
        }
        
        try {
            CommandResult result = BlueMapCommands.executeCommand(context, input);
            
            if (result.getMessage() != null && !result.getMessage().isEmpty()) {
                context.sendMessage(text(result.getMessage()));
            }
            
            return new ExecutionResult(result.getResultCode(), false);
        } catch (Exception e) {
            Logger.global.logError("Command execution for '" + input + "' failed", e);
            context.sendMessage(text("There was an error executing this command! See logs or console for details.")
                    .color(NEGATIVE_COLOR));
            return new ExecutionResult(0, false);
        }
    }

}
