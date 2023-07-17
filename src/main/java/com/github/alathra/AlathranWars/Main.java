package com.github.alathra.AlathranWars;

import com.github.alathra.AlathranWars.commands.CommandManager;
import com.github.alathra.AlathranWars.data.ConfigManager;
import com.github.alathra.AlathranWars.data.DataManager;
import com.github.alathra.AlathranWars.holder.WarManager;
import com.github.alathra.AlathranWars.hooks.HookManager;
import com.github.alathra.AlathranWars.items.WarItemRegistry;
import com.github.alathra.AlathranWars.listeners.ListenerHandler;
import com.github.alathra.AlathranWars.utility.SQLQueries;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Main extends JavaPlugin {
    public static Economy econ;
    public static AlathranWarsLogger warLogger;
    private static Main instance;

    static {
        instance = null;
        econ = null;
    }

    private ConfigManager configManager;
    private DataManager dataManager;
    private CommandManager commandManager;
    private ListenerHandler listenerHandler;
    private HookManager hookManager;

    @NotNull
    public static Main getInstance() {
        return instance;
    }

    /**
     * Dangerous if done at the wrong time!
     */
    /*public static void initData() {
        File userDataFolder = new File("plugins" + File.separator + "AlathranWars" + File.separator + "userdata");
        if (!userDataFolder.exists()) {
            userDataFolder.mkdirs();
        }

        WarData.setWars(WarData.createWars());
    }

    public static void initLogs() {
        File logsFolder = new File("plugins" + File.separator + "AlathranWars" + File.separator + "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
        File log = new File(
            "plugins" + File.separator + "AlathranWars" + File.separator + "logs" + File.separator + "log.txt");
        if (!log.exists()) {
            try {
                log.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[AlathranWars] Encountered error when creating log file!");
            }
        }
        warLogger = new AlathranWarsLogger();
    }*/
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
        WarManager.getInstance();
        configManager = new ConfigManager(getInstance());
        dataManager = new DataManager(getInstance());
        commandManager = new CommandManager(getInstance());
        listenerHandler = new ListenerHandler(getInstance());
        hookManager = new HookManager();

        configManager.onLoad();
        dataManager.onLoad();
        commandManager.onLoad();
        listenerHandler.onLoad();
        hookManager.onLoad();
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
//        initLogs();

        //new WarData(this);
//		new TimeoutData(this);

//		//run first
        new WarItemRegistry();
//		//run second
//		new WarRecipeRegistry();

//        initData();
        setupEconomy();
//        initLogs();

        configManager.onEnable();
        dataManager.onEnable();
        commandManager.onEnable();
        listenerHandler.onEnable();
        hookManager.onEnable();
        WarManager.getInstance().loadAll();
    }

    @Override
    public void onDisable() {
        SQLQueries.saveAll();
        configManager.onDisable();
        commandManager.onDisable();
        listenerHandler.onDisable();
        hookManager.onDisable();
        dataManager.onDisable();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
