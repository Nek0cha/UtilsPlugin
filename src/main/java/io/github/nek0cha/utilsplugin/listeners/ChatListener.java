package io.github.nek0cha.utilsplugin.listeners;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final Utilsplugin plugin;

    public ChatListener(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // AFKステータスの更新
        if (plugin.getConfig().getBoolean("features.afk.enabled", true)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getAFKManager().updateActivity(player);
            });
        }

        // チャットミュートチェック
        if (plugin.getChatManager().isChatMuted() && !player.hasPermission("utilsplugin.mutechat.bypass")) {
            event.setCancelled(true);
            String message = plugin.getConfig().getString("mutechat.muted-message", "&cチャットは現在ミュートされています");
            player.sendMessage(plugin.getChatManager().translateToComponent(message));
            return;
        }

        // チャットフォーマットの適用
        if (plugin.getConfig().getBoolean("chat.enabled", true)) {
            event.setCancelled(true);

            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            String formattedMessage = plugin.getChatManager().formatChatMessage(player, message);

            if (formattedMessage != null) {
                // URLClickManagerを使用してクリック可能なURLを処理
                TextComponent clickableMessage = plugin.getURLClickManager().processMessage(formattedMessage);

                // TextComponentからAdventure Componentに変換してブロードキャスト
                Component adventureComponent = plugin.getChatManager().translateToComponent(clickableMessage.toLegacyText());
                plugin.getServer().broadcast(adventureComponent);
            }
        }
    }
}
