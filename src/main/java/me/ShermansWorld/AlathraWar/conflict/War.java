package me.ShermansWorld.AlathraWar.conflict;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import me.ShermansWorld.AlathraWar.conflict.battle.Raid;
import me.ShermansWorld.AlathraWar.conflict.battle.Side;
import me.ShermansWorld.AlathraWar.conflict.battle.Siege;
import me.ShermansWorld.AlathraWar.enums.BattleSide;
import me.ShermansWorld.AlathraWar.enums.BattleTeam;
import me.ShermansWorld.AlathraWar.holder.WarManager;
import me.ShermansWorld.AlathraWar.utility.UtilsChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The type New war.
 */
public class War extends Conflict {
    private final UUID uuid;
    private final String name; // Used in commands and such, like "TidalHaven.vs.Meme"
    private final String label; 
    private final ConflictType conflictType = ConflictType.WAR;

    private final Side side1;
    private final Side side2;
    private final Side attacker; // Reference variable to side1 or side2
    private final Side defender; // Reference variable to side1 or side2

    private final Set<Siege> sieges = new HashSet<>();
    private final Set<Raid> raids = new HashSet<>();

    // Merged utility data from Side's
    private Set<Player> players = new HashSet<>();
    private Set<UUID> playersIncludingOffline = new HashSet<>();

