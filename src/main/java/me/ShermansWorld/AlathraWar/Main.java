package me.ShermansWorld.AlathraWar;

import me.ShermansWorld.AlathraWar.commands.CommandManager;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.hooks.HookManager;
import me.ShermansWorld.AlathraWar.items.WarItemRegistry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {
    public static Economy econ;
    public static AlathraWarLogger warLogger;
    private static Main instance;

    static {
        instance = null;
        econ = null;
    }

    private final CommandManager commandManager = new CommandManager(Main.getInstance());
    private final HookManager hookManager = new HookManager();

    @NotNull
    public static Main getInstance() {
        return instance;
    }

    /**
     * Dangerous if done at the wrong time!
     */
    public static void initData() {
        File userDataFolder = new File("plugins" + File.separator + "AlathraWar" + File.separator + "userdata");
        if (!userDataFolder.exists()) {
            userDataFolder.mkdirs();
        }

        WarData.setWars(WarData.createWars());
    }

    public static void initLogs() {
        File logsFolder = new File("plugins" + File.separator + "AlathraWar" + File.separator + "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
        File log = new File(
            "plugins" + File.separator + "AlathraWar" + File.separator + "logs" + File.separator + "log.txt");
        if (!log.exists()) {
            try {
                log.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[AlathraWar] Encountered error when creating log file!");
            }
        }
        warLogger = new AlathraWarLogger();
    }

    @SuppressWarnings("rawtypes")
    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        @SuppressWarnings("unchecked") final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>) this.getServer()
            .getServicesManager().getRegistration((Class) Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onLoad() {
        Main.instance = this;
        commandManager.onLoad();
        hookManager.onLoad();
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        initLogs();

        //new WarData(this);
//		new TimeoutData(this);

//        getServer().getPluginManager().registerEvents(new KillsListener(), this);
//        getServer().getPluginManager().registerEvents(new CommandsListener(), this);
//        getServer().getPluginManager().registerEvents(new JoinListener(), this);
//        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
//        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);

//		//run first
        new WarItemRegistry();
//		//run second
//		new WarRecipeRegistry();

        initData();
        setupEconomy();
        initLogs();

        commandManager.onEnable();
        hookManager.onEnable();
    }

    @Override
    public void onDisable() {
        commandManager.onDisable();
        hookManager.onDisable();

//        for (OldWar oldWar : WarData.getWars()) {
//            oldWar.save();
//        }
    }
}
