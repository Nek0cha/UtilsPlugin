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

    // 重複行の一意化に使う、見た目に影響しない色コード
    private static final String[] UNIQUE_SUFFIXES = {
            "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7",
            "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f"
    };

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
        title = plugin.getChatManager().translateColorCodes(title);

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

        // 既存のエントリをすべてクリア
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        int maxLines = Math.min(lines.size(), 15);

        // すでに追加した“表示上の”行の出現回数を数える
        java.util.Map<String, Integer> seen = new java.util.HashMap<>();

        for (int i = 0; i < maxLines; i++) {
            String line = lines.get(i);
            line = plugin.getPlaceholderManager().replacePlaceholders(line, player);
            line = plugin.getChatManager().translateColorCodes(line);

            if (line.trim().isEmpty()) {
                // 空行は素直にスペースで一意化（見た目は空行のまま）
                line = " ".repeat(i + 1);
            }

            // 行の長さ制限（Minecraftの制限: 40文字）
            if (line.length() > 40) {
                line = line.substring(0, 40);
            }

            // 重複判定は “見た目の文字列” を基準にする（色コードも含めて同一なら重複扱い）
            int count = seen.getOrDefault(line, 0);
            seen.put(line, count + 1);

            String uniqueLine = line;
            if (count > 0) {
                // 末尾に不可視サフィックスを追加して“内部キー”だけ一意化。
                // ただし末尾で色が変わるのを防ぐため、元の最後の色を再付与する。
                String lastColors = org.bukkit.ChatColor.getLastColors(uniqueLine);
                String suffix = UNIQUE_SUFFIXES[(count - 1) % UNIQUE_SUFFIXES.length];
                uniqueLine = uniqueLine + suffix + lastColors;

                // 40文字制限に収まるよう最終調整
                if (uniqueLine.length() > 40) {
                    uniqueLine = uniqueLine.substring(0, 40);
                }
            }

            Score scoreEntry = objective.getScore(uniqueLine);
            scoreEntry.setScore(maxLines - i);
        }
    }

    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
    }
}
