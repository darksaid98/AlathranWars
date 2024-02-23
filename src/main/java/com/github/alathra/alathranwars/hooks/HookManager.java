package com.github.alathra.alathranwars.hooks;

import com.github.alathra.alathranwars.Reloadable;

public class HookManager implements Reloadable {
    private final PAPIHook papi = new PAPIHook();

    public void onLoad() {
        papi.onLoad();
    }

    public void onEnable() {
        TownyHook.init();
        papi.onEnable();
    }

    public void onDisable() {
        papi.onDisable();
    }
}
