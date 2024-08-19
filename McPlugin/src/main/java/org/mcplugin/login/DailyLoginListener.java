package org.mcplugin.login;import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;

public class DailyLoginListener implements Listener {

    private final DailyLoginReward dailyLoginReward;
    private final Plugin plugin;

    public DailyLoginListener(DailyLoginReward dailyLoginReward, Plugin plugin) {
        this.dailyLoginReward = dailyLoginReward;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // 异步处理签到逻辑
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            dailyLoginReward.handleLogin(event.getPlayer());
        });
    }
}
