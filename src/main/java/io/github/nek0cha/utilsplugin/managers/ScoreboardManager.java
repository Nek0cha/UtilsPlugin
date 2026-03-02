package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
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

        String title = plugin.getConfig().getString("scoreboard.title", "&6&lサーバー情報");

        Objective objective = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY,
            plugin.getChatManager().translateToComponent(title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);


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

        Objective objective = scoreboard.getObjective("sidebar");
        if (objective == null) {
            createScoreboard(player);
            return;
        }

        // タイトルを毎回更新（設定変更後も反映されるように）
        String title = plugin.getConfig().getString("scoreboard.title", "&6&lサーバー情報");
        title = plugin.getPlaceholderManager().replacePlaceholders(title, player);
        objective.displayName(plugin.getChatManager().translateToComponent(title));

        // 既存のエントリをすべてクリア
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        int maxLines = Math.min(lines.size(), 15);

        for (int i = 0; i < maxLines; i++) {
            String line = lines.get(i);
            line = plugin.getPlaceholderManager().replacePlaceholders(line, player);

            // Adventure ComponentとしてHEXカラーコードを含む行を変換
            net.kyori.adventure.text.Component lineComponent;
            if (line.trim().isEmpty()) {
                // 空行は一意なスペース
                lineComponent = net.kyori.adventure.text.Component.text(" ".repeat(i + 1));
            } else {
                lineComponent = plugin.getChatManager().translateToComponent(line);
            }

            // エントリ名は行インデックスベースの一意な識別子
            // 表示はcustomName(Component)でHEXカラーを含むComponentを使用
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
