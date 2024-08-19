package org.mcplugin.mysql;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private String url;
    private String username;
    private String password;
    private Connection connection;

    public DatabaseManager(String host, int port, String database, String username, String password) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
        this.username = username;
        this.password = password;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("数据库连接成功！");
        } catch (SQLException e) {
            System.out.println("数据库连接失败: " + e.getMessage());
            throw e;
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("数据库连接已断开。");
            } catch (SQLException e) {
                System.out.println("断开数据库连接时出错: " + e.getMessage());
            }
        }
    }

    public Connection getConnection()  throws SQLException{
        // 检查连接是否关闭或为空
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("数据库连接已成功建立");
            } catch (SQLException e) {
                System.out.println("数据库连接失败: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }
}
