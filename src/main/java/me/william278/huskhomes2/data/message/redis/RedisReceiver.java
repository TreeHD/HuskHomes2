package me.william278.huskhomes2.data.message.redis;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.message.CrossServerMessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.Collections;

public class RedisReceiver {

    private static final HuskHomes plugin = HuskHomes.getInstance();
    public static final String REDIS_CHANNEL = "HuskHomes";

    private static Player getRandomReceiver() {
        ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) {
            return null;
        }
        Collections.shuffle(players);
        return players.get(0);
    }

    public static void listen() {
        Jedis jedis = new Jedis(HuskHomes.getSettings().getRedisHost(), HuskHomes.getSettings().getRedisPort());
        final String jedisPassword = HuskHomes.getSettings().getRedisPassword();
        if (!jedisPassword.equals("")) {
            jedis.auth(jedisPassword);
        }
        jedis.connect();
        if (jedis.isConnected()) {
            plugin.getLogger().info("Enabled Redis listener successfully!");
            new Thread(() -> jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (!channel.equals(REDIS_CHANNEL)) {
                        return;
                    }

                    /* HuskHomes Redis messages have a header formatted as such:
                     * <cluster_id>:<message_type>:<target_player>£ followed by the message arguments and data. */
                    final String[] splitMessage = message.split(RedisMessage.REDIS_MESSAGE_HEADER_SEPARATOR);
                    final String messageHeader = splitMessage[0];
                    int clusterID;

                    // Ensure the cluster ID matches
                    try {
                        clusterID = Integer.parseInt(messageHeader.split(":")[0]);
                    } catch (Exception e) {
                        // In case the message is malformed or the cluster ID is invalid
                        HuskHomes.getInstance().getLogger().warning("Received a Redis message on the HuskHomes channel with an invalid server Cluster ID! \n" +
                                "Please ensure that the cluster ID is set to a valid integer on all servers.");
                        return;
                    }
                    if (HuskHomes.getSettings().getClusterId() != clusterID) {
                        return;
                    }

                    // Get the type of redis message
                    final String messageType = messageHeader.split(":")[1];

                    // Get the player targeted by the message and make sure they are online
                    final String target = messageHeader.split(":")[2];
                    Player receiver = Bukkit.getPlayerExact(target);

                    // If the redis message was targeting this server
                    if (target.equalsIgnoreCase("-all-")) {
                        receiver = getRandomReceiver();
                    } else if (target.contains("server-")) { //todo In the future, allow redis messages to execute without a player online. For now it's duplicating the behaviour of plugin messages for consistency.
                        if (target.split("-")[1].equalsIgnoreCase(HuskHomes.getSettings().getServerID())) {
                            receiver = getRandomReceiver();
                        } else {
                            return; // The message was targeting another server; ignore
                        }
                    }

                    // Return if the target is not available
                    if (receiver == null) {
                        return;
                    }
                    if (!receiver.isOnline()) {
                        return;
                    }

                    final String messageData = splitMessage[1];
                    CrossServerMessageHandler.handlePluginMessage(new RedisMessage(clusterID, receiver.getName(), messageType, messageData), receiver);
                }
            }, REDIS_CHANNEL), "Redis Subscriber").start();
        } else {
            plugin.getLogger().severe("Failed to initialize the redis listener!");
        }
    }
}
