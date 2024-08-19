package org.mcplugin.login;
import org.bukkit.entity.Player;
import org.mcplugin.mysql.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class DailyLoginReward {

    private final DatabaseManager dbManager;
    private final int dailyRewardPoints;

    public DailyLoginReward(DatabaseManager dbManager, int dailyRewardPoints) {
        this.dbManager = dbManager;
        this.dailyRewardPoints = dailyRewardPoints;
    }

    public void handleLogin(Player player) {
        UUID playerUUID = player.getUniqueId();
        LocalDate today = LocalDate.now();

        try (Connection connection = dbManager.getConnection()) {
            // 查询玩家的最后签到日期和当前积分
            String selectQuery = "SELECT last_login_date, points FROM player_rewards WHERE player_uuid = ?";
            PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
            selectStmt.setString(1, playerUUID.toString());
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                // 玩家已存在，检查最后签到日期
                LocalDate lastLoginDate = rs.getDate("last_login_date").toLocalDate();
                int points = rs.getInt("points");

                if (!lastLoginDate.equals(today)) {
                    // 更新签到日期和积分
                    points += dailyRewardPoints;
                    updatePlayerData(playerUUID, today, points);
                    player.sendMessage("签到成功！你获得了 " + dailyRewardPoints + " 积分。当前总积分: " + points);
                } else {
                    player.sendMessage("今天你已经签到过了！");
                }
            } else {
                // 玩家首次签到，插入新记录
                insertNewPlayerData(playerUUID, today, dailyRewardPoints);
                player.sendMessage("签到成功！你获得了 " + dailyRewardPoints + " 积分！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("签到失败，数据库错误！");
        }
    }

    private void updatePlayerData(UUID playerUUID, LocalDate loginDate, int points) throws SQLException {
        try (Connection connection = dbManager.getConnection()) {
            String updateQuery = "UPDATE player_rewards SET last_login_date = ?, points = ? WHERE player_uuid = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setDate(1, java.sql.Date.valueOf(loginDate));
            updateStmt.setInt(2, points);
            updateStmt.setString(3, playerUUID.toString());
            updateStmt.executeUpdate();
        }
    }

    private void insertNewPlayerData(UUID playerUUID, LocalDate loginDate, int points) throws SQLException {
        try (Connection connection = dbManager.getConnection()) {
            String insertQuery = "INSERT INTO player_rewards (player_uuid, last_login_date, points) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
            insertStmt.setString(1, playerUUID.toString());
            insertStmt.setDate(2, java.sql.Date.valueOf(loginDate));
            insertStmt.setInt(3, points);
            insertStmt.executeUpdate();
        }
    }
}
