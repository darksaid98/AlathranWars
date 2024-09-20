package com.github.alathra.alathranwars.commands;

import com.github.alathra.alathranwars.AlathranWars;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;

public class CommandHandler {
    private final AlathranWars plugin;

    /**
     * Instantiates the Command handler.
     *
     * @param plugin the plugin
     */
    public CommandHandler(AlathranWars plugin) {
        this.plugin = plugin;
    }

    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin).shouldHookPaperReload(true).silentLogs(true));
    }

    public void onEnable() {
        CommandAPI.onEnable();

        // Register commands
        new WarCommands();
        new SiegeCommands();
        new AdminCommands();
    }

    public void onDisable() {
        CommandAPI.onDisable();
    }
}
