package com.github.alathra.alathranwars;

import com.github.alathra.alathranwars.commands.CommandHandler;
import com.github.alathra.alathranwars.config.ConfigHandler;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.database.DatabaseQueries;
import com.github.alathra.alathranwars.database.handler.DatabaseHandler;
import com.github.alathra.alathranwars.hook.*;
import com.github.alathra.alathranwars.listeners.ListenerHandler;
import com.github.alathra.alathranwars.translation.TranslationManager;
import com.github.alathra.alathranwars.updatechecker.UpdateChecker;
import com.github.alathra.alathranwars.utility.Logger;
import com.github.milkdrinkers.colorparser.ColorParser;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.MorePaperLib;

public class AlathranWars extends JavaPlugin {
    private static MorePaperLib paperLib;

    private static AlathranWars instance;
    private ConfigHandler configHandler;
    private TranslationManager translationManager;
    private DatabaseHandler databaseHandler;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;
    private UpdateChecker updateChecker;

    // Hooks
    private static BStatsHook bStatsHook;
    private static VaultHook vaultHook;
    private static ProtocolLibHook protocolLibHook;
    private static PAPIHook papiHook;

    public void onLoad() {
        instance = this;
        paperLib = new MorePaperLib(instance);
        WarController.getInstance();
        configHandler = new ConfigHandler(instance);
        translationManager = new TranslationManager(instance);
        databaseHandler = new DatabaseHandler(configHandler, getComponentLogger());
        commandHandler = new CommandHandler(instance);
        listenerHandler = new ListenerHandler(instance);
        updateChecker = new UpdateChecker();
        bStatsHook = new BStatsHook(instance);
        vaultHook = new VaultHook(instance);
        protocolLibHook = new ProtocolLibHook(instance);
        papiHook = new PAPIHook(instance);

        configHandler.onLoad();
        translationManager.onLoad();
        databaseHandler.onLoad();
        commandHandler.onLoad();
        listenerHandler.onLoad();
        updateChecker.onLoad();
        bStatsHook.onLoad();
        vaultHook.onLoad();
        protocolLibHook.onLoad();
        papiHook.onLoad();
    }

    public void onEnable() {
        configHandler.onEnable();
        translationManager.onEnable();
        databaseHandler.onEnable();
        commandHandler.onEnable();
        listenerHandler.onEnable();
        updateChecker.onEnable();
        bStatsHook.onEnable();
        vaultHook.onEnable();
        protocolLibHook.onEnable();
        papiHook.onEnable();

        if (!databaseHandler.isRunning()) {
            Logger.get().warn(ColorParser.of("<yellow>Database handler failed to start. Database support has been disabled.").build());
        }

        if (vaultHook.isVaultLoaded()) {
            Logger.get().info(ColorParser.of("<green>Vault has been found on this server. Vault support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>Vault is not installed on this server. Vault support has been disabled.").build());
        }

        if (protocolLibHook.isHookLoaded()) {
            Logger.get().info(ColorParser.of("<green>ProtocolLib has been found on this server. ProtocolLib support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>ProtocolLib is not installed on this server. ProtocolLib support has been disabled.").build());
        }

        WarController.getInstance().loadAll();
    }

    public void onDisable() {
        getPaperLib().scheduling().cancelGlobalTasks();
        DatabaseQueries.saveAll();

        configHandler.onDisable();
        translationManager.onDisable();
        databaseHandler.onDisable();
        commandHandler.onDisable();
        listenerHandler.onDisable();
        updateChecker.onDisable();
        bStatsHook.onDisable();
        vaultHook.onDisable();
        protocolLibHook.onDisable();
        papiHook.onDisable();
    }

    /**
     * Gets plugin instance.
     *
     * @return the plugin instance
     */
    @NotNull
    public static AlathranWars getInstance() {
        return instance;
    }

    /**
     * Gets more paper lib instance.
     *
     * @return the more paper lib instance
     */
    public static MorePaperLib getPaperLib() {
        return paperLib;
    }

    /**
     * Gets data handler.
     *
     * @return the data handler
     */
    @NotNull
    public DatabaseHandler getDataHandler() {
        return databaseHandler;
    }

    /**
     * Gets config handler.
     *
     * @return the config handler
     */
    @NotNull
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    /**
     * Gets config handler.
     *
     * @return the translation handler
     */
    @NotNull
    public TranslationManager getTranslationManager() {
        return translationManager;
    }

    /**
     * Gets update checker.
     *
     * @return the update checker
     */
    @NotNull
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    /**
     * Gets bStats hook.
     *
     * @return the bStats hook
     */
    @NotNull
    public static BStatsHook getBStatsHook() {
        return bStatsHook;
    }

    /**
     * Gets vault hook.
     *
     * @return the vault hook
     */
    @NotNull
    public static VaultHook getVaultHook() {
        return vaultHook;
    }

    /**
     * Gets ProtocolLib hook.
     *
     * @return the ProtocolLib hook
     */
    @NotNull
    public static ProtocolLibHook getProtocolLibHook() {
        return protocolLibHook;
    }
}
