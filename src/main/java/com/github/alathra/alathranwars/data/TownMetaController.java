package com.github.alathra.alathranwars.data;

import com.github.alathra.alathranwars.AlathranWars;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.LocationDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import org.bukkit.Location;

public class TownMetaController {
    private AlathranWars plugin;
    private static LocationDataField defendersSpawn = new LocationDataField("alathranwars_defendersSpawn");

    TownMetaController(AlathranWars plugin) {
        this.plugin = plugin;
    }

    public static boolean hasDefendersSpawn(Town town) {
        return town.hasMeta(defendersSpawn.getKey());
    }

    public static Location getDefendersSpawn(Town town) {
        if (hasDefendersSpawn(town)) {
            return MetaDataUtil.getLocation(town, defendersSpawn);
        } else {
            Location location = town.getSpawnOrNull();

            setDefendersSpawn(town, location);

            return location;
        }
    }

    public static void setDefendersSpawn(Town town, Location location) {
        if (hasDefendersSpawn(town)) {
            MetaDataUtil.setLocation(town, defendersSpawn, location, true);
        } else {
            town.addMetaData(new LocationDataField(defendersSpawn.getKey(), location));
        }
    }
}
