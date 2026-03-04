package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NavigationManager {
    private final Utilsplugin plugin;

    public NavigationManager(Utilsplugin plugin) {
        this.plugin = plugin;

        // BungeeCordメッセージチャンネルの登録
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    }

    public void openNavigationMenu(Player player) {
        ConfigurationSection navConfig = plugin.getConfig().getConfigurationSection("navigation");
        if (navConfig == null) return;

        String titleConfig = navConfig.getString("menu.title", "&6&lナビゲーションメニュー");
        Component titleComponent = plugin.getChatManager().translateToComponent(titleConfig);
        int size = navConfig.getInt("menu.size", 27);

        Inventory inv = Bukkit.createInventory(null, size, titleComponent);

        // ワールドメニュー項目
        ConfigurationSection worlds = navConfig.getConfigurationSection("worlds");
        if (worlds != null) {
            for (String key : worlds.getKeys(false)) {
                ConfigurationSection world = worlds.getConfigurationSection(key);
                if (world == null) continue;

                int slot = world.getInt("slot", 0);
                ItemStack item = createMenuItem(world);
                inv.setItem(slot, item);
            }
        }

        // サーバーメニュー項目
        ConfigurationSection servers = navConfig.getConfigurationSection("servers");
        if (servers != null) {
            for (String key : servers.getKeys(false)) {
                ConfigurationSection server = servers.getConfigurationSection(key);
                if (server == null) continue;

                int slot = server.getInt("slot", 0);
                ItemStack item = createMenuItem(server);
                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    private ItemStack createMenuItem(ConfigurationSection config) {
        Material material = Material.matchMaterial(
            config.getString("material", "GRASS_BLOCK")
        );
        if (material == null) material = Material.GRASS_BLOCK;

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String nameConfig = config.getString("name", "アイテム");
            Component nameComponent = plugin.getChatManager().translateToComponent(nameConfig);
            meta.displayName(nameComponent);

            List<Component> lore = new ArrayList<>();
            List<String> configLore = config.getStringList("lore");
            for (String line : configLore) {
                lore.add(plugin.getChatManager().translateToComponent(line));
            }
            meta.lore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    public void handleNavigation(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        ConfigurationSection navConfig = plugin.getConfig().getConfigurationSection("navigation");
        if (navConfig == null) return;

        // ワールド移動チェック
        ConfigurationSection worlds = navConfig.getConfigurationSection("worlds");
        if (worlds != null) {
            for (String key : worlds.getKeys(false)) {
                ConfigurationSection world = worlds.getConfigurationSection(key);
                if (world == null) continue;

                String name = PlainTextComponentSerializer.plainText().serialize(
                    plugin.getChatManager().translateToComponent(world.getString("name", ""))
                );

                if (name.equals(displayName)) {
                    String worldName = world.getString("world");
                    if (worldName != null && Bukkit.getWorld(worldName) != null) {
                        player.teleport(Bukkit.getWorld(worldName).getSpawnLocation());
                        player.sendMessage(ChatColor.GREEN + worldName + " にテレポートしました！");
                        player.closeInventory();
                    }
                    return;
                }
            }
        }

        // サーバー移動チェック
        ConfigurationSection servers = navConfig.getConfigurationSection("servers");
        if (servers != null) {
            for (String key : servers.getKeys(false)) {
                ConfigurationSection server = servers.getConfigurationSection(key);
                if (server == null) continue;

                String name = PlainTextComponentSerializer.plainText().serialize(
                    plugin.getChatManager().translateToComponent(server.getString("name", ""))
                );

                if (name.equals(displayName)) {
                    String serverName = server.getString("server");
                    if (serverName != null) {
                        // アドレスとポートが指定されている場合
                        if (server.contains("address") && server.contains("port")) {
                            String address = server.getString("address");
                            int port = server.getInt("port", 25565);
                            connectToServer(player, serverName, address, port);
                        } else {
                            // 通常のサーバー名のみの接続
                            connectToServer(player, serverName);
                        }
                        player.closeInventory();
                    }
                    return;
                }
            }
        }
    }

    private void connectToServer(Player player, String serverName) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "サーバーへの接続に失敗しました");
            e.printStackTrace();
        }
    }

    // 修正: サーバー移動機能にアドレスとポートを指定するオプションを追加
    public void connectToServer(Player player, String serverName, String address, int port) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("ConnectOther");
            out.writeUTF(player.getName());
            out.writeUTF(serverName);
            out.writeUTF(address);
            out.writeInt(port);
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "サーバーへの接続に失敗しました");
            e.printStackTrace();
        }
    }
}
