package org.mcplugin.Region;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.mcplugin.mysql.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionSelector implements Listener {

    private final Map<UUID, Block[]> playerSelections = new HashMap<>(); // 存储玩家选择的两个方块

    private final Plugin plugin;
    private final DatabaseManager dbManager;

    public RegionSelector(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查玩家是否手持木棍
        if (item != null && item.getType() == Material.STICK) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Block clickedBlock = event.getClickedBlock();

                if (clickedBlock != null) {
                    if (!playerSelections.containsKey(playerUUID)) {
                        // 玩家第一次点击，记录第一个方块
                        playerSelections.put(playerUUID, new Block[]{clickedBlock, null});
                        player.sendMessage("第一个点已选择: " + formatBlockPosition(clickedBlock));
                    } else {
                        // 玩家第二次点击，记录第二个方块
                        Block[] selection = playerSelections.get(playerUUID);
                        if (selection[1] == null) {
                            selection[1] = clickedBlock;
                            player.sendMessage("第二个点已选择: " + formatBlockPosition(clickedBlock));

                            // 开始创建领地
                            createRegion(player, selection[0], selection[1]);
                        } else {
                            // 重置选择，重新开始
                            playerSelections.remove(playerUUID);
                            player.sendMessage("已重置领地选择，请重新选择两个点。");
                        }
                    }
                }
            }
        }
    }

    private String formatBlockPosition(Block block) {
        return "(" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")";
    }

    private void createRegion(Player player, Block firstBlock, Block secondBlock) {
        UUID playerUUID = player.getUniqueId();

        // 异步处理，防止阻塞主线程
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = dbManager.getConnection()) {
                // 查询玩家的当前积分
                String selectQuery = "SELECT points FROM player_rewards WHERE player_uuid = ?";
                PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                selectStmt.setString(1, playerUUID.toString());
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    int points = rs.getInt("points");
                    int regionCost = calculateRegionCost(firstBlock, secondBlock); // 计算创建领地所需积分

                    if (points >= regionCost) {
                        // 扣除积分并保存领地
                        points -= regionCost;
                        String updatePointsQuery = "UPDATE player_rewards SET points = ? WHERE player_uuid = ?";
                        PreparedStatement updatePointsStmt = connection.prepareStatement(updatePointsQuery);
                        updatePointsStmt.setInt(1, points);
                        updatePointsStmt.setString(2, playerUUID.toString());
                        updatePointsStmt.executeUpdate();

                        // 保存领地信息到数据库
                        String insertRegionQuery = "INSERT INTO player_regions (player_uuid, world, x1, y1, z1, x2, y2, z2) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement insertRegionStmt = connection.prepareStatement(insertRegionQuery);
                        insertRegionStmt.setString(1, playerUUID.toString());
                        insertRegionStmt.setString(2, firstBlock.getWorld().getName());
                        insertRegionStmt.setInt(3, firstBlock.getX());
                        insertRegionStmt.setInt(4, firstBlock.getY());
                        insertRegionStmt.setInt(5, firstBlock.getZ());
                        insertRegionStmt.setInt(6, secondBlock.getX());
                        insertRegionStmt.setInt(7, secondBlock.getY());
                        insertRegionStmt.setInt(8, secondBlock.getZ());
                        insertRegionStmt.executeUpdate();

                        player.sendMessage("领地创建成功！剩余积分: " + points);
                    } else {
                        player.sendMessage("你的积分不足以创建此领地，所需积分: " + regionCost + "，当前积分: " + points);
                    }
                } else {
                    player.sendMessage("未找到你的积分数据，请先进行每日签到。");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage("创建领地时发生数据库错误。");
            }
        });
    }

    private int calculateRegionCost(Block firstBlock, Block secondBlock) {
        // 根据领地大小计算所需积分（简单示例: 每个方块消耗 1 积分）
        int width = Math.abs(firstBlock.getX() - secondBlock.getX()) + 1;
        int height = Math.abs(firstBlock.getY() - secondBlock.getY()) + 1;
        int depth = Math.abs(firstBlock.getZ() - secondBlock.getZ()) + 1;

        return width * height * depth; // 每个方块 1 积分
    }

}
