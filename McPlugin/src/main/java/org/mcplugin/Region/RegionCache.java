package org.mcplugin.Region;

import org.mcplugin.mysql.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RegionCache {

    private final Map<UUID, List<Region>> regionCache = new HashMap<>();

    public List<Region> getPlayerRegions(UUID playerUUID) {
        return regionCache.get(playerUUID);
    }

    public void addPlayerRegion(UUID playerUUID, Region region) {
        regionCache.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(region);
    }

    public void removePlayerRegion(UUID playerUUID, Region region) {
        List<Region> regions = regionCache.get(playerUUID);
        if (regions != null) {
            regions.remove(region);
        }
    }

    public void refreshCache(DatabaseManager dbManager) {
        // 清空现有缓存
        regionCache.clear();

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = dbManager.getConnection();
            String query = "SELECT player_uuid, world, x1, y1, z1, x2, y2, z2 FROM player_regions";
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                UUID ownerUUID = UUID.fromString(rs.getString("player_uuid"));
                String world = rs.getString("world");
                int x1 = rs.getInt("x1");
                int y1 = rs.getInt("y1");
                int z1 = rs.getInt("z1");
                int x2 = rs.getInt("x2");
                int y2 = rs.getInt("y2");
                int z2 = rs.getInt("z2");

                Region region = new Region(ownerUUID, world, x1, y1, z1, x2, y2, z2);
                addPlayerRegion(ownerUUID, region);
            }

            System.out.println("领地缓存已刷新");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("刷新领地缓存时发生数据库错误");
        } finally {
            // 确保资源正确关闭
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Set<UUID> getAllOwners() {
        return regionCache.keySet();
    }
}
