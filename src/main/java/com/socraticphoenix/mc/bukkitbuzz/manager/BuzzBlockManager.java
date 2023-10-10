package com.socraticphoenix.mc.bukkitbuzz.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.block.Block;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class BuzzBlockManager {
    private Set<Loc> buzzBlocks = new LinkedHashSet<>();

    public JsonObject save() {
        JsonObject object = new JsonObject();
        JsonArray blocks = new JsonArray();

        this.buzzBlocks.forEach(loc -> {
            JsonObject locObj = new JsonObject();
            locObj.addProperty("world", loc.world);
            locObj.addProperty("x", loc.x);
            locObj.addProperty("y", loc.y);
            locObj.addProperty("z", loc.z);
            blocks.add(locObj);
        });

        object.add("blocks", blocks);
        return object;
    }

    public void load(JsonObject object) {
        this.buzzBlocks.clear();

        if (object.has("blocks")) {
            JsonElement blocksElem = object.get("blocks");
            if (blocksElem instanceof JsonArray) {
                ((JsonArray) blocksElem).forEach(elem -> {
                    JsonObject locObj = elem.getAsJsonObject();
                    this.buzzBlocks.add(new Loc(locObj.get("world").getAsString(),
                            locObj.get("x").getAsInt(), locObj.get("y").getAsInt(), locObj.get("z").getAsInt()));
                });
            }
        }
    }

    public Set<Loc> getBuzzBlocks() {
        return this.buzzBlocks;
    }

    public void add(String world, int x, int y, int z) {
        this.buzzBlocks.add(new Loc(world, x, y, z));
    }

    public void add(Block block) {
        this.add(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public void remove(String world, int x, int y, int z) {
        this.buzzBlocks.remove(new Loc(world, x, y, z));
    }

    public void remove(Block block) {
        this.remove(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public boolean contains(String world, int x, int y, int z) {
        return this.buzzBlocks.contains(new Loc(world, x, y, z));
    }

    public boolean contains(Block block) {
        return this.contains(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public static class Loc {
        public final String world;
        public final int x, y, z;

        public Loc(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Loc loc = (Loc) o;
            return x == loc.x && y == loc.y && z == loc.z && Objects.equals(world, loc.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, x, y, z);
        }
    }

}
