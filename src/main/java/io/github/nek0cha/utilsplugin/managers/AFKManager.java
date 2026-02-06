package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AFKManager {

    private final Utilsplugin plugin;
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final Set<UUID> afkPlayers = new HashSet<>();

    public AFKManager(Utilsplugin plugin) {
        this.plugin = plugin;
        startAFKChecker();
    }

    private void startAFKChecker() {
        if (!plugin.getConfig().getBoolean("features.afk.enabled", true)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                long timeout = plugin.getConfig().getLong("features.afk.timeout", 300) * 1000;
                long currentTime = System.currentTimeMillis();

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();

                    if (lastActivity.containsKey(uuid)) {
                        long lastTime = lastActivity.get(uuid);

                        if (currentTime - lastTime > timeout && !afkPlayers.contains(uuid)) {
                            setAFK(player, true);
                        }
                    } else {
                        lastActivity.put(uuid, currentTime);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L * 10, 20L * 10); // 10秒ごとにチェック
    }

    public void updateActivity(Player player) {
        UUID uuid = player.getUniqueId();

        if (afkPlayers.contains(uuid)) {
            setAFK(player, false);
        }

        lastActivity.put(uuid, System.currentTimeMillis());
    }

    public void setAFK(Player player, boolean afk) {
        UUID uuid = player.getUniqueId();

        if (afk && !afkPlayers.contains(uuid)) {
            afkPlayers.add(uuid);
            String message = plugin.getConfig().getString("features.afk.message-afk",
                "&e{player} は現在AFK（離席中）です");
            message = message.replace("{player}", player.getName());
            String finalMessage = plugin.getChatManager().translateColorCodes(message);
            plugin.getServer().broadcast(net.kyori.adventure.text.Component.text(finalMessage));

        } else if (!afk && afkPlayers.contains(uuid)) {
            afkPlayers.remove(uuid);
            String message = plugin.getConfig().getString("features.afk.message-return",
                "&e{player} がAFK状態から戻りました");
            message = message.replace("{player}", player.getName());
            String finalMessage = plugin.getChatManager().translateColorCodes(message);
            plugin.getServer().broadcast(net.kyori.adventure.text.Component.text(finalMessage));
        }
    }

    public boolean isAFK(UUID playerUUID) {
        return afkPlayers.contains(playerUUID);
    }

    public void removePlayer(UUID playerUUID) {
        lastActivity.remove(playerUUID);
        afkPlayers.remove(playerUUID);
    }
}
