package me.ShermansWorld.AlathranWars.conflict;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import me.ShermansWorld.AlathranWars.conflict.battle.raid.Raid;
import me.ShermansWorld.AlathranWars.conflict.battle.siege.Siege;
import me.ShermansWorld.AlathranWars.enums.BattleSide;
import me.ShermansWorld.AlathranWars.enums.BattleTeam;
import me.ShermansWorld.AlathranWars.holder.WarManager;
import me.ShermansWorld.AlathranWars.listeners.JoinListener;
import me.ShermansWorld.AlathranWars.utility.UtilsChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final Side side1;
    private final Side side2;
    private final Side attacker; // Reference variable to side1 or side2
    private final Side defender; // Reference variable to side1 or side2

    private final Set<Siege> sieges = new HashSet<>();
    private final Set<Raid> raids = new HashSet<>();

    // Merged utility data from Side's
//    private Set<Player> players = new HashSet<>();
//    private Set<UUID> playersIncludingOffline = new HashSet<>();

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
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        this.side1 = new Side(this.uuid, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

//        recalculatePlayers();

        WarManager.getInstance().addWar(this);

        start();
    }

    public War(final String label, Nation aggressor, Nation victim) throws WrapperCommandSyntaxException {
        this.uuid = generateUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        this.side1 = new Side(this.uuid, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

//        recalculatePlayers();

        WarManager.getInstance().addWar(this);

        start();
    }

    public War(final String label, Town aggressor, Nation victim) throws WrapperCommandSyntaxException {
        this.uuid = generateUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        this.side1 = new Side(this.uuid, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

//        recalculatePlayers();

        WarManager.getInstance().addWar(this);

        start();
    }

    public War(final String label, Nation aggressor, Town victim) throws WrapperCommandSyntaxException {
        this.uuid = generateUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        this.side1 = new Side(this.uuid, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

//        recalculatePlayers();

        WarManager.getInstance().addWar(this);

        start();
    }

    private UUID generateUUID() {
        UUID uuid = UUID.randomUUID();

        while (WarManager.getInstance().getWar(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
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

    @NotNull
    public Set<Player> getPlayers() {
        return Stream.concat(
            getSide1().getPlayers().stream(),
            getSide2().getPlayers().stream()
        )
        .collect(Collectors.toSet());
    }

    @NotNull
    public Set<OfflinePlayer> getPlayersIncludingOffline() {
        return Stream.concat(
            getSide1().getPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer),
            getSide2().getPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer)
        )
        .collect(Collectors.toSet());
    }

    @NotNull
    public Set<OfflinePlayer> getSurrenderedPlayers() {
        return Stream.concat(
            getSide1().getSurrenderedPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer),
            getSide2().getSurrenderedPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer)
        )
        .collect(Collectors.toSet());
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
        return Stream.concat(
                getSide1().getTowns().stream(),
                getSide2().getTowns().stream()
            )
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Nation> getNations() {
        return Stream.concat(
                getSide1().getNations().stream(),
                getSide2().getNations().stream()
            )
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Town> getSurrenderedTowns() {
        return Stream.concat(
                getSide1().getSurrenderedTowns().stream(),
                getSide2().getSurrenderedTowns().stream()
            )
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Nation> getSurrenderedNations() {
        return Stream.concat(
                getSide1().getSurrenderedNations().stream(),
                getSide2().getSurrenderedNations().stream()
            )
            .collect(Collectors.toSet());
    }

    public void start() {
        Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The war of <war> has started between <attacker> and <defender>.")
            .parseMinimessagePlaceholder("war", getLabel())
            .parseMinimessagePlaceholder("attacker", getAttacker().getName())
            .parseMinimessagePlaceholder("defender", getDefender().getName())
            .build()
        );

        side1.calculateOnlinePlayers();
        side2.calculateOnlinePlayers();
    }

    public void end() {
        // TODO Stop battles
        getSieges().forEach(Siege::stop);
//        getRaids().forEach(Raid::stop);

        WarManager.getInstance().removeWar(this);

        // Run check after war is removed to cleanup player tags
        this.getPlayers().forEach(JoinListener::checkPlayer);
    }

    public void defeat(Side loserSide) {
        Side loser = loserSide.equals(getSide1()) ? getSide1() : getSide2();
        Side winner = loserSide.equals(getSide1()) ? getSide2() : getSide1();

        Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The war of <war> has ended. <red><winner> <reset>has triumphed against <red><loser><reset>.")
            .parseMinimessagePlaceholder("war", getLabel())
            .parseMinimessagePlaceholder("winner", winner.getName())
            .parseMinimessagePlaceholder("loser", loser.getName())
            .build()
        );

        end();
    }

    // White defeat/force end war
    public void draw() {
        Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The war of <war> has ended with a white peace.")
            .parseMinimessagePlaceholder("war", getLabel())
            .build()
        );

        end();
    }
}
