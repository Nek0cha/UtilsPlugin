package io.github.nek0cha.utilsplugin.commands;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;

public class MenuCommand implements CommandExecutor {

    private final Utilsplugin plugin;

    public MenuCommand(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみが使用できます。", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("/menu get - ナビゲーションメニューアイテムを取得します。", NamedTextColor.YELLOW));
            return true;
        }

        if (args[0].equalsIgnoreCase("get")) {
            giveNavigationCompass(player);
            return true;
        }

        player.sendMessage(Component.text("不明なコマンドです。/menu を使用してヘルプを確認してください。", NamedTextColor.RED));
        return true;
    }

    private void giveNavigationCompass(Player player) {
        String compassName = plugin.getConfig().getString("navigation.compass.name", "&6&lメニュー");
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {
            // カラーコードを変換してComponentに設定
            Component displayName = plugin.getChatManager().translateToComponent(compassName);
            meta.displayName(displayName);
            compass.setItemMeta(meta);
        }

        player.getInventory().addItem(compass);
        player.sendMessage(Component.text("ナビゲーションコンパスを受け取りました！", NamedTextColor.GREEN));
    }
}
