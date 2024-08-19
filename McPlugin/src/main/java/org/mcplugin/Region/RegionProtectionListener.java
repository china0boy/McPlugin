package org.mcplugin.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.mcplugin.mysql.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RegionProtectionListener implements Listener {

    private final RegionCache regionCache;
    private final Map<UUID, Location> previousLocations = new HashMap<>();

    public RegionProtectionListener(RegionCache regionCache) {
        this.regionCache = regionCache;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        UUID playerUUID = player.getUniqueId();

        // 检查玩家是否在他人的领地内
        if (isInsideOtherPlayersRegion(player, location)) {
            Location previousLocation = previousLocations.get(playerUUID);

            // 如果存在之前的合法位置，将玩家传送回该位置
            if (previousLocation != null) {
                player.teleport(previousLocation);
                player.sendMessage("你不能进入他人的领地！");
            }
        } else {
            // 记录当前的合法位置
            previousLocations.put(playerUUID, location.clone());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        if (isInsideOtherPlayersRegion(player, location)) {
            player.sendMessage("你不能在他人的领地内破坏方块！");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        if (isInsideOtherPlayersRegion(player, location)) {
            player.sendMessage("你不能在他人的领地内放置方块！");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location location = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : null;

        if (location != null && isInsideOtherPlayersRegion(player, location)) {
            // 防止在领地内交互可交互的方块（如箱子、按钮、拉杆等）
            Material blockType = event.getClickedBlock().getType();
            if (isInteractiveBlock(blockType)) {
                player.sendMessage("你不能在他人的领地内使用此方块！");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Location location = event.getEntity().getLocation();

            if (isInsideOtherPlayersRegion(player, location)) {
                // 阻止玩家在他人领地内攻击实体
                player.sendMessage("你不能在他人的领地内攻击实体！");
                event.setCancelled(true);
            }
        }
    }

    private boolean isInteractiveBlock(Material material) {
        // 可交互方块列表，玩家不能在他人领地内使用这些方块
        return material == Material.CHEST ||
                material == Material.FURNACE ||
                material == Material.ANVIL ||
                material == Material.BREWING_STAND ||
                material == Material.ENCHANTING_TABLE ||
                material == Material.LEVER ||
                material == Material.STONE_BUTTON ||
                material == Material.ACACIA_BUTTON ||
                material == Material.DISPENSER ||
                material == Material.DROPPER ||
                material == Material.HOPPER ||
                material == Material.TRAPPED_CHEST ||
                material == Material.BARREL;
    }

    private boolean isInsideOtherPlayersRegion(Player player, Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        UUID playerUUID = player.getUniqueId();

        // 遍历所有缓存中的领地
        for (UUID ownerUUID : regionCache.getAllOwners()) {
            List<Region> allRegions = regionCache.getPlayerRegions(ownerUUID);

            if (allRegions != null) {
                for (Region region : allRegions) {
                    if (region.contains(location) && !region.getOwnerUUID().equals(playerUUID)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}

