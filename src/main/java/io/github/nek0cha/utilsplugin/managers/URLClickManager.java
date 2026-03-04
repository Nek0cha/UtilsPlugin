package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLClickManager {
    private final Utilsplugin plugin;
    private final Pattern urlPattern;

    public URLClickManager(Utilsplugin plugin) {
        this.plugin = plugin;
        this.urlPattern = Pattern.compile(
            "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
            Pattern.CASE_INSENSITIVE
        );
    }

    public TextComponent processMessage(String message) {
        TextComponent result = new TextComponent();
        Matcher matcher = urlPattern.matcher(message);
        int lastEnd = 0;

        while (matcher.find()) {
            // URL前のテキスト
            if (matcher.start() > lastEnd) {
                result.addExtra(message.substring(lastEnd, matcher.start()));
            }

            // URLをクリック可能に
            String url = matcher.group();
            TextComponent urlComponent = new TextComponent(url);
            urlComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            urlComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("クリックしてURLを開く\n" + url).create()
            ));

            result.addExtra(urlComponent);
            lastEnd = matcher.end();
        }

        // 残りのテキスト
        if (lastEnd < message.length()) {
            result.addExtra(message.substring(lastEnd));
        }

        return result;
    }
}
