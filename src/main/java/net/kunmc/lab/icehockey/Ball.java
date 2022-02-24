package net.kunmc.lab.icehockey;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
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
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

public class Ball extends BukkitRunnable implements Listener {
    private final ArmorStand armorStand;
    private final Player rider;
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final PacketContainer mountPacket;
    private Vector velocity = new Vector();
    private double friction = 0.0;

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

        runTaskTimerAsynchronously(plugin, 0, 0);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void remove() {
        armorStand.remove();
        HandlerList.unregisterAll(this);
        cancel();
    }

    public Vector getVelocity() {
        return velocity.clone();
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public double friction() {
        return friction;
    }

    public void friction(double friction) {
        this.friction = friction;
    }

    public void teleport(Location to) {
        if (to.getBlock().getType() != Material.AIR && to.getBlock().getType() != Material.CAVE_AIR) {
            return;
        }
        
        try {
            ((CraftArmorStand) armorStand).getHandle().teleportAndSync(to.getX(), to.getY(), to.getZ());
        } catch (Exception ignored) {
        }
    }

    public Player getRider() {
        return rider;
    }

    public Location getLocation() {
        return armorStand.getLocation();
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

        tickMove();
    }

    private void tickMove() {
        teleport(armorStand.getLocation().add(velocity.clone().multiply(0.05)));
        velocity.multiply(1 - friction);

        RayTraceResult rayTraceResult = rayTrace();
        if (rayTraceResult == null || rayTraceResult.getHitBlock() == null) {
            return;
        }
        if (!isCollideWith(rayTraceResult.getHitBlock())) {
            return;
        }

        Vector normal = rayTraceResult.getHitBlockFace().getDirection();
        Vector direction = direction().add(direction().multiply(-1).multiply(normal).multiply(2).multiply(normal)).normalize();
        velocity = direction.multiply(velocity.length());
    }

    private boolean isCollideWith(Block b) {
        BoundingBox boundingBox = rider.getBoundingBox().expand(0.625);
        return !b.isPassable() && boundingBox.overlaps(b.getBoundingBox());
    }

    private Vector direction() {
        return velocity.clone().normalize();
    }

    private RayTraceResult rayTrace() {
        if (velocity.length() == 0) {
            return null;
        }

        BoundingBox boundingBox = rider.getBoundingBox();
        World w = rider.getWorld();

        return w.rayTraceBlocks(boundingBox.getCenter().toLocation(w), direction(), 3);
    }
}
