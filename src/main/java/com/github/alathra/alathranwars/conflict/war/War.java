package com.github.alathra.alathranwars.conflict.war;

import com.github.alathra.alathranwars.conflict.Conflict;
import com.github.alathra.alathranwars.conflict.Occupation;
import com.github.alathra.alathranwars.conflict.battle.raid.Raid;
import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.conflict.war.side.SideBuilder;
import com.github.alathra.alathranwars.conflict.war.side.SideCreationException;
import com.github.alathra.alathranwars.database.DatabaseQueries;
import com.github.alathra.alathranwars.enums.ConflictType;
import com.github.alathra.alathranwars.enums.WarDeleteReason;
import com.github.alathra.alathranwars.enums.battle.BattleSide;
import com.github.alathra.alathranwars.enums.battle.BattleTeam;
import com.github.alathra.alathranwars.enums.battle.BattleVictoryReason;
import com.github.alathra.alathranwars.events.PreWarCreateEvent;
import com.github.alathra.alathranwars.events.PreWarDeleteEvent;
import com.github.alathra.alathranwars.events.WarCreateEvent;
import com.github.alathra.alathranwars.events.WarDeleteEvent;
import com.github.alathra.alathranwars.hook.NameColorHandler;
import com.github.alathra.alathranwars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type New war.
 */
public class War extends Conflict {
    private final String name; // Used in commands and such, like "TidalHaven.vs.Meme"
    private final String label;
    private final ConflictType conflictType = ConflictType.WAR;
    private boolean event;

    private Side side1;
    private Side side2;
    private final Side attacker; // Reference variable to side1 or side2
    private final Side defender; // Reference variable to side1 or side2

    private Set<Siege> sieges = new HashSet<>();
    private Set<Raid> raids = new HashSet<>();

    /**
     * Instantiates a existing War.
     *
     * @param uuid   the uuid
     * @param name   the name
     * @param label  the label
     * @param side1  the side 1
     * @param side2  the side 2
     * @param sieges the sieges
     * @param raids  the raids
     * @param event  the event
     */
    @ApiStatus.Internal
    War(
        UUID uuid,
        String name,
        String label,
        Side side1,
        Side side2,
        Set<Siege> sieges,
        Set<Raid> raids,
        boolean event
    ) {
        super(uuid);
        this.name = name;
        this.label = label;

        this.side1 = side1;
        this.side2 = side2;

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

        this.sieges = sieges;
        this.raids = raids;
        this.event = event;

        resume();
    }

