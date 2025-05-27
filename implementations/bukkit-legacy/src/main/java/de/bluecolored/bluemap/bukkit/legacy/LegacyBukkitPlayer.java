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
package de.bluecolored.bluemap.bukkit.legacy;

import java.util.UUID;

import de.bluecolored.bluemap.bukkit.legacy.java8compat.Player;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.ServerWorld;

/**
 * Legacy Bukkit implementation of Player interface
 */
public class LegacyBukkitPlayer implements Player {
    private final org.bukkit.entity.Player bukkitPlayer;
    private final LegacyBukkitWorld world;

    public LegacyBukkitPlayer(org.bukkit.entity.Player bukkitPlayer, LegacyBukkitWorld world) {
        this.bukkitPlayer = bukkitPlayer;
        this.world = world;
    }

    @Override
    public String getName() {
        return bukkitPlayer.getName();
    }

    @Override
    public String getUUID() {
        return bukkitPlayer.getUniqueId().toString();
    }

    @Override
    public ServerWorld getWorld() {
        return world;
    }

    @Override
    public double getX() {
        return bukkitPlayer.getLocation().getX();
    }

    @Override
    public double getY() {
        return bukkitPlayer.getLocation().getY();
    }

    @Override
    public double getZ() {
        return bukkitPlayer.getLocation().getZ();
    }

    @Override
    public float getPitch() {
        return bukkitPlayer.getLocation().getPitch();
    }

    @Override
    public float getYaw() {
        return bukkitPlayer.getLocation().getYaw();
    }

    public org.bukkit.entity.Player getBukkitPlayer() {
        return bukkitPlayer;
    }
} 
