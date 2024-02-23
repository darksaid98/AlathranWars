package com.github.alathra.alathranwars.conflict;

import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.jetbrains.annotations.Nullable;

public class Occupation {
    public static boolean isOccupied(Town town) {
        return town.isConquered();
    }

    public static void removeOccupied(Town town) {
        if (!isOccupied(town)) return;

//        final boolean hasPreviousNation = town.hasNation();
//        final Nation previousNation = town.getNationOrNull();
//        if (hasPreviousNation) {
//            town.removeNation();
//        }

        town.setConquered(false);

        town.save();
//        if (hasPreviousNation) {
//            previousNation.save();
//        }
    }

    public static void setOccupied(Nation occupied, @Nullable Nation occupier) {
        for (Town town : occupied.getTowns())
            setOccupied(town, occupier);
    }

    public static void setOccupied(Town town, @Nullable Nation occupier) {
        if (isOccupied(town)) return;

        // Remove from current nation
//        final boolean hasPreviousNation = town.hasNation();
//        final boolean hasNewNation = occupier != null;
//        final Nation previousNation = town.getNationOrNull();
//        if (hasPreviousNation && hasNewNation && previousNation != occupier)
//            town.removeNation();

//        // Put town into occupiers nation
//        try {
//            if (hasNewNation)
//                town.setNation(occupier);
//        } catch (AlreadyRegisteredException e) {
//            e.printStackTrace();
//            return;
//        }
        town.setConquered(true);

        town.save();
//        if (hasNewNation)
//            occupier.save();
//
//        if (hasPreviousNation && previousNation != occupier)
//            previousNation.save();
    }
}
