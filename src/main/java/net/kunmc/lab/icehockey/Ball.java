package net.kunmc.lab.icehockey;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

public class Ball extends BukkitRunnable implements Listener {
    private final ArmorStand armorStand;
    private final Player rider;
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final PacketContainer mountPacket;

    public Ball(Player rider, Location location, Plugin plugin) {
        armorStand = ((ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND, CreatureSpawnEvent.SpawnReason.CUSTOM, e -> {
            ArmorStand as = ((ArmorStand) e);
            as.addPassenger(rider);
            as.setMarker(true);
            as.setVisible(false);
        }));

        this.rider = rider;

        mountPacket = protocolManager.createPacket(PacketType.Play.Server.MOUNT);
        mountPacket.getIntegers().write(0, armorStand.getEntityId());
        mountPacket.getIntegerArrays().write(0, new int[]{rider.getEntityId()});

        runTaskTimerAsynchronously(plugin, 0, 4);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void remove() {
        armorStand.remove();
        HandlerList.unregisterAll(this);
        cancel();
    }

    public void teleport(Location to) {
        armorStand.teleport(to);
    }

    @EventHandler
    private void onRiderDismount(EntityDismountEvent e) {
        Entity vehicle = e.getDismounted();
        if (vehicle.equals(armorStand)) {
            e.setCancelled(true);
        }
    }

    @Override
    public void run() {
        if (armorStand.isDead()) {
            remove();
            return;
        }

        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                protocolManager.sendServerPacket(p, mountPacket);
            }
        } catch (Exception ignored) {
        }
    }
}
