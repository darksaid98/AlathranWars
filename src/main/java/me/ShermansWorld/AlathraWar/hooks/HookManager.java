package me.ShermansWorld.AlathraWar.hooks;

public class HookManager {
    public HookManager() {

    }

    public void onLoad() {


    }

    public void onEnable() {
        TABHook.init();
        TownyHook.init();
    }

    public void onDisable() {

    }
}
