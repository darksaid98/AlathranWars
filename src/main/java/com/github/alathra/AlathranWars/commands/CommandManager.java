package com.github.alathra.AlathranWars.commands;

import com.github.alathra.AlathranWars.AlathranWars;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;

public class CommandManager {
    private final AlathranWars instance;

    public CommandManager(AlathranWars instance) {
        this.instance = instance;
    }

    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(instance).shouldHookPaperReload(true).silentLogs(true));
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