    /**
     * Instantiates a new War.
     *
     * @param uuid      the uuid
     * @param label     the label
     * @param aggressor the aggressor
     * @param victim    the victim
     * @param event     the event
     * @throws SideCreationException         the side creation exception
     * @throws WrapperCommandSyntaxException the wrapper command syntax exception
     */
    @ApiStatus.Internal
    War(
        UUID uuid,
        String label,
        Government aggressor,
        Government victim,
        boolean event
    ) throws SideCreationException, WrapperCommandSyntaxException {
        super(uuid);
        this.label = label;

        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        if (WarController.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        this.side1 = new SideBuilder()
            .setWarUUID(getUUID())
            .setUuid(UUID.randomUUID())
            .setLeader(aggressor)
            .setSide(BattleSide.ATTACKER)
            .setTeam(BattleTeam.SIDE_1)
            .build();
        this.side2 = new SideBuilder()
            .setWarUUID(getUUID())
            .setUuid(UUID.randomUUID())
            .setLeader(victim)
            .setSide(BattleSide.DEFENDER)
            .setTeam(BattleTeam.SIDE_2)
            .build();

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;
        this.event = event;

        if (!new PreWarCreateEvent(this).callEvent())
            return;

        // TODO All logic below this point should be separated out
        WarController.getInstance().addWar(this);

        start();

        new WarCreateEvent(this).callEvent();
    }

    /**
     * Gets name of the war.
     *
     * @return the name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets label of the war.
     *
     * @return the label
     */
    @NotNull
    public String getLabel() {
        return label;
    }

    /**
     * Compare war by names.
     *
     * @param warName the war name
     * @return the boolean
     */
    public boolean equals(String warName) {
        return this.name.equals(warName);
    }

    /**
     * Gets side 1.
     *
     * @return the side 1
     */
    @NotNull
    public Side getSide1() {
        return side1;
    }

    /**
     * Gets side 2.
     *
     * @return the side 2
     */
    @NotNull
    public Side getSide2() {
        return side2;
    }

    /**
     * Gets sides in a list.
     *
     * @return the sides
     */
    @NotNull
    public Set<Side> getSides() {
        return Set.of(
            getSide1(),
            getSide2()
        );
    }

    /**
     * Gets attacker side.
     *
     * @return the attacker
     */
    @NotNull
    public Side getAttacker() {
        return attacker;
    }

    /**
     * Gets defender side.
     *
     * @return the defender
     */
    @NotNull
    public Side getDefender() {
        return defender;
    }

    /**
     * Gets side by name.
     *
     * @param name the name
     * @return the side
     */
    @Nullable
    public Side getSide(String name) {
        if (getSide1().equals(name))
            return getSide1();

        if (getSide2().equals(name))
            return getSide2();

        return null;
    }

    /**
     * Gets side by the side UUID.
     *
     * @param uuid the uuid
     * @return the side
     */
    @Nullable
    public Side getSide(UUID uuid) {
        if (getSide1().equals(uuid))
            return getSide1();

        if (getSide2().equals(uuid))
            return getSide2();

        return null;
    }

    // SECTION Battles

    /**
     * Gets sieges in a list.
     *
     * @return the sieges
     */
    @NotNull
    public Set<Siege> getSieges() {
        return sieges;
    }

    /**
     * Sets sieges for the war.
     *
     * @param sieges the sieges
     */
    @ApiStatus.Internal
    public void setSieges(Set<Siege> sieges) {
        this.sieges = sieges;
        this.sieges.forEach(Siege::resume);
    }

    /**
     * Gets siege by the siege UUID.
     *
     * @param uuid the uuid
     * @return the siege
     */
    @Nullable
    public Siege getSiege(UUID uuid) {
        return sieges.stream()
            .filter(s -> s.equals(uuid))
            .findAny()
            .orElse(null);
    }

    /**
     * Add siege to war.
     *
     * @param siege the siege
     */
    @ApiStatus.Internal
    public void addSiege(Siege siege) {
        sieges.add(siege);
    }

    /**
     * Remove siege from war.
     *
     * @param siege the siege
     */
    @ApiStatus.Internal
    public void removeSiege(Siege siege) {
        sieges.remove(siege);
    }

    /**
     * Gets raids.
     *
     * @return the raids
     */
    @NotNull
    public Set<Raid> getRaids() {
        return raids;
    }

    /**
     * Sets raids for the war.
     *
     * @param raids the raids
     */
    @ApiStatus.Internal
    public void setRaids(Set<Raid> raids) {
        this.raids = raids;
        // TODO Raids
//        this.raids.forEach(Raid::resume);
    }

    /**
     * Gets raid by the raid UUID.
     *
     * @param uuid the uuid
     * @return the raid
     */
    @Nullable
    public Raid getRaid(UUID uuid) {
        return raids.stream()
            .filter(s -> s.equals(uuid))
            .findAny()
            .orElse(null);
    }

    /**
     * Add raid to war.
     *
     * @param raid the raid
     */
    @ApiStatus.Internal
    public void addRaid(Raid raid) {
        raids.add(raid);
    }

    /**
     * Remove raid from war.
     *
     * @param raid the raid
     */
    @ApiStatus.Internal
    public void removeRaid(Raid raid) {
        raids.remove(raid);
    }

    /**
     * Is town under siege in this war.
     *
     * @param town the town
     * @return the boolean
     */
    public boolean isTownUnderSiege(Town town) {
        return sieges.stream().anyMatch(siege -> siege.getTown().equals(town));
    }

    /**
     * Is town under raid in this war.
     *
     * @param town the town
     * @return the boolean
     */
    public boolean isTownUnderRaid(Town town) {
        // TODO Raids
        return false;
//        return raids.stream().anyMatch(raid -> raid.getTown().equals(town));
    }

    // SECITON Nation & Town Getters

    /**
     * Gets nations in the war excluding surrendered ones.
     *
     * @return the nations
     */
    public Set<Nation> getNations() {
        return Stream.concat(
                getSide1().getNations().stream(),
                getSide2().getNations().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all surrendered nations in the war.
     *
     * @return the surrendered nations
     */
    public Set<Nation> getNationsSurrendered() {
        return Stream.concat(
                getSide1().getNationsSurrendered().stream(),
                getSide2().getNationsSurrendered().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all nations in the war including surrendered.
     *
     * @return the all nations
     */
    public Set<Nation> getNationsAll() {
        return Stream.concat(
                getSide1().getNations().stream(),
                Stream.concat(
                    getSide2().getNations().stream(),
                    getNationsSurrendered().stream()
                )
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets towns in the war excluding surrendered ones.
     *
     * @return the towns
     */
    public Set<Town> getTowns() {
        return Stream.concat(
                getSide1().getTowns().stream(),
                getSide2().getTowns().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all surrendered towns in the war.
     *
     * @return the surrendered towns
     */
    public Set<Town> getTownsSurrendered() {
        return Stream.concat(
                getSide1().getTownsSurrendered().stream(),
                getSide2().getTownsSurrendered().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all towns in the war including surrendered towns.
     *
     * @return the all towns
     */
    public Set<Town> getTownsAll() {
        return Stream.concat(
                getSide1().getTowns().stream(),
                Stream.concat(
                    getSide2().getTowns().stream(),
                    getTownsSurrendered().stream()
                )
            )
            .collect(Collectors.toSet());
    }

    // SECTION Players Getters

    /**
     * Get all players in the war (excluding surrendered)
     * @return player list
     */
    public List<OfflinePlayer> getPlayers() {
        return Stream.concat(
            getSide1().getPlayers().stream(),
            getSide2().getPlayers().stream()
        ).toList();
    }

    /**
     * Get all players in the war (excludes non-surrendered players)
     * @return player list
     */
    public List<OfflinePlayer> getPlayersSurrendered() {
        return Stream.concat(
            getSide1().getPlayersSurrendered().stream(),
            getSide2().getPlayersSurrendered().stream()
        ).toList();
    }

    /**
     * Get all players in the war (includes surrendered players)
     * @return player list
     */
    public List<OfflinePlayer> getPlayersAll() {
        return Stream.concat(getSide1().getPlayersAll().stream(), getSide2().getPlayersAll().stream()).toList();
    }

    /**
     * Get all online players in the war (excluding surrendered)
     * @return player list
     */
    public List<Player> getPlayersOnline() {
        return Stream.concat(
            getSide1().getPlayersOnline().stream(),
            getSide2().getPlayersOnline().stream()
        ).toList();
    }

    /**
     * Get all online players in the war (excludes non-surrendered players)
     * @return player list
     */
    public List<Player> getPlayersSurrenderedOnline() {
        return Stream.concat(
            getSide1().getPlayersSurrenderedOnline().stream(),
            getSide2().getPlayersSurrenderedOnline().stream()
        ).toList();
    }

    /**
     * Get all online players in the war (includes surrendered players)
     * @return player list
     */
    public List<Player> getPlayersOnlineAll() {
        return Stream.concat(
            getSide1().getPlayersOnlineAll().stream(),
            getSide2().getPlayersOnlineAll().stream()
        ).toList();
    }

    // SECTION Misc

    /**
     * Check if player is in war.
     *
     * @param p the p
     * @return the boolean
     */
    public boolean isInWar(Player p) {
        return side1.isOnSide(p) || side2.isOnSide(p);
    }

    /**
     * Check if player is in war.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean isInWar(UUID uuid) {
        return side1.isOnSide(uuid) || side2.isOnSide(uuid);
    }

    /**
     * Gets the side of the player or null if they are not in the war.
     *
     * @param p the p
     * @return the player side
     */
    @Nullable
    public Side getPlayerSide(Player p) {
        return getPlayerSide(p.getUniqueId());
    }

    /**
     * Gets player side by player UUID or null if they are not in the war.
     *
     * @param uuid the uuid
     * @return the player side
     */
    @Nullable
    public Side getPlayerSide(UUID uuid) {
        if (side1.isOnSide(uuid))
            return getSide1();

        if (side2.isOnSide(uuid))
            return getSide2();

        return null;
    }

    /**
     * Is nation or town in war.
     *
     * @param government the nation or town
     * @return the boolean
     */
    public boolean isInWar(Government government) {
        return side1.isOnSide(government) || side2.isOnSide(government);
    }

    /**
     * Gets nation or town side, or null if not in war.
     *
     * @param government the nation or town
     * @return the town side
     */
    @Nullable
    public Side getSide(Government government) {
        if (side1.isOnSide(government))
            return getSide1();

        if (side2.isOnSide(government))
            return getSide2();

        return null;
    }

    /**
     * Check if a side exists with the specified name.
     *
     * @param sideName the side name
     * @return the boolean
     */
    public boolean isSideValid(String sideName) {
        return getSide1().equals(sideName) || getSide2().equals(sideName);
    }

    /**
     * Cancel all active sieges in the war.
     */
    @ApiStatus.Internal
    public void cancelSieges() {
        getTowns().forEach(this::cancelSieges);
    }

    /**
     * Cancel all active sieges in the war. Ends the sieges in a draw.
     *
     * @param town the town
     */
    @ApiStatus.Internal
    public void cancelSieges(Town town) {
        if (this != null) {
            Side townSide = getSide(town);

            if (townSide == null) return;

            getSieges().forEach(siege -> { // TODO INFINITE LOOP, runs surrenderTown
                if (siege.getTown().equals(town)) {
                    if (siege.getAttackerSide().equals(townSide)) {
                        siege.defendersWin(BattleVictoryReason.ADMIN_CANCEL);
                    } else {
                        siege.attackersWin(BattleVictoryReason.ADMIN_CANCEL);
                    }
                }
            });
        }
    }

    /**
     * Makes a government surrender (Recursively surrenders subjects AKA nations, towns, players)
     *
     * @param government the government
     */
    public void surrender(Government government) {
        if (government instanceof Nation nation) {
            Side nationSide = getSide(nation);
            if (nationSide == null) return;

            final @Nullable Nation occupier = nationSide.equals(getSide1()) ? getSide2().getTown().getNationOrNull() : getSide1().getTown().getNationOrNull();
            Occupation.setOccupied(nation, occupier);

            nationSide.surrender(nation);
            // TODO Cancel in progress sieges for towns?
            nationSide.processSurrenders();
        } else if (government instanceof Town town) {
            Side townSide = getSide(town);
            if (townSide == null) return;

            final @Nullable Nation townNation = town.getNationOrNull();
            final @Nullable Nation occupier = townSide.equals(getSide1()) ? getSide2().getTown().getNationOrNull() : getSide1().getTown().getNationOrNull();
            Occupation.setOccupied(town, occupier);
            townSide.surrender(town);

            if (townNation != null) {
                if (townSide.shouldSurrender(townNation)) {
                    surrender(townNation);
                }
            }

            // TODO Cancel in progress sieges for towns?
            townSide.processSurrenders();
        }
    }

    /**
     * Makes a government un-surrender (Recursively un-surrenders subjects AKA nations, towns, players)
     *
     * @param government the government
     */
    public void unsurrender(Government government) {
        if (government instanceof Nation nation) {
            Side nationSide = getSide(nation);
            if (nationSide == null) return;

            nationSide.unsurrender(nation);
            nation.getTowns().forEach(this::unsurrender);
        } else if (government instanceof Town town) {
            Side townSide = getSide(town);
            if (townSide == null) return;

            Occupation.removeOccupied(town);

            townSide.unsurrender(town);
        }
    }

    /**
     * Start a new war after creation.
     */
    @ApiStatus.Internal
    public void start() {
        Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The war of <war> has started between <attacker> and <defender>.")
            .parseMinimessagePlaceholder("war", getLabel())
            .parseMinimessagePlaceholder("attacker", getAttacker().getName())
            .parseMinimessagePlaceholder("defender", getDefender().getName())
            .build()
        );

        final Title warTitle = Title.title(
            ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War")
                .build(),
            ColorParser.of("<gray><i>The war of <war> has begun!")
                .parseMinimessagePlaceholder("war", getLabel())
                .build(),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
        );
        final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

        side1.getPlayersOnline().forEach(player -> {
            player.showTitle(warTitle);
            player.playSound(warSound);
        });
        side2.getPlayersOnline().forEach(player -> {
            player.showTitle(warTitle);
            player.playSound(warSound);
        });

        side1.applyNameTags();
        side2.applyNameTags();
    }

    /**
     * Resume a pre-existing war after loading it from db.
     */
    @ApiStatus.Internal
    public void resume() {
        side1.applyNameTags();
        side2.applyNameTags();
    }

    /**
     * End a war for the specified reason. Triggered internally by {@link War#defeat(Side)} & {@link War#draw()}.
     *
     * @param reason the reason
     */
    @ApiStatus.Internal
    public void end(WarDeleteReason reason) {
        if (!new PreWarDeleteEvent(this, reason).callEvent())
            return;

        getSieges().forEach(Siege::stop);
        getTownsAll().forEach(Occupation::removeOccupied);

        DatabaseQueries.deleteWar(this);
        WarController.getInstance().removeWar(this);

        // Run check after war is removed to cleanup player tags
        getPlayersOnline().forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));

        new WarDeleteEvent(this, reason).callEvent();
    }

    /**
     * End the war in defeat for the specified side. Internally runs {@link War#end(WarDeleteReason)}.
     *
     * @param loserSide the loser side
     */
    public void defeat(Side loserSide) {
        Side loser = loserSide.equals(getSide1()) ? getSide1() : getSide2();
        Side winner = loserSide.equals(getSide1()) ? getSide2() : getSide1();

        Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The war of <war> has ended. <red><winner> <reset>has triumphed against <red><loser><reset>.")
            .parseMinimessagePlaceholder("war", getLabel())
            .parseMinimessagePlaceholder("winner", winner.getName())
            .parseMinimessagePlaceholder("loser", loser.getName())
            .build()
        );

        end(WarDeleteReason.DEFEAT);
    }

    /**
     * End the war in a white peace/draw. Internally runs {@link War#end(WarDeleteReason)}.
     */
    public void draw() {
        Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The war of <war> has ended with a white peace.")
            .parseMinimessagePlaceholder("war", getLabel())
            .build()
        );

        end(WarDeleteReason.DRAW);
    }

    /**
     * Sets if this is an event war.
     *
     * @param event the event
     */
    public void setEventWar(boolean event) {
        this.event = event;
    }

    /**
     * Gets if this is an event war. Event wars skip or execute certain logic as they are for player events.
     *
     * @return the boolean
     */
    public boolean isEventWar() {
        return event;
    }
}
