package im.cig.deathlogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetLastDeath
        implements CommandExecutor {
    private DeathLogger plugin;

    public GetLastDeath(DeathLogger plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Player player = (Player)sender;
        if (player.hasPermission("dl.last")) {
            if (args.length != 1) {
                player.sendMessage((Object)ChatColor.RED + this.plugin.missingName);
            } else {
                String target = args[0];
                String url = this.plugin.getURL();
                String user = this.plugin.getUser();
                String password = this.plugin.getPass();
                try (Connection connection = DriverManager.getConnection(url, user, password);){
                    String query = "SELECT Server, Location, id FROM `DeathLogger` WHERE Name = ? ORDER BY id DESC LIMIT 10";
                    try (PreparedStatement statement = connection.prepareStatement(query);){
                        statement.setString(1, target);
                        ResultSet resultSet = statement.executeQuery();
                        if (!resultSet.isBeforeFirst()) {
                            player.sendMessage((Object)ChatColor.RED + this.plugin.recentDeath);
                        } else {
                            while (resultSet.next()) {
                                String server = resultSet.getString("Server");
                                String position = resultSet.getString("Location");
                                int id = resultSet.getInt("id");
                                player.sendMessage(this.plugin.serverWord + ": " + server + " | " + this.plugin.locationWord + ": " + position + " | ID: " + id);
                            }
                        }
                        resultSet.close();
                    }
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    player.sendMessage((Object)ChatColor.RED + this.plugin.queryError);
                }
            }
        } else {
            player.sendMessage((Object)ChatColor.RED + this.plugin.noPerm);
        }
        return true;
    }
}

