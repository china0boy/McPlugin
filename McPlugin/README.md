```
Mc版本：1.16.5
platform：buikkit
sever：spigot-1.16.5
```
该插件为 Minecraft 实现了以下核心功能：

每日登录签到功能：

玩家每日首次登录时，自动签到并获得积分奖励。
积分存储在 MySQL 数据库中，防止重复签到。

领地系统：

玩家可以使用木棍选择两个对角线方块来创建领地，创建领地需要消耗积分。
玩家创建的领地会被存储在 MySQL 数据库中，插件会定期刷新缓存中的领地数据。

领地保护：

其他玩家无法进入或在他人领地内交互，如破坏方块、放置方块、打开箱子等操作。
如果玩家进入他人的领地，会将他们传送回安全位置并发出警告信息。

下面是mysql数据库的建表语句

```CREATE TABLE IF NOT EXISTS player_rewards (
id INT AUTO_INCREMENT PRIMARY KEY,
player_uuid VARCHAR(36) NOT NULL UNIQUE,
last_login_date DATE NOT NULL,
points INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS player_regions (
id INT AUTO_INCREMENT PRIMARY KEY,
player_uuid VARCHAR(36) NOT NULL,
world VARCHAR(255) NOT NULL,
x1 INT NOT NULL,
y1 INT NOT NULL,
z1 INT NOT NULL,
x2 INT NOT NULL,
y2 INT NOT NULL,
z2 INT NOT NULL
);
```