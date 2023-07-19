package im.cig.deathlogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectOutputStream;

public final class DeathLogger
        extends JavaPlugin
        implements Listener {
    private Connection connection;
    private String server;
    String host = this.getConfig().getString("host");
    int port = this.getConfig().getInt("port");
    String db = this.getConfig().getString("database-name");
    String recentDeath = this.getConfig().getString("noRecentDeath");
    String missingName = this.getConfig().getString("missingName");
    String serverWord = this.getConfig().getString("server");
    String locationWord = this.getConfig().getString("location");
    String queryError = this.getConfig().getString("queryError");
    String noPerm = this.getConfig().getString("noPermission");
    String playerOnly = this.getConfig().getString("playerOnly");
    String usage = this.getConfig().getString("usage");
    String wrongFormat = this.getConfig().getString("wrongFormat");
    String invFromDeath = this.getConfig().getString("invFromDeath");
    String wasOpened = this.getConfig().getString("wasOpened");
    String isEmpty = this.getConfig().getString("isEmpty");
    String notFound = this.getConfig().getString("notFound");
    String error = this.getConfig().getString("error");
    String reloaded = this.getConfig().getString("reloaded");
    String deathMsg = this.getConfig().getString("deathMsg");

    private void loadConfig() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    public String getUser() {
        return this.getConfig().getString("username");
    }

    public String getPass() {
        return this.getConfig().getString("password");
    }

    public String getURL() {
        return "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.db;
    }

    public void seConnecter(String host, int port, String db, String user, String pass) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
        try {
            this.connection = DriverManager.getConnection(url, user, pass);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void seDeconnecter() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void createTableIfNotExists(String host, int port, String db, String user, String pass) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
        try {
            this.connection = DriverManager.getConnection(url, user, pass);
            Statement statement = this.connection.createStatement();
            String query = "CREATE TABLE IF NOT EXISTS `" + db + "`.`DeathLogger` (`ID` INT NOT NULL AUTO_INCREMENT ,`UUID` VARCHAR(36) NOT NULL ,`Name` CHAR(20) NOT NULL ,`Time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ,`Server` VARCHAR(255) NOT NULL ,`Location` VARCHAR(255) NOT NULL ,`Message` VARCHAR(1000) NOT NULL ,`Inventory` BLOB NULL ,PRIMARY KEY (`ID`)) ENGINE = InnoDB";
            statement.executeUpdate(query);
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            this.seDeconnecter();
        }
    }

    public void Insert(String uuid, String name, String server, Location pos, String message, ItemStack[] inv, Player player, String errorMessage) {
        String insertQuery = "INSERT INTO `DeathLogger` (`UUID`, `Name`, `Server`, `Location`, `Message`, `Inventory`) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement insertStatement = this.connection.prepareStatement(insertQuery);
            insertStatement.setString(1, uuid);
            insertStatement.setString(2, name);
            insertStatement.setString(3, server);
            insertStatement.setString(4, this.posToString(pos));
            insertStatement.setString(5, message);
            insertStatement.setBytes(6, this.serializeInventory(inv));
            insertStatement.executeUpdate();
            insertStatement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage((Object)ChatColor.RED + errorMessage);
        }
    }

    private String posToString(Location pos) {
        return pos.getWorld().getName() + "," + Math.round(pos.getX()) + "," + Math.round(pos.getY()) + "," + Math.round(pos.getZ());
    }

    private byte[] serializeInventory(ItemStack[] inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);
            dataOutput.writeInt(inventory.length);
            for (ItemStack itemStack : inventory) {
                dataOutput.writeObject((Object)itemStack);
            }
            dataOutput.close();
            return outputStream.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.loadConfig();
        this.createTableIfNotExists(this.host, this.port, this.db, this.getUser(), this.getPass());
        String host = this.getConfig().getString("host");
        int port = this.getConfig().getInt("port");
        String db = this.getConfig().getString("database-name");
        String user = this.getConfig().getString("username");
        String pass = this.getConfig().getString("password");
        this.server = this.getConfig().getString("serverName");
        this.getCommand("getdeath").setExecutor((CommandExecutor)new GetInventory(this));
        this.getCommand("lastdeaths").setExecutor((CommandExecutor)new GetLastDeath(this));
        this.getCommand("dlreload").setExecutor((CommandExecutor)new ReloadPlugin(this));
    }

    @EventHandler
    public void death(PlayerDeathEvent event) {
        this.seConnecter(this.host, this.port, this.db, this.getUser(), this.getPass());
        Player player = event.getEntity();
        UUID id = player.getUniqueId();
        String uuid = id.toString();
        String name = player.getName();
        Location pos = player.getLocation();
        String message = event.getDeathMessage();
        ItemStack[] inv = player.getInventory().getContents();
        String errorMessage = this.getConfig().getString("error");
        this.Insert(uuid, name, this.server, pos, message, inv, player, errorMessage);
        this.seDeconnecter();
    }

    public void onDisable() {
    }
}

