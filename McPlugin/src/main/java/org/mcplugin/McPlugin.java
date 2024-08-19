package org.mcplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.mcplugin.Region.RegionCache;
import org.mcplugin.Region.RegionProtectionListener;
import org.mcplugin.Region.RegionSelector;
import org.mcplugin.login.DailyLoginListener;
import org.mcplugin.login.DailyLoginReward;
import org.mcplugin.mysql.DatabaseManager;

import java.sql.SQLException;

public final class McPlugin extends JavaPlugin {

    private DatabaseManager dbManager;

    @Override
    public void onEnable() {
        // 读取数据库配置
        String host = "localhost";
        int port = 3306;
        String database = "mc";
        String username = "root";
        String password = "123456";

        // 初始化并连接数据库
        dbManager = new DatabaseManager(host, port, database, username, password);
        try {
            dbManager.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            // 处理连接错误
        }
        saveDefaultConfig();
        int dailyRewardPoints = getConfig().getInt("daily-reward-points", 10);

        // 实例化每日登录奖励类
        DailyLoginReward dailyLoginReward = new DailyLoginReward(dbManager, dailyRewardPoints);

        // 注册每日登录监听器
        getServer().getPluginManager().registerEvents(new DailyLoginListener(dailyLoginReward, this), this);

        // 实例化 RegionSelector 并注册事件监听器
        RegionSelector regionSelector = new RegionSelector(this, dbManager);
        getServer().getPluginManager().registerEvents(regionSelector, this);

        // 初始化缓存并从数据库加载数据
        RegionCache regionCache = new RegionCache();
        regionCache.refreshCache(dbManager); // 初次加载数据

        // 注册领地保护监听器
        getServer().getPluginManager().registerEvents(new RegionProtectionListener(regionCache), this);

        // 设置定期刷新任务（每5分钟刷新一次）
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            regionCache.refreshCache(dbManager);
        }, 0L, 200L);  // 6000L 等于 5 分钟，单位为游戏刻 (1刻 = 50ms)
    }

    @Override
    public void onDisable() {
        dbManager.disconnect();
    }

    public DatabaseManager getDbManager() {
        return dbManager;
    }
}
