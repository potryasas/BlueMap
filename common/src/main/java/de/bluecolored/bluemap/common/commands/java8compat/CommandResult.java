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

/**
 * Результат выполнения команды
 */
public class CommandResult {
    private final int resultCode;
    private final boolean success;
    private final String message;

    /**
     * Создает успешный результат
     * @return успешный результат
     */
    public static CommandResult success() {
        return new CommandResult(1, true, null);
    }

    /**
     * Создает успешный результат с сообщением
     * @param message сообщение
     * @return успешный результат
     */
    public static CommandResult success(String message) {
        return new CommandResult(1, true, message);
    }

    /**
     * Создает неудачный результат
     * @return неудачный результат
     */
    public static CommandResult failure() {
        return new CommandResult(0, false, null);
    }

    /**
     * Создает неудачный результат с сообщением
     * @param message сообщение
     * @return неудачный результат
     */
    public static CommandResult failure(String message) {
        return new CommandResult(0, false, message);
    }

    /**
     * Создает результат с указанным кодом и статусом
     * @param resultCode код результата
     * @param success статус успеха
     * @param message сообщение (может быть null)
     */
    public CommandResult(int resultCode, boolean success, String message) {
        this.resultCode = resultCode;
        this.success = success;
        this.message = message;
    }

    /**
     * Получает код результата
     * @return код результата
     */
    public int getResultCode() {
        return resultCode;
    }

    /**
     * Проверяет, успешно ли выполнение
     * @return true, если выполнение успешно
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Получает сообщение
     * @return сообщение или null, если его нет
     */
    public String getMessage() {
        return message;
    }
} 