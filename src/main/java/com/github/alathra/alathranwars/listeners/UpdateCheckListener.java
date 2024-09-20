package com.github.alathra.alathranwars.listeners;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.translation.Translation;
import com.github.alathra.alathranwars.updatechecker.SemanticVersion;
import com.github.alathra.alathranwars.updatechecker.UpdateChecker;
import com.github.alathra.alathranwars.utility.Cfg;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Sends an update message to operators if there's a plugin update available
 */
public class UpdateCheckListener implements Listener {
    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (AlathranWars.getInstance().getUpdateChecker().isLatest())
            return;

        if (!Cfg.get().getOrDefault("update-checker.enable", true) || !Cfg.get().getOrDefault("update-checker.op", true))
            return;

        if (!e.getPlayer().isOp())
            return;

        String pluginName = AlathranWars.getInstance().getUpdateChecker().getPluginName();
        SemanticVersion latestVersion = AlathranWars.getInstance().getUpdateChecker().getLatestVersion();
        SemanticVersion currentVersion = AlathranWars.getInstance().getUpdateChecker().getCurrentVersion();

        if (latestVersion == null || currentVersion == null)
            return;

        e.getPlayer().sendMessage(
            ColorParser.of(Translation.of("update-checker.update-found-player"))
                .parseMinimessagePlaceholder("plugin_name", pluginName)
                .parseMinimessagePlaceholder("version_current", currentVersion.getVersionFull())
                .parseMinimessagePlaceholder("version_latest", latestVersion.getVersionFull())
                .parseMinimessagePlaceholder("download_link", UpdateChecker.LATEST_RELEASE)
                .build()
        );
    }
}
