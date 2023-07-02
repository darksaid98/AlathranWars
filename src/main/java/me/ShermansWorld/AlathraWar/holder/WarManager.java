package me.ShermansWorld.AlathraWar.holder;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.conflict.War;
import me.ShermansWorld.AlathraWar.conflict.battle.Raid;
import me.ShermansWorld.AlathraWar.conflict.battle.Siege;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.util.*;

@Singleton
public class WarManager {
    private static WarManager instance;
    private final Set<War> wars = new HashSet<>();

    private WarManager() {
        if (instance != null)
            Bukkit.getServer().getLogger().warning("Tried to re-initialize singleton");
    }

    @NotNull
    public static WarManager getInstance() {
        if (instance == null) new WarManager();
        return instance;
    }

    @NotNull
    public Set<War> getWars() {
        return wars;
    }

    @Nullable
    public War getWar(String warName) {
        for (War war : wars)
            if (war.equals(warName)) return war;

        return null;
    }

    @Nullable
    public War getWar(UUID uuid) {
        for (War war : wars)
            if (war.equals(uuid)) return war;

        return null;
    }

    public void addWar(War war) {
        wars.add(war);
    }

    public void removeWar(War war) {
        wars.remove(war);
    }

    public boolean isPlayerInAnyWars(UUID uuid) {
        for (War war : wars) {
            if (war.isPlayerInWar(uuid)) return true;
        };
        return false;
    }

    public boolean isPlayerInAnyWars(Player p) {
        for (War war : wars) {
            if (war.isPlayerInWar(p)) return true;
        };
        return false;
    }

    public boolean isTownInAnyWars(Town town) {
        for (War war : wars) {
            if (war.isTownInWar(town)) return true;
        };
        return false;
    }

    public boolean isNationInAnyWars(Nation nation) {
        for (War war : wars) {
            if (war.isNationInWar(nation)) return true;
        };
        return false;
    }

    @NotNull
    public List<String> getWarNames() {
        final List<String> names = new ArrayList<>();
        wars.forEach((War war) -> names.add(war.getName()));
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    @NotNull
    public List<String> getWarLabels() {
        final List<String> labels = new ArrayList<>();
        wars.forEach((War war) -> labels.add(war.getLabel()));
        labels.sort(String::compareToIgnoreCase);
        return labels;
    }

    @NotNull
    public Set<Siege> getSieges() {
        final Set<Siege> sieges = new HashSet<>();

        wars.forEach((War war) -> sieges.addAll(war.getSieges()));

        return sieges;
    }

    @NotNull
    public Set<Raid> getRaids() {
        final Set<Raid> raids = new HashSet<>();

        wars.forEach((War war) -> raids.addAll(war.getRaids()));

        return raids;
    }
}
