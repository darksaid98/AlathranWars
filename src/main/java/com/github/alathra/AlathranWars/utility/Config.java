package com.github.alathra.AlathranWars.utility;

import com.github.alathra.AlathranWars.Main;

public class Config {
    public static de.leonhard.storage.Config get() {
        return Main.getInstance().getConfigManager().getConfig();
    }
}
