# 寄录器

在玩家寄的时候保存物品栏。

---

## 前置
- LuckPerms（或其他权限插件）
- 一个 MySQL 数据库

## 指令
- /lastdeaths <用户名> - 显示玩家最近寄的日期
- /getdeath <id> - 显示玩家的遗物

## 权限
- dl.last - 允许使用 `lastdeaths` 指令
- dl.get - 允许使用 `getdeath` 指令
- dl.get.take - 允许使用 `getdeath` 指令时取走物品