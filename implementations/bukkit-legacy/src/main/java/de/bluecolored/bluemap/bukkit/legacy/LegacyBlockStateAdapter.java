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

import de.bluecolored.bluemap.common.serverinterface.BlockProperty;
import de.bluecolored.bluemap.common.serverinterface.BlockState;
import de.bluecolored.bluemap.core.util.Key;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LegacyBlockStateAdapter {

    private final World world;
    private static final Map<String, BlockState> BLOCK_STATE_CACHE = new HashMap<String, BlockState>();
    
    public LegacyBlockStateAdapter(World world) {
        this.world = world;
    }
    
    public BlockState getBlockState(int x, int y, int z) {
        // Check bounds
        if (y < 0 || y > 255) {
            return createAirBlockState();
        }
        
        // Get block ID and data
        Block block = world.getBlockAt(x, y, z);
        int blockId = block.getTypeId();
        byte blockData = block.getData();
        
        // If it's air, return air block state
        if (blockId == 0) {
            return createAirBlockState();
        }
        
        // Create a unique key for the block state
        String cacheKey = blockId + ":" + blockData;
        
        // Check if the block state is already cached
        BlockState cachedState = BLOCK_STATE_CACHE.get(cacheKey);
        if (cachedState != null) {
            return cachedState;
        }
        
        // Create a new block state
        BlockState blockState = createBlockState(blockId, blockData);
        
        // Cache the block state
        BLOCK_STATE_CACHE.put(cacheKey, blockState);
        
        return blockState;
    }
    
    private BlockState createAirBlockState() {
        return createBlockState(0, (byte) 0);
    }
    
    private BlockState createBlockState(int blockId, byte data) {
        // Map Minecraft 1.5.2 block IDs to modern namespaced IDs
        String blockName = getLegacyBlockName(blockId);
        Map<String, BlockProperty> properties = getLegacyBlockProperties(blockId, data);
        
        return new BlockState() {
            private final Key key = new Key("minecraft", blockName);
            
            @Override
            public Key getKey() {
                return key;
            }

            @Override
            public Map<String, BlockProperty> getProperties() {
                return properties;
            }
            
            @Override
            public String toString() {
                return "BlockState{" + key + "}";
            }
        };
    }
    
    private String getLegacyBlockName(int blockId) {
        // Map legacy block IDs to modern namespaced IDs
        switch (blockId) {
            case 0: return "air";
            case 1: return "stone";
            case 2: return "grass_block";
            case 3: return "dirt";
            case 4: return "cobblestone";
            case 5: return "oak_planks";
            case 6: return "sapling";
            case 7: return "bedrock";
            case 8: return "water";
            case 9: return "water";
            case 10: return "lava";
            case 11: return "lava";
            case 12: return "sand";
            case 13: return "gravel";
            case 14: return "gold_ore";
            case 15: return "iron_ore";
            case 16: return "coal_ore";
            case 17: return "oak_log";
            case 18: return "oak_leaves";
            case 19: return "sponge";
            case 20: return "glass";
            case 21: return "lapis_ore";
            case 22: return "lapis_block";
            case 23: return "dispenser";
            case 24: return "sandstone";
            case 25: return "note_block";
            case 26: return "bed";
            case 27: return "powered_rail";
            case 28: return "detector_rail";
            case 29: return "sticky_piston";
            case 30: return "cobweb";
            case 31: return "grass";
            case 32: return "dead_bush";
            case 33: return "piston";
            case 34: return "piston_head";
            case 35: return "white_wool";
            case 36: return "moving_piston";
            case 37: return "dandelion";
            case 38: return "poppy";
            case 39: return "brown_mushroom";
            case 40: return "red_mushroom";
            case 41: return "gold_block";
            case 42: return "iron_block";
            case 43: return "stone_slab";
            case 44: return "stone_slab";
            case 45: return "brick_block";
            case 46: return "tnt";
            case 47: return "bookshelf";
            case 48: return "mossy_cobblestone";
            case 49: return "obsidian";
            case 50: return "torch";
            case 51: return "fire";
            case 52: return "spawner";
            case 53: return "oak_stairs";
            case 54: return "chest";
            case 55: return "redstone_wire";
            case 56: return "diamond_ore";
            case 57: return "diamond_block";
            case 58: return "crafting_table";
            case 59: return "wheat";
            case 60: return "farmland";
            case 61: return "furnace";
            case 62: return "furnace";
            case 63: return "sign";
            case 64: return "oak_door";
            case 65: return "ladder";
            case 66: return "rail";
            case 67: return "cobblestone_stairs";
            case 68: return "wall_sign";
            case 69: return "lever";
            case 70: return "stone_pressure_plate";
            case 71: return "iron_door";
            case 72: return "oak_pressure_plate";
            case 73: return "redstone_ore";
            case 74: return "redstone_ore";
            case 75: return "redstone_torch";
            case 76: return "redstone_torch";
            case 77: return "stone_button";
            case 78: return "snow";
            case 79: return "ice";
            case 80: return "snow_block";
            case 81: return "cactus";
            case 82: return "clay";
            case 83: return "sugar_cane";
            case 84: return "jukebox";
            case 85: return "oak_fence";
            case 86: return "pumpkin";
            case 87: return "netherrack";
            case 88: return "soul_sand";
            case 89: return "glowstone";
            case 90: return "nether_portal";
            case 91: return "jack_o_lantern";
            case 92: return "cake";
            case 93: return "repeater";
            case 94: return "repeater";
            case 95: return "white_stained_glass";
            case 96: return "trapdoor";
            case 97: return "infested_stone";
            case 98: return "stone_bricks";
            case 99: return "brown_mushroom_block";
            case 100: return "red_mushroom_block";
            case 101: return "iron_bars";
            case 102: return "glass_pane";
            case 103: return "melon";
            case 104: return "pumpkin_stem";
            case 105: return "melon_stem";
            case 106: return "vine";
            case 107: return "oak_fence_gate";
            case 108: return "brick_stairs";
            case 109: return "stone_brick_stairs";
            case 110: return "mycelium";
            case 111: return "lily_pad";
            case 112: return "nether_bricks";
            case 113: return "nether_brick_fence";
            case 114: return "nether_brick_stairs";
            case 115: return "nether_wart";
            case 116: return "enchanting_table";
            case 117: return "brewing_stand";
            case 118: return "cauldron";
            case 119: return "end_portal";
            case 120: return "end_portal_frame";
            case 121: return "end_stone";
            case 122: return "dragon_egg";
            case 123: return "redstone_lamp";
            case 124: return "redstone_lamp";
            case 125: return "oak_slab";
            case 126: return "oak_slab";
            case 127: return "cocoa";
            case 128: return "sandstone_stairs";
            case 129: return "emerald_ore";
            case 130: return "ender_chest";
            case 131: return "tripwire_hook";
            case 132: return "tripwire";
            case 133: return "emerald_block";
            case 134: return "spruce_stairs";
            case 135: return "birch_stairs";
            case 136: return "jungle_stairs";
            case 137: return "command_block";
            case 138: return "beacon";
            case 139: return "cobblestone_wall";
            case 140: return "flower_pot";
            case 141: return "carrots";
            case 142: return "potatoes";
            case 143: return "wooden_button";
            case 144: return "skeleton_skull";
            case 145: return "anvil";
            case 146: return "trapped_chest";
            case 147: return "light_weighted_pressure_plate";
            case 148: return "heavy_weighted_pressure_plate";
            case 149: return "comparator";
            case 150: return "comparator";
            case 151: return "daylight_detector";
            case 152: return "redstone_block";
            case 153: return "nether_quartz_ore";
            case 154: return "hopper";
            case 155: return "quartz_block";
            case 156: return "quartz_stairs";
            case 157: return "activator_rail";
            case 158: return "dropper";
            case 159: return "white_terracotta";
            case 170: return "hay_block";
            case 171: return "white_carpet";
            case 172: return "terracotta";
            case 173: return "coal_block";
            default: return "unknown";
        }
    }
    
    private Map<String, BlockProperty> getLegacyBlockProperties(int blockId, byte data) {
        if (data == 0) {
            return Collections.emptyMap();
        }
        
        Map<String, BlockProperty> properties = new HashMap<String, BlockProperty>();
        
        // Add specific properties based on block ID and data
        switch (blockId) {
            case 17: // Log
                if (data == 1) {
                    properties.put("variant", new StringBlockProperty("spruce"));
                } else if (data == 2) {
                    properties.put("variant", new StringBlockProperty("birch"));
                } else if (data == 3) {
                    properties.put("variant", new StringBlockProperty("jungle"));
                } else {
                    properties.put("variant", new StringBlockProperty("oak"));
                }
                break;
            case 18: // Leaves
                if (data == 1) {
                    properties.put("variant", new StringBlockProperty("spruce"));
                } else if (data == 2) {
                    properties.put("variant", new StringBlockProperty("birch"));
                } else if (data == 3) {
                    properties.put("variant", new StringBlockProperty("jungle"));
                } else {
                    properties.put("variant", new StringBlockProperty("oak"));
                }
                break;
            case 35: // Wool
                properties.put("color", new StringBlockProperty(getWoolColor(data)));
                break;
            case 95: // Stained Glass
                properties.put("color", new StringBlockProperty(getWoolColor(data)));
                break;
            case 98: // Stone Bricks
                if (data == 1) {
                    properties.put("variant", new StringBlockProperty("mossy"));
                } else if (data == 2) {
                    properties.put("variant", new StringBlockProperty("cracked"));
                } else if (data == 3) {
                    properties.put("variant", new StringBlockProperty("chiseled"));
                }
                break;
        }
        
        return properties;
    }
    
    private String getWoolColor(byte data) {
        switch (data) {
            case 1: return "orange";
            case 2: return "magenta";
            case 3: return "light_blue";
            case 4: return "yellow";
            case 5: return "lime";
            case 6: return "pink";
            case 7: return "gray";
            case 8: return "light_gray";
            case 9: return "cyan";
            case 10: return "purple";
            case 11: return "blue";
            case 12: return "brown";
            case 13: return "green";
            case 14: return "red";
            case 15: return "black";
            default: return "white";
        }
    }
    
    private static class StringBlockProperty implements BlockProperty {
        private final String value;
        
        public StringBlockProperty(String value) {
            this.value = value;
        }
        
        @Override
        public String getAsString() {
            return value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
} 