    /**
     * Instantiates a new New war.
     *
     * @param label  the name
     * @param aggressor the side 1
     * @param victim the side 2
     */
    public War(final String label, Town aggressor, Town victim) throws WrapperCommandSyntaxException {
        this.uuid = generateUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cA war already exists with that name!").build());

        this.side1 = new Side(this.uuid, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

        recalculatePlayers();
    }

    public War(final String label, Nation aggressor, Nation victim) throws WrapperCommandSyntaxException {
        this.uuid = generateUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cA war already exists with that name!").build());

        this.side1 = new Side(this.uuid, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

        recalculatePlayers();
    }

    public War(final String label, Town aggressor, Nation victim) throws WrapperCommandSyntaxException {
        this.uuid = generateUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cA war already exists with that name!").build());

        this.side1 = new Side(this.uuid, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

        recalculatePlayers();
    }

    public War(final String label, Nation aggressor, Town victim) throws WrapperCommandSyntaxException {
        this.uuid = generateUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cA war already exists with that name!").build());

        this.side1 = new Side(this.uuid, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

        recalculatePlayers();
    }

    private UUID generateUUID() {
        UUID uuid = UUID.randomUUID();

        while (WarManager.getInstance().getWar(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        return UUID.randomUUID();
    }

    /**
     * Gets uuid.
     *
     * @return the uuid
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    /**
     * Equals boolean.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean equals(UUID uuid) {
        return this.uuid.equals(uuid);
    }

    /**
     * Equals boolean.
     *
     * @param war the war
     * @return the boolean
     */
    public boolean equals(War war) {
        return this.uuid.equals(war.getUUID());
    }

    /**
     * Equals boolean.
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

    @NotNull
    public Set<Side> getSides() {
        final Set<Side> sides = new HashSet<>();
        sides.add(getSide1());
        sides.add(getSide2());
        return sides;
    }

    /**
     * Gets attacker.
     *
     * @return the attacker
     */
    @NotNull
    public Side getAttacker() {
        return attacker;
    }

    /**
     * Gets defender.
     *
     * @return the defender
     */
    @NotNull
    public Side getDefender() {
        return defender;
    }

    @Nullable
    public Side getSide(String name) {
        if (getSide1().equals(name))
            return getSide1();

        if (getSide2().equals(name))
            return getSide2();

        return null;
    }

    @Nullable
    public Side getSide(UUID uuid) {
        if (getSide1().equals(uuid))
            return getSide1();

        if (getSide2().equals(uuid))
            return getSide2();

        return null;
    }

    /**
     * Gets surrendered.
     *
     * @return the surrendered
     */
//    public Set<UUID> getSurrenderedPlayers() {
//        return surrendered;
//    }

    /**
     * Gets sieges.
     *
     * @return the sieges
     */
    @NotNull
    public Set<Siege> getSieges() {
        return sieges;
    }

    /**
     * Add siege.
     *
     * @param siege the siege
     */
    public void addSiege(Siege siege) {
        sieges.add(siege);
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
     * Add raid.
     *
     * @param raid the raid
     */
    public void addRaid(Raid raid) {
        raids.add(raid);
    }

    /**
     * Gets all players in the war, including offline players.
     *
     * @return the all players
     */
    @NotNull
    public Set<OfflinePlayer> getPlayersIncludingOffline() {
        final Set<OfflinePlayer> playersIncludingOffline = new HashSet<>();

        for (UUID uuid : this.playersIncludingOffline)
            playersIncludingOffline.add(Bukkit.getOfflinePlayer(uuid));

        return playersIncludingOffline;
    }

    /**
     * Gets all players in the war, including offline players.
     *
     * @return the all players
     */
    @NotNull
    public Set<Player> getPlayers() {
        return players;
    }

    public void recalculatePlayers() {
        final Set<UUID> newPlayersIncludingOffline = new HashSet<>();
        newPlayersIncludingOffline.addAll(side1.getPlayersIncludingOffline());
        newPlayersIncludingOffline.addAll(side2.getPlayersIncludingOffline());
        playersIncludingOffline = newPlayersIncludingOffline;

        final Set<Player> newPlayers = new HashSet<>();
        newPlayers.addAll(side1.getPlayers());
        newPlayers.addAll(side2.getPlayers());
        players = newPlayers;
    }

    public boolean isPlayerInWar(Player p) {
        return side1.isPlayerOnSide(p) || side2.isPlayerOnSide(p);
    }

    public boolean isPlayerInWar(UUID uuid) {
        return side1.isPlayerOnSide(uuid) || side2.isPlayerOnSide(uuid);
    }

    @Nullable
    public Side getPlayerSide(Player p) {
        if (side1.isPlayerOnSide(p))
            return getSide1();

        if (side2.isPlayerOnSide(p))
            return getSide2();

        return null;
    }

    @Nullable
    public Side getPlayerSide(UUID uuid) {
        if (side1.isPlayerOnSide(uuid))
            return getSide1();

        if (side2.isPlayerOnSide(uuid))
            return getSide2();

        return null;
    }

    public boolean isTownInWar(Town town) {
        return side1.isTownOnSide(town) || side2.isTownOnSide(town);
    }

    @Nullable
    public Side getTownSide(Town town) {
        if (side1.isTownOnSide(town))
            return getSide1();

        if (side2.isTownOnSide(town))
            return getSide2();

        return null;
    }

    public boolean isNationInWar(Nation nation) {
        return side1.isNationOnSide(nation) || side2.isNationOnSide(nation);
    }

    @Nullable
    public Side getNationSide(Nation nation) {
        if (side1.isNationOnSide(nation))
            return getSide1();

        if (side2.isNationOnSide(nation))
            return getSide2();

        return null;
    }

    public boolean isSideValid(String sideName) {
        return getSide1().equals(sideName) || getSide2().equals(sideName);
    }

    @NotNull
    public Set<Town> getTowns() {
        Set<Town> towns = new HashSet<>();

        towns.addAll(side1.getTowns());
        towns.addAll(side2.getTowns());

        return towns;
    }

    @NotNull
    public Set<Nation> getNations() {
        Set<Nation> nations = new HashSet<>();

        nations.addAll(side1.getNations());
        nations.addAll(side2.getNations());

        return nations;
    }

    @NotNull
    public Set<Town> getSurrenderedTowns() {
        Set<Town> towns = new HashSet<>();

        towns.addAll(side1.getSurrenderedTowns());
        towns.addAll(side2.getSurrenderedTowns());

        return towns;
    }

    @NotNull
    public Set<Nation> getSurrenderedNations() {
        Set<Nation> nations = new HashSet<>();

        nations.addAll(side1.getSurrenderedNations());
        nations.addAll(side2.getSurrenderedNations());

        return nations;
    }

    public void start() {

    }

    public void end() {

    }
}
