package io.github.nek0cha.utilsplugin;

import io.github.nek0cha.utilsplugin.commands.*;
import io.github.nek0cha.utilsplugin.listeners.*;
import io.github.nek0cha.utilsplugin.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Utilsplugin extends JavaPlugin {

    private static Utilsplugin instance;
    private ConfigManager configManager;
    private ChatManager chatManager;
    private MessageManager messageManager;
    private AFKManager afkManager;
    private PlaceholderManager placeholderManager;
    private MentionManager mentionManager;
    private AutoBroadcastManager autoBroadcastManager;
    private TabListManager tabListManager;
    private ScoreboardManager scoreboardManager;
    private URLClickManager urlClickManager;
    private NavigationManager navigationManager;

    @Override
    public void onEnable() {
        instance = this;

        // 設定ファイルの初期化
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        // マネージャーの初期化
        chatManager = new ChatManager(this);
        messageManager = new MessageManager(this);
        afkManager = new AFKManager(this);
        placeholderManager = new PlaceholderManager(this);
        mentionManager = new MentionManager(this);
        autoBroadcastManager = new AutoBroadcastManager(this);
        tabListManager = new TabListManager(this);
        scoreboardManager = new ScoreboardManager(this);
        urlClickManager = new URLClickManager(this);
        navigationManager = new NavigationManager(this);

        // マネージャーの開始
        tabListManager.start();
        scoreboardManager.start();

        // イベントリスナーの登録
        registerListeners();

        // コマンドの登録
        registerCommands();

        getLogger().info("Utilsplugin が有効化されました！");
    }

    @Override
    public void onDisable() {
        getLogger().info("Utilsplugin が無効化されました");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new NavigationListener(this), this);
    }

    private void registerCommands() {
        // プライベートメッセージコマンド
        if (getConfig().getBoolean("private-message.enabled", true)) {
            PrivateMessageCommand pmCmd = new PrivateMessageCommand(this);
            // 修正: getCommand() の結果が null の場合に備えて null チェックを追加
            if (getCommand("msg") != null) {
                getCommand("msg").setExecutor(pmCmd);
            }
            if (getCommand("tell") != null) {
                getCommand("tell").setExecutor(pmCmd);
            }
            if (getCommand("w") != null) {
                getCommand("w").setExecutor(pmCmd);
            }
            if (getCommand("reply") != null) {
                getCommand("reply").setExecutor(new ReplyCommand(this));
            }
            if (getCommand("r") != null) {
                getCommand("r").setExecutor(new ReplyCommand(this));
            }
        }

        // ブロードキャストコマンド
        if (getConfig().getBoolean("broadcast.enabled", true)) {
            // 修正: getCommand() の結果が null の場合に備えて null チェックを追加
            if (getCommand("broadcast") != null) {
                getCommand("broadcast").setExecutor(new BroadcastCommand(this));
            }
        }

        // ソーシャルスパイコマンド
        if (getConfig().getBoolean("socialspy.enabled", true)) {
            // 修正: getCommand() の結果が null の場合に備えて null チェックを追加
            if (getCommand("socialspy") != null) {
                getCommand("socialspy").setExecutor(new SocialSpyCommand(this));
            }
        }

        // チャットミュートコマンド
        if (getConfig().getBoolean("mutechat.enabled", true)) {
            // 修正: getCommand() の結果が null の場合に備えて null チェックを追加
            if (getCommand("mutechat") != null) {
                getCommand("mutechat").setExecutor(new MuteChatCommand(this));
            }
        }

        // AFKコマンド
        if (getConfig().getBoolean("features.afk.enabled", true)) {
            // 修正: getCommand() の結果が null の場合に備えて null チェックを追加
            if (getCommand("afk") != null) {
                getCommand("afk").setExecutor(new AFKCommand(this));
            }
        }

        // 飛行コマンド
        if (getCommand("fly") != null) {
            getCommand("fly").setExecutor(new FlyCommand(this));
        }

        // 回復コマンド
        if (getCommand("heal") != null) {
            getCommand("heal").setExecutor(new HealCommand(this));
        }

        // リロードコマンド
        if (getCommand("utilsplugin") != null) {
            getCommand("utilsplugin").setExecutor(new ReloadCommand(this));
        }

        // メニューコマンド
        if (getCommand("menu") != null) {
            getCommand("menu").setExecutor(new MenuCommand(this));
        }
    }

    public static Utilsplugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public AFKManager getAFKManager() {
        return afkManager;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public MentionManager getMentionManager() {
        return mentionManager;
    }

    public AutoBroadcastManager getAutoBroadcastManager() {
        return autoBroadcastManager;
    }

    public TabListManager getTabListManager() {
        return tabListManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public URLClickManager getURLClickManager() {
        return urlClickManager;
    }

    public NavigationManager getNavigationManager() {
        return navigationManager;
    }
}
