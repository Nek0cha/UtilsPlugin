package io.github.nek0cha.utilsplugin.listeners;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NavigationListener implements Listener {
    private final Utilsplugin plugin;

    public NavigationListener(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;
        if (!item.hasItemMeta()) return;

        String compassName = plugin.getConfig().getString("navigation.compass.name");
        if (compassName == null) return;

        String itemName = "";
        // アイテムの表示名を取得（プレーンテキストに変換）
        if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                itemName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
            }
        }

        // 設定ファイルの名前をカラーコード変換してからプレーンテキストに変換
        String configName = PlainTextComponentSerializer.plainText().serialize(
            plugin.getChatManager().translateToComponent(compassName)
        );

        if (itemName.equals(configName)) {
            event.setCancelled(true);
            plugin.getNavigationManager().openNavigationMenu(event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // インベントリのタイトルを取得（プレーンテキストに変換）
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // 設定ファイルのメニュータイトルをカラーコード変換してからプレーンテキストに変換
        String menuTitle = PlainTextComponentSerializer.plainText().serialize(
            plugin.getChatManager().translateToComponent(
                plugin.getConfig().getString("navigation.menu.title", "&6&lメニュー")
            )
        );

        if (title.equals(menuTitle)) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR) {
                plugin.getNavigationManager().handleNavigation(
                    (Player) event.getWhoClicked(), clicked
                );
            }
        }
    }
}
