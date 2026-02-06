package io.github.nek0cha.utilsplugin.listeners;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final Utilsplugin plugin;

    public PlayerListener(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getConfig().getBoolean("features.join-quit-messages.enabled", true)) {
            String format = plugin.getConfig().getString("features.join-quit-messages.join-format",
                "&a+ &7{player} がサーバーに参加しました");
            format = format.replace("{player}", event.getPlayer().getName());
            event.joinMessage(plugin.getChatManager().translateToComponent(format));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // AFKマネージャーから削除
        plugin.getAFKManager().removePlayer(event.getPlayer().getUniqueId());

        // スコアボードの削除
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());

        if (plugin.getConfig().getBoolean("features.join-quit-messages.enabled", true)) {
            String format = plugin.getConfig().getString("features.join-quit-messages.quit-format",
                "&c- &7{player} がサーバーから退出しました");
            format = format.replace("{player}", event.getPlayer().getName());
            event.quitMessage(plugin.getChatManager().translateToComponent(format));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (plugin.getConfig().getBoolean("features.afk.enabled", true)) {
            // 位置が実際に変わった場合のみAFKステータスを更新
            if (event.hasChangedPosition()) {
                plugin.getAFKManager().updateActivity(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (plugin.getConfig().getBoolean("features.afk.enabled", true)) {
            plugin.getAFKManager().updateActivity(event.getPlayer());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (plugin.getConfig().getBoolean("features.death-messages.enabled", true)) {
            if (plugin.getConfig().getBoolean("features.death-messages.custom", false)) {
                String format = plugin.getConfig().getString("features.death-messages.format",
                    "&c☠ &7{message}");
                String deathMessage = event.deathMessage() != null ?
                    net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.deathMessage())
                    : event.getPlayer().getName() + " が死亡しました";
                format = format.replace("{message}", deathMessage);
                format = plugin.getChatManager().translateColorCodes(format);
                event.deathMessage(net.kyori.adventure.text.Component.text(format));
            }
        }
    }
}
