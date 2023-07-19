package im.cig.deathlogger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;

public class InventoryClickListener implements Listener {
    private Player player;

    public InventoryClickListener(Player player) {
        this.player = player;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().equals(player)) {
            event.setCancelled(true);
        }
    }
}