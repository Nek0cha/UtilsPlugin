package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final Utilsplugin plugin;
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();

    public ScoreboardManager(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            return;
        }

        // すべてのオンラインプレイヤーにスコアボードを表示
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            createScoreboard(player);
        }

        int updateInterval = plugin.getConfig().getInt("scoreboard.update-interval", 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
                    cancel();
                    return;
                }

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 20L, updateInterval);
    }

    public void createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard);
        playerScoreboards.put(player.getUniqueId(), scoreboard);
        updateScoreboard(player);
    }

    private void updateScoreboard(Player player) {
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            createScoreboard(player);
            return;
        }

        // タイトルをHEXカラー込みで生成
        String titleRaw = plugin.getConfig().getString("scoreboard.title", "&6&lサーバー情報");
        titleRaw = plugin.getPlaceholderManager().replacePlaceholders(titleRaw, player);
        Component titleComponent = plugin.getChatManager().translateToComponent(titleRaw);

        // Objectiveを毎回再作成してタイトル変更を確実に反映
        Objective old = scoreboard.getObjective("sidebar");
        if (old != null) {
            old.unregister();
        }
        Objective objective = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, titleComponent);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        int maxLines = Math.min(lines.size(), 15);

        for (int i = 0; i < maxLines; i++) {
            String line = lines.get(i);
            line = plugin.getPlaceholderManager().replacePlaceholders(line, player);

            Component lineComponent;
            if (line.trim().isEmpty()) {
                // 空行は一意なスペース（インデックス分のスペース）
                lineComponent = Component.text(" ".repeat(i + 1));
            } else {
                lineComponent = plugin.getChatManager().translateToComponent(line);
            }

            // エントリキーは一意な識別子
            String entryKey = "line_" + i;
            Score scoreEntry = objective.getScore(entryKey);
            scoreEntry.customName(lineComponent);
            scoreEntry.setScore(maxLines - i);
        }
    }

    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
    }
}
