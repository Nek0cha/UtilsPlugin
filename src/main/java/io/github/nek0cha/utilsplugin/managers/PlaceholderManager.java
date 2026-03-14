package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PlaceholderManager {

    private final Utilsplugin plugin;
    // DateTimeFormatter はイミュータブルでスレッドセーフ
    private DateTimeFormatter timeFormat;
    private DateTimeFormatter dateFormat;
    private DateTimeFormatter hourFormat;
    private DateTimeFormatter minuteFormat;
    private DateTimeFormatter secondFormat;
    private DateTimeFormatter yearFormat;
    private DateTimeFormatter monthFormat;
    private DateTimeFormatter dayFormat;

    public PlaceholderManager(Utilsplugin plugin) {
        this.plugin = plugin;
        updateTimeZone();
    }

    public void updateTimeZone() {
        String timezone = plugin.getConfig().getString("timezone", "UTC");
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezone);
        } catch (Exception e) {
            plugin.getLogger().warning("無効なタイムゾーン設定: " + timezone + " - UTCを使用します");
            zoneId = ZoneId.of("UTC");
        }

        timeFormat   = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(zoneId);
        dateFormat   = DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(zoneId);
        hourFormat   = DateTimeFormatter.ofPattern("HH").withZone(zoneId);
        minuteFormat = DateTimeFormatter.ofPattern("mm").withZone(zoneId);
        secondFormat = DateTimeFormatter.ofPattern("ss").withZone(zoneId);
        yearFormat   = DateTimeFormatter.ofPattern("yyyy").withZone(zoneId);
        monthFormat  = DateTimeFormatter.ofPattern("MM").withZone(zoneId);
        dayFormat    = DateTimeFormatter.ofPattern("dd").withZone(zoneId);
    }

    public String replacePlaceholders(String text, org.bukkit.entity.Player player) {
        ZonedDateTime now = ZonedDateTime.now();

        // 時間関連
        text = text.replace("{time}",   timeFormat.format(now));
        text = text.replace("{date}",   dateFormat.format(now));
        text = text.replace("{hour}",   hourFormat.format(now));
        text = text.replace("{minute}", minuteFormat.format(now));
        text = text.replace("{second}", secondFormat.format(now));
        text = text.replace("{year}",   yearFormat.format(now));
        text = text.replace("{month}",  monthFormat.format(now));
        text = text.replace("{day}",    dayFormat.format(now));

        if (player != null) {
            // プレイヤー情報
            text = text.replace("{player}", player.getName());
            text = text.replace("{player_world}", player.getWorld().getName());
            text = text.replace("{player_x}", String.valueOf((int) player.getLocation().getX()));
            text = text.replace("{player_y}", String.valueOf((int) player.getLocation().getY()));
            text = text.replace("{player_z}", String.valueOf((int) player.getLocation().getZ()));
            text = text.replace("{player_ping}", String.valueOf(player.getPing()));
        }

        // サーバー情報
        text = text.replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        text = text.replace("{max}", String.valueOf(plugin.getServer().getMaxPlayers()));

        // TPS と MSPT
        double tps = getTPS();
        double mspt = getMSPT();
        text = text.replace("{tps}", String.format("%.2f", tps));
        text = text.replace("{mspt}", String.format("%.2f", mspt));

        // メモリ情報
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        text = text.replace("{max_memory}", String.valueOf(maxMemory));
        text = text.replace("{used_memory}", String.valueOf(usedMemory));

        return text;
    }

    private double getTPS() {
        double[] tpsArray = plugin.getServer().getTPS();
        return tpsArray.length > 0 ? tpsArray[0] : 20.0;
    }

    private double getMSPT() {
        return plugin.getServer().getAverageTickTime();
    }
}
