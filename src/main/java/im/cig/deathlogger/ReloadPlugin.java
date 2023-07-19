package im.cig.deathlogger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadPlugin
        implements CommandExecutor {
    private DeathLogger plugin;

    public ReloadPlugin(DeathLogger plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("dl.reload")) {
            this.plugin.reloadConfig();
            sender.sendMessage((Object)ChatColor.GREEN + this.plugin.reloaded);
        } else {
            sender.sendMessage((Object)ChatColor.RED + this.plugin.noPerm);
        }
        return true;
    }
}
