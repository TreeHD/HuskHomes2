package me.william278.huskhomes2.teleport.points;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.UUID;

/**
 * An object representing a Player's home in-game
 * @author William
 */
public class Home extends SetPoint {

    private boolean isPublic;
    private UUID ownerUUID;
    private String ownerUsername;

    /**
     * An object representing a Player's home in-game
     * @param location The Bukkit {@link Location} of the home
     * @param server The Bungee server ID of the home
     * @param homeOwner The Player who owns the home
     * @param name The name of the home
     * @param isPublic Whether the home is public
     */
    public Home(Location location, String server, Player homeOwner, String name, boolean isPublic) {
        super(location, server, name, MessageManager.getRawMessage("home_default_description", homeOwner.getName()));
        this.ownerUUID = homeOwner.getUniqueId();
        this.ownerUsername = homeOwner.getName();
        this.isPublic = isPublic;
    }

    /**
     * An object representing a Player's home in-game
     * @deprecated Use other method that accepts UUID instead of UUID-string
     * @param teleportationPoint The {@link TeleportationPoint} position of the home
     * @param ownerUsername The username of the owner
     * @param ownerUUID The UUID string of the owner
     * @param name The name of the home
     * @param description The description of the home
     * @param isPublic Whether the home is public
     */
    public Home(TeleportationPoint teleportationPoint, String ownerUsername, String ownerUUID, String name, String description, boolean isPublic, long creationTime) throws IllegalArgumentException {
        super(teleportationPoint.worldName, teleportationPoint.x, teleportationPoint.y, teleportationPoint.z, teleportationPoint.yaw, teleportationPoint.pitch, teleportationPoint.server, name, description, creationTime);
        this.ownerUsername = ownerUsername;
        this.ownerUUID = UUID.fromString(ownerUUID);
        this.isPublic = isPublic;
    }

    /**
     * An object representing a Player's home in-game
     * @param teleportationPoint The {@link TeleportationPoint} position of the home
     * @param ownerUsername The username of the owner
     * @param ownerUUID The {@link UUID} of the owner
     * @param name The name of the home
     * @param description The description of the home
     * @param isPublic Whether the home is public
     */
    public Home(TeleportationPoint teleportationPoint, String ownerUsername, UUID ownerUUID, String name, String description, boolean isPublic, long creationTime) {
        super(teleportationPoint.worldName, teleportationPoint.x, teleportationPoint.y, teleportationPoint.z, teleportationPoint.yaw, teleportationPoint.pitch, teleportationPoint.server, name, description, creationTime);
        this.ownerUsername = ownerUsername;
        this.ownerUUID = ownerUUID;
        this.isPublic = isPublic;
    }

    /**
     * Returns whether the Home is public
     * @return if the Home is public
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Set the privacy of the home
     * @param isPublic should the home be public?
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Returns the UUID of the Home's owner
     * @return the owner's UUID
     */
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * Returns the username of the Home's owner
     * @return the owner's username
     */
    public String getOwnerUsername() {
        return ownerUsername;
    }

    /**
     * Returns the Player who owns the home
     * @return the Player owner of the home
     */
    public Player getOwner() {
        Player player = Bukkit.getPlayer(ownerUUID);
        if (player != null) {
            return player;
        } else {
            throw new IllegalStateException("The home owner is not online");
        }
    }

    /**
     * Set the owner of the home
     * @param player The new owner of the home
     */
    public void setOwner(Player player) {
        this.ownerUUID = player.getUniqueId();
        this.ownerUsername = player.getName();
    }

    // Returns the maximum number of set homes a player can make
    public static int getSetHomeLimit(Player p) {
        p.recalculatePermissions();
        int maxSetHomes = -1;
        for (PermissionAttachmentInfo permissionAI : p.getEffectivePermissions()) {
            String permission = permissionAI.getPermission();
            if (permission.contains("huskhomes.max_sethomes.")) {
                try {
                    if (Integer.parseInt(permission.split("\\.")[2]) > maxSetHomes) {
                        maxSetHomes = Integer.parseInt(permission.split("\\.")[2]);
                    }
                } catch (Exception ignored) { }
            }
        }
        if (maxSetHomes != -1) {
            return maxSetHomes;
        }
        return HuskHomes.getSettings().getMaximumHomes();
    }

    // Returns the number of set homes a player can set for free
    public static int getFreeHomes(Player p) {
        int freeSetHomes = -1;
        for (PermissionAttachmentInfo permissionAI : p.getEffectivePermissions()) {
            String permission = permissionAI.getPermission();
            if (permission.contains("huskhomes.free_sethomes.")) {
                try {
                    if (Integer.parseInt(permission.split("\\.")[2]) > freeSetHomes) {
                        freeSetHomes = Integer.parseInt(permission.split("\\.")[2]);
                    }
                } catch(Exception ignored) { }
            }
        }
        if (freeSetHomes != -1) {
            return freeSetHomes;
        }
        return HuskHomes.getSettings().getFreeHomeSlots();
    }
}
