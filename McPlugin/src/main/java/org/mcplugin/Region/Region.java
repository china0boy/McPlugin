package org.mcplugin.Region;

import org.bukkit.Location;

import java.util.UUID;

public class Region {

    private final UUID ownerUUID;
    private final String world;
    private final int x1, y1, z1, x2, y2, z2;

    public Region(UUID ownerUUID, String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.ownerUUID = ownerUUID;
        this.world = world;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public boolean contains(Location location) {
        if (!location.getWorld().getName().equals(world)) return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return (x >= Math.min(x1, x2) && x <= Math.max(x1, x2)) &&
                (y >= Math.min(y1, y2) && y <= Math.max(y1, y2)) &&
                (z >= Math.min(z1, z2) && z <= Math.max(z1, z2));
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }
}
