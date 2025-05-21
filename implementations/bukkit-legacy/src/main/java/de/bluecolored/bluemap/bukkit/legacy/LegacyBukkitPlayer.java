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

import de.bluecolored.bluemap.common.serverinterface.Player;
import de.bluecolored.bluemap.common.serverinterface.ServerWorld;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class LegacyBukkitPlayer implements Player {

    private final org.bukkit.entity.Player player;
    private String name;
    private String displayName;
    private boolean sneaking;
    private float health;
    
    private double posX;
    private double posY;
    private double posZ;
    private float rotationYaw;
    private ServerWorld world;

    public LegacyBukkitPlayer(org.bukkit.entity.Player player) {
        this.player = player;
        this.update();
    }

    public void update() {
        this.name = player.getName();
        this.displayName = player.getDisplayName();
        this.sneaking = player.isSneaking();
        this.health = player.getHealth();
        
        Location location = player.getLocation();
        this.posX = location.getX();
        this.posY = location.getY();
        this.posZ = location.getZ();
        this.rotationYaw = location.getYaw();
        this.world = LegacyBukkitPlugin.getInstance().getServerWorld(player.getWorld());
    }

    @Override
    public ServerWorld getWorld() {
        return world;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean isSneaking() {
        return sneaking;
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public boolean isInvisible() {
        return false; // Not available in 1.5.2 API
    }

    @Override
    public double getPosX() {
        return posX;
    }

    @Override
    public double getPosY() {
        return posY;
    }

    @Override
    public double getPosZ() {
        return posZ;
    }

    @Override
    public float getRotationYaw() {
        return rotationYaw;
    }

    @Override
    public Entity getEntity() {
        return player;
    }
} 