package com.github.alathra.AlathranWars.conflict;

import com.github.alathra.AlathranWars.conflict.battle.raid.Raid;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.db.DatabaseQueries;
import com.github.alathra.AlathranWars.enums.ConflictType;
import com.github.alathra.AlathranWars.enums.WarDeleteReason;
import com.github.alathra.AlathranWars.enums.battle.BattleSide;
import com.github.alathra.AlathranWars.enums.battle.BattleTeam;
import com.github.alathra.AlathranWars.enums.battle.BattleVictoryReason;
import com.github.alathra.AlathranWars.events.PreWarCreateEvent;
import com.github.alathra.AlathranWars.events.PreWarDeleteEvent;
import com.github.alathra.AlathranWars.events.WarCreateEvent;
import com.github.alathra.AlathranWars.events.WarDeleteEvent;
import com.github.alathra.AlathranWars.hooks.NameColorHandler;
import com.github.alathra.AlathranWars.utility.UtilsChat;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type New war.
 */
public class War extends Conflict {
    private final UUID uuid;
    private final String name; // Used in commands and such, like "TidalHaven.vs.Meme"
    private final String label;
    private final ConflictType conflictType = ConflictType.WAR;
    private boolean event;

    private @NotNull Side side1;
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
    public War(
        UUID uuid,
        String name,
        String label,
        @NotNull Side side1,
        Side side2,
        Set<Siege> sieges,
        Set<Raid> raids,
        boolean event
    ) {
        this.uuid = uuid;
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
    public War(
        UUID uuid,
        String label,
        Government aggressor,
        Government victim,
        boolean event
    ) throws SideCreationException, WrapperCommandSyntaxException {
        this.uuid = uuid;
        this.label = label;

        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        this.side1 = new SideBuilder()
            .setWarUUID(this.uuid)
            .setUuid(UUID.randomUUID())
            .setLeader(aggressor)
            .setSide(BattleSide.ATTACKER)
            .setTeam(BattleTeam.SIDE_1)
            .buildNew();
        this.side2 = new SideBuilder()
            .setWarUUID(this.uuid)
            .setUuid(UUID.randomUUID())
            .setLeader(victim)
            .setSide(BattleSide.DEFENDER)
            .setTeam(BattleTeam.SIDE_2)
            .buildNew();

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;
        this.event = event;

        if (!new PreWarCreateEvent(this).callEvent())
            return;

        // TODO All logic below this point should be separated out
        WarManager.getInstance().addWar(this);

        start();

        new WarCreateEvent(this).callEvent();
    }

    /**
     * Gets uuid of the war.
     *
     * @return the uuid
     */
    public UUID getUUID() {
        return this.uuid;
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
     * Compare war UUID's.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean equals(UUID uuid) {
        return this.uuid.equals(uuid);
    }

    /**
     * Compare war by UUID's.
     *
     * @param war the war
     * @return the boolean
     */
    public boolean equals(@NotNull War war) {
        return this.uuid.equals(war.getUUID());
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
        return raids.stream().anyMatch(raid -> raid.getTown().equals(town));
    }

    /**
     * Gets all online players in the war.
     *
     * @return the players
     */
    @NotNull
    public Set<Player> getPlayers() {
        return Stream.concat(
                getSide1().getPlayers().stream(),
                getSide2().getPlayers().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all players, including offline ones in the war.
     *
     * @return the players including offline
     */
    @NotNull
    public Set<OfflinePlayer> getPlayersIncludingOffline() {
        return Stream.concat(
                getSide1().getPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer),
                getSide2().getPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer)
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all surrendered players in the war.
     *
     * @return the surrendered players
     */
    @NotNull
    public Set<OfflinePlayer> getSurrenderedPlayers() {
        return Stream.concat(
                getSide1().getSurrenderedPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer),
                getSide2().getSurrenderedPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer)
            )
            .collect(Collectors.toSet());
    }

    /**
     * Check if player is in war.
     *
     * @param p the p
     * @return the boolean
     */
    public boolean isPlayerInWar(Player p) {
        return side1.isPlayerOnSide(p) || side2.isPlayerOnSide(p);
    }

    /**
     * Check if player is in war.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean isPlayerInWar(UUID uuid) {
        return side1.isPlayerOnSide(uuid) || side2.isPlayerOnSide(uuid);
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
        if (side1.isPlayerOnSide(uuid))
            return getSide1();

        if (side2.isPlayerOnSide(uuid))
            return getSide2();

        return null;
    }

    /**
     * Is town in war.
     *
     * @param town the town
     * @return the boolean
     */
    public boolean isTownInWar(Town town) {
        return side1.isTownOnSide(town) || side2.isTownOnSide(town);
    }

    /**
     * Gets town side or null if not in war.
     *
     * @param town the town
     * @return the town side
     */
    @Nullable
    public Side getTownSide(Town town) {
        if (side1.isTownOnSide(town))
            return getSide1();

        if (side2.isTownOnSide(town))
            return getSide2();

        return null;
    }

    /**
     * Is nation in war boolean.
     *
     * @param nation the nation
     * @return the boolean
     */
    public boolean isNationInWar(Nation nation) {
        return side1.isNationOnSide(nation) || side2.isNationOnSide(nation);
    }

    /**
     * Gets nation side or null if not in war.
     *
     * @param nation the nation
     * @return the nation side
     */
    @Nullable
    public Side getNationSide(Nation nation) {
        if (side1.isNationOnSide(nation))
            return getSide1();

        if (side2.isNationOnSide(nation))
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
            Side townSide = getTownSide(town);

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
     * Surrender a nation and all its towns out of the war.
     *
     * @param nation the nation
     */
    public void surrenderNation(Nation nation) {
        Side nationSide = getNationSide(nation);
        if (nationSide == null) return;

        final @Nullable Nation occupier = nationSide.equals(getSide1()) ? getSide2().getTown().getNationOrNull() : getSide1().getTown().getNationOrNull();
        Occupation.setOccupied(nation, occupier);

        nationSide.surrenderNation(nation);
        // TODO Cancel in progress sieges for towns?
        nationSide.processSurrenders();
    }

    /**
     * Surrender town and all its players out of a war.
     *
     * @param town the town
     */
    public void surrenderTown(Town town) {
        Side townSide = getTownSide(town);
        if (townSide == null) return;

        final @Nullable Nation townNation = town.getNationOrNull();
        final @Nullable Nation occupier = townSide.equals(getSide1()) ? getSide2().getTown().getNationOrNull() : getSide1().getTown().getNationOrNull();
        Occupation.setOccupied(town, occupier);
        townSide.surrenderTown(town);

        if (townNation != null) {
            if (townSide.shouldNationSurrender(townNation)) {
                surrenderNation(townNation);
            }
        }

        // TODO Cancel in progress sieges for towns?
        townSide.processSurrenders();
    }

    /**
     * Unsurrender a nation and all its towns and players.
     *
     * @param nation the nation
     */
    public void unsurrenderNation(Nation nation) {
        Side nationSide = getNationSide(nation);
        if (nationSide == null) return;

        nationSide.unsurrenderNation(nation);
        nation.getTowns().forEach(this::unsurrenderTown);
    }

    /**
     * Unsurrender town and all its players.
     *
     * @param town the town
     */
    public void unsurrenderTown(Town town) {
        Side townSide = getTownSide(town);
        if (townSide == null) return;

        Occupation.removeOccupied(town);

        townSide.unsurrenderTown(town);
    }

    /**
     * Gets towns in the war excluding surrendered ones.
     *
     * @return the towns
     */
    @NotNull
    public Set<Town> getTowns() {
        return Stream.concat(
                getSide1().getTowns().stream(),
                getSide2().getTowns().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all towns in the war including surrendered towns.
     *
     * @return the all towns
     */
    @NotNull
    public Set<Town> getAllTowns() {
        return Stream.concat(
                getSide1().getTowns().stream(),
                Stream.concat(
                    getSide2().getTowns().stream(),
                    getSurrenderedTowns().stream()
                )
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets nations in the war excluding surrendered ones.
     *
     * @return the nations
     */
    @NotNull
    public Set<Nation> getNations() {
        return Stream.concat(
                getSide1().getNations().stream(),
                getSide2().getNations().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all nations in the war including surrendered.
     *
     * @return the all nations
     */
    @NotNull
    public Set<Nation> getAllNations() {
        return Stream.concat(
                getSide1().getNations().stream(),
                Stream.concat(
                    getSide2().getNations().stream(),
                    getSurrenderedNations().stream()
                )
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all surrendered towns in the war.
     *
     * @return the surrendered towns
     */
    @NotNull
    public Set<Town> getSurrenderedTowns() {
        return Stream.concat(
                getSide1().getSurrenderedTowns().stream(),
                getSide2().getSurrenderedTowns().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all surrendered nations in the war.
     *
     * @return the surrendered nations
     */
    @NotNull
    public Set<Nation> getSurrenderedNations() {
        return Stream.concat(
                getSide1().getSurrenderedNations().stream(),
                getSide2().getSurrenderedNations().stream()
            )
            .collect(Collectors.toSet());
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

        side1.getPlayers().forEach(player -> {
            player.showTitle(warTitle);
            player.playSound(warSound);
        });
        side2.getPlayers().forEach(player -> {
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
        getAllTowns().forEach(Occupation::removeOccupied);

        DatabaseQueries.deleteWar(this);
        WarManager.getInstance().removeWar(this);

        // Run check after war is removed to cleanup player tags
        getPlayers().forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));

        new WarDeleteEvent(this, reason).callEvent();
    }

    /**
     * End the war in defeat for the specified side. Internally runs {@link War#end(WarDeleteReason)}.
     *
     * @param loserSide the loser side
     */
    public void defeat(@NotNull Side loserSide) {
        @NotNull Side loser = loserSide.equals(getSide1()) ? getSide1() : getSide2();
        @NotNull Side winner = loserSide.equals(getSide1()) ? getSide2() : getSide1();

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
