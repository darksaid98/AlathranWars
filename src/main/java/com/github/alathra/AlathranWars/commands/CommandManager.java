package com.github.alathra.AlathranWars.commands;

import com.github.alathra.AlathranWars.Main;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;

public class CommandManager {
    private final Main instance;

    public CommandManager(Main instance) {
        this.instance = instance;
    }

    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(instance).shouldHookPaperReload(true).silentLogs(true));
    }

    public void onEnable() {
        CommandAPI.onEnable();

        // Register commands
        new WarCommand();
        new SiegeCommand();
//        new RaidCommand();
        new AdminCommand();
    }

    public void onDisable() {
        CommandAPI.onDisable();
    }
}
