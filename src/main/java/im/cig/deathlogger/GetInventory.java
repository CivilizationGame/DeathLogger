package im.cig.deathlogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;

public class GetInventory
        implements CommandExecutor {
    private DeathLogger plugin;

    public GetInventory(DeathLogger plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int deathId;
        if (!(sender instanceof Player)) {
            sender.sendMessage((Object)ChatColor.RED + this.plugin.playerOnly);
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("dl.get")) {
            player.sendMessage((Object)ChatColor.RED + this.plugin.noPerm);
            return true;
        }
        boolean canTakeItems = player.hasPermission("dl.get.take");
        if (args.length < 1) {
            player.sendMessage((Object)ChatColor.RED + this.plugin.usage + " /getdeath <id>");
            return true;
        }
        try {
            deathId = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
            player.sendMessage((Object)ChatColor.RED + this.plugin.wrongFormat);
            return true;
        }
        try (Connection connection = this.getConnection();){
            String query = "SELECT Message,Inventory FROM `DeathLogger` WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, deathId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String deathMsg = resultSet.getString("Message");
                byte[] inventoryData = resultSet.getBytes("Inventory");
                ItemStack[] inventoryContents = this.deserializeInventory(inventoryData);
                if (inventoryContents != null) {
                    Inventory inventory = Bukkit.createInventory(null, (int)54, (String)(this.plugin.invFromDeath + " #" + deathId));
                    inventory.setContents(inventoryContents);
                    ItemStack msg = new ItemStack(Material.PAPER);
                    ItemMeta msgMeta = msg.getItemMeta();
                    msgMeta.setDisplayName((Object)ChatColor.YELLOW + this.plugin.deathMsg);
                    ArrayList<String> msgLore = new ArrayList<String>();
                    msgLore.add((Object)ChatColor.GRAY + deathMsg);
                    msgMeta.setLore(msgLore);
                    msg.setItemMeta(msgMeta);
                    inventory.addItem(new ItemStack[]{msg});
                    player.openInventory(inventory);
                    inventory.getViewers().forEach(viewer -> {
                        if (viewer instanceof Player) {
                            Player p = (Player) viewer;
                            if (!canTakeItems) {
                                InventoryClickListener clickListener = new InventoryClickListener(p);
                                Bukkit.getPluginManager().registerEvents(clickListener, this.plugin);
                            }
                        }
                    });
                    player.sendMessage((Object)ChatColor.GREEN + this.plugin.invFromDeath + " #" + deathId + " " + this.plugin.wasOpened);
                } else {
                    player.sendMessage((Object)ChatColor.RED + this.plugin.invFromDeath + " #" + deathId + " " + this.plugin.isEmpty);
                }
            } else {
                player.sendMessage((Object)ChatColor.RED + this.plugin.notFound + " " + deathId);
            }
            resultSet.close();
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage((Object)ChatColor.RED + this.plugin.error);
        }
        return true;
    }

    private Connection getConnection() throws SQLException {
        String url = this.plugin.getURL();
        String user = this.plugin.getUser();
        String password = this.plugin.getPass();
        return DriverManager.getConnection(url, user, password);
    }

    private ItemStack[] deserializeInventory(byte[] data) {
        try {
            if (data == null) {
                return null;
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);
            int size = dataInput.readInt();
            ItemStack[] inventoryContents = new ItemStack[size];
            for (int i = 0; i < size; ++i) {
                inventoryContents[i] = (ItemStack)dataInput.readObject();
            }
            dataInput.close();
            return inventoryContents;
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

