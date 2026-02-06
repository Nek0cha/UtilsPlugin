package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PlaceholderManager {

    private final Utilsplugin plugin;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat hourFormat;
    private SimpleDateFormat minuteFormat;
    private SimpleDateFormat secondFormat;
    private SimpleDateFormat yearFormat;
    private SimpleDateFormat monthFormat;
    private SimpleDateFormat dayFormat;

    public PlaceholderManager(Utilsplugin plugin) {
        this.plugin = plugin;
        updateTimeZone();
    }

    public void updateTimeZone() {
        String timezone = plugin.getConfig().getString("timezone", "UTC");
        TimeZone tz = TimeZone.getTimeZone(timezone);

        timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeFormat.setTimeZone(tz);

        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        dateFormat.setTimeZone(tz);

        hourFormat = new SimpleDateFormat("HH");
        hourFormat.setTimeZone(tz);

        minuteFormat = new SimpleDateFormat("mm");
        minuteFormat.setTimeZone(tz);

        secondFormat = new SimpleDateFormat("ss");
        secondFormat.setTimeZone(tz);

        yearFormat = new SimpleDateFormat("yyyy");
        yearFormat.setTimeZone(tz);

        monthFormat = new SimpleDateFormat("MM");
        monthFormat.setTimeZone(tz);

        dayFormat = new SimpleDateFormat("dd");
        dayFormat.setTimeZone(tz);
    }

    public String replacePlaceholders(String text, org.bukkit.entity.Player player) {
        Date now = new Date();

        // 時間関連
        text = text.replace("{time}", timeFormat.format(now));
        text = text.replace("{date}", dateFormat.format(now));
        text = text.replace("{hour}", hourFormat.format(now));
        text = text.replace("{minute}", minuteFormat.format(now));
        text = text.replace("{second}", secondFormat.format(now));
        text = text.replace("{year}", yearFormat.format(now));
        text = text.replace("{month}", monthFormat.format(now));
        text = text.replace("{day}", dayFormat.format(now));

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
        try {
            Object server = plugin.getServer().getClass().getMethod("getServer").invoke(plugin.getServer());
            Object tickTimes = server.getClass().getField("recentTps").get(server);
            return ((double[]) tickTimes)[0];
        } catch (Exception e) {
            return 20.0;
        }
    }

    private double getMSPT() {
        try {
            Object server = plugin.getServer().getClass().getMethod("getServer").invoke(plugin.getServer());
            long[] tickTimes = (long[]) server.getClass().getMethod("getTickTimes").invoke(server);
            long sum = 0;
            for (long time : tickTimes) {
                sum += time;
            }
            return (sum / (double) tickTimes.length) / 1000000.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
