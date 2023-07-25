package com.github.alathra.AlathranWars.conflict;

import com.github.alathra.AlathranWars.enums.BattleSide;
import com.github.alathra.AlathranWars.enums.BattleTeam;
import com.github.alathra.AlathranWars.holder.WarManager;
import com.github.alathra.AlathranWars.listeners.war.PlayerJoinListener;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Side {
    private static final Duration SIEGE_COOLDOWN = Duration.ofMinutes(120);
    private static final Duration RAID_COOLDOWN = Duration.ofMinutes(30);
    private final UUID warUUID;
    private final UUID uuid;
    private final BattleSide side;
    private final BattleTeam team;
    private final String name; // A town name
    // TODO Use Town & Nation UUID's when saving to config
    private final Town town; // The initiating town or capital of the nation
    private final Set<Town> towns; // A list of participating Towns
    private final Set<Nation> nations; // A list of participating Nations
    private final Set<UUID> playersIncludingOffline; // A list of all participating player UUID, (Online & Offline)
    private final @NotNull Set<Player> players; // A list of the players who are currently online
    private final Set<Town> surrenderedTowns;
    private final Set<Nation> surrenderedNations;
    private final Set<UUID> surrenderedPlayersIncludingOffline;
    private Instant siegeGrace = Instant.now().minus(SIEGE_COOLDOWN); // The time after which this side can be besieged
    private Instant raidGrace = Instant.now().minus(RAID_COOLDOWN); // The time after which this side can be raided

    private int score = 0;

    // Create object from DB
    public Side(
        UUID warUUID,
        UUID uuid,
        Town town,
        BattleSide side,
        BattleTeam team,
        String name,
        Set<Town> towns,
        Set<Nation> nations,
        Set<UUID> playersIncludingOffline,
        Set<Town> surrenderedTowns,
        Set<Nation> surrenderedNations,
        Set<UUID> surrenderedPlayersIncludingOffline,
        Instant siegeGrace,
        Instant raidGrace
    ) {
        this.warUUID = warUUID;
        this.uuid = uuid;
        this.side = side;
        this.team = team;
        this.town = town;
        this.name = name;

        this.towns = towns;
        this.nations = nations;
        this.playersIncludingOffline = playersIncludingOffline;
        this.players = new HashSet<>();

        this.surrenderedTowns = surrenderedTowns;
        this.surrenderedNations = surrenderedNations;
        this.surrenderedPlayersIncludingOffline = surrenderedPlayersIncludingOffline;

        this.siegeGrace = siegeGrace;
        this.raidGrace = raidGrace;

        calculateOnlinePlayers();
    }

    public Side(UUID warUUID, UUID uuid, Town town, BattleSide side, BattleTeam team) {
        this.warUUID = warUUID;
        this.uuid = uuid;
        this.side = side;
        this.team = team;
        this.town = town;
        this.name = this.town.getName();

        this.towns = new HashSet<>();
        this.nations = new HashSet<>();
        this.playersIncludingOffline = new HashSet<>();
        this.players = new HashSet<>();

        this.surrenderedTowns = new HashSet<>();
        this.surrenderedNations = new HashSet<>();
        this.surrenderedPlayersIncludingOffline = new HashSet<>();

        addTown(this.town);

        calculateOnlinePlayers();
    }

    public Side(UUID warUUID, UUID uuid, @NotNull Nation nation, BattleSide side, BattleTeam team) {
        this.warUUID = warUUID;
        this.uuid = uuid;
        this.side = side;
        this.team = team;
        this.town = nation.getCapital();
        this.name = this.town.getName();

        this.towns = new HashSet<>();
        this.nations = new HashSet<>();
        this.playersIncludingOffline = new HashSet<>();
        this.players = new HashSet<>();

        this.surrenderedTowns = new HashSet<>();
        this.surrenderedNations = new HashSet<>();
        this.surrenderedPlayersIncludingOffline = new HashSet<>();

        addNation(nation);

        calculateOnlinePlayers();
    }

    public BattleSide getSide() {
        return side;
    }

    public BattleTeam getTeam() {
        return team;
    }

    public String getName() {
        return name;
    }

    public Set<Nation> getNations() {
        return nations;
    }

    public Set<Town> getTowns() {
        return towns;
    }

    public Set<UUID> getPlayersIncludingOffline() {
        return playersIncludingOffline;
    }

    public @NotNull Set<Player> getPlayers() {
        return players;
    }

    public Set<Town> getSurrenderedTowns() {
        return surrenderedTowns;
    }

    public Set<Nation> getSurrenderedNations() {
        return surrenderedNations;
    }

    public Set<UUID> getSurrenderedPlayersIncludingOffline() {
        return surrenderedPlayersIncludingOffline;
    }

    // Participant management

    public boolean isTownOnSide(Town town) {
        return towns.contains(town) || surrenderedTowns.contains(town);
    }

    public boolean isNationOnSide(Nation nation) {
        return nations.contains(nation) || surrenderedNations.contains(nation);
    }

    public boolean isTownSurrendered(Town town) {
        return surrenderedTowns.contains(town);
    }

    public boolean isNationSurrendered(Nation nation) {
        return surrenderedNations.contains(nation);
    }

    public void addTown(@NotNull Town town) {
        if (isTownOnSide(town)) return;

        towns.add(town);

        town.getResidents().forEach((Resident resident) -> addPlayer(resident.getUUID()));
    }

    public void addNation(@NotNull Nation nation) {
        if (isNationOnSide(nation)) return;

        nations.add(nation);

        nation.getTowns().forEach(this::addTown);
    }

    public void removeTown(@NotNull Town town) {
        if (!isTownOnSide(town)) return;

        towns.remove(town);

        town.getResidents().forEach((Resident resident) -> removePlayer(resident.getUUID()));
    }

    public void removeNation(@NotNull Nation nation) {
        if (!isNationOnSide(nation)) return;

        nations.remove(nation);

        nation.getTowns().forEach(this::removeTown);
    }

    public void surrenderTown(@NotNull Town town) {
        if (isTownSurrendered(town)) return;

        removeTown(town);

        surrenderedTowns.add(town);

        town.getResidents().forEach((Resident resident) -> surrenderPlayer(resident.getUUID()));
    }

    public void surrenderNation(@NotNull Nation nation) {
        if (isNationSurrendered(nation)) return;

        removeNation(nation);

        surrenderedNations.add(nation);

        nation.getTowns().forEach(this::surrenderTown);
    }

    public boolean isPlayerOnSide(@NotNull Player p) {
        return isPlayerOnSide(p.getUniqueId());
    }

    public boolean isPlayerOnSide(UUID uuid) {
        return playersIncludingOffline.contains(uuid) || surrenderedPlayersIncludingOffline.contains(uuid);
    }

    public boolean isPlayerSurrendered(@NotNull Player p) {
        return isPlayerSurrendered(p.getUniqueId());
    }

    public boolean isPlayerSurrendered(UUID uuid) {
        return surrenderedPlayersIncludingOffline.contains(uuid);
    }

    public void addPlayer(@NotNull Player p) {
        addPlayer(p.getUniqueId());
    }

    public void addPlayer(@NotNull OfflinePlayer offlinePlayer) {
        if (offlinePlayer.hasPlayedBefore())
            addPlayer(offlinePlayer.getUniqueId());
    }

    public void addPlayer(@NotNull UUID uuid) {
        if (isPlayerOnSide(uuid)) return;

        playersIncludingOffline.add(uuid);

        if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
            final @Nullable Player p = Bukkit.getPlayer(uuid);
            addOnlinePlayer(p);
        }
    }

    public void addOnlinePlayer(Player p) {
        players.add(p);
    }

    public void removePlayer(@NotNull Player p) {
        removePlayer(p.getUniqueId());
    }

    public void removePlayer(@NotNull OfflinePlayer offlinePlayer) {
        if (offlinePlayer.hasPlayedBefore())
            removePlayer(offlinePlayer.getUniqueId());
    }

    public void removePlayer(@NotNull UUID uuid) {
        if (!isPlayerOnSide(uuid)) return;

        playersIncludingOffline.remove(uuid);

        if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
            final @Nullable Player p = Bukkit.getPlayer(uuid);
            removeOnlinePlayer(p);
        }
    }

    public void removeOnlinePlayer(Player p) {
        players.remove(p);
    }

    public void surrenderPlayer(@NotNull Player p) {
        surrenderPlayer(p.getUniqueId());
    }

    public void surrenderPlayer(@NotNull UUID uuid) {
        if (isPlayerSurrendered(uuid)) return;

        removePlayer(uuid);
        surrenderedPlayersIncludingOffline.add(uuid);
        applyNameTags();
    }

    public void calculateOnlinePlayers() {
        final @NotNull Set<Player> onlinePlayers = getPlayersIncludingOffline().stream()
            .filter(uuid -> Bukkit.getOfflinePlayer(uuid).isOnline())
            .map(Bukkit::getPlayer)
            .collect(Collectors.toSet());

        this.players.clear();
        this.players.addAll(onlinePlayers);

        applyNameTags();
    }

    public void applyNameTags() {
        players.forEach(PlayerJoinListener::checkPlayer);
        surrenderedPlayersIncludingOffline.stream()
            .filter((UUID uuid) -> Bukkit.getPlayer(uuid) != null)
            .map(Bukkit::getPlayer)
            .toList()
            .forEach(PlayerJoinListener::checkPlayer);
    }

    // Graces

    public Instant getSiegeGrace() {
        return this.siegeGrace;
    }

    public void setSiegeGrace() {
        this.siegeGrace = Instant.now().plus(SIEGE_COOLDOWN);
    }

    public boolean isSiegeGraceActive() {
        return this.siegeGrace.isAfter(Instant.now());
    }

    public Duration getSiegeGraceCooldown() {
        return Duration.between(Instant.now(), this.siegeGrace);
    }

    public Instant getRaidGrace() {
        return this.raidGrace;
    }

    public void setRaidGrace() {
        this.raidGrace = Instant.now().plus(RAID_COOLDOWN);
    }

    public boolean isRaidGraceActive() {
        return this.raidGrace.isAfter(Instant.now());
    }

    public Duration getRaidGraceCooldown() {
        return Duration.between(Instant.now(), this.raidGrace);
    }

    // Score

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int add) {
        this.score += add;
    }

    // Comparators & UUID

    public boolean equals(UUID uuid) {
        return this.uuid.equals(uuid);
    }

    public boolean equals(@NotNull War war) {
        return this.warUUID.equals(war.getUUID());
    }

    public boolean equals(@NotNull Side side) {
        return getUUID().equals(side.getUUID());
    }

    public boolean equals(String sideName) {
        return this.name.equals(sideName);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    @NotNull
    @SuppressWarnings({"all"})
    public War getWar() {
        return WarManager.getInstance().getWar(warUUID); // The war must exist, or this object wouldn't
    }

    public void processSurrenders() {
        if (shouldSurrender())
            surrenderWar();
    }

    private void surrenderWar() {
        getWar().defeat(this);
    }

    private boolean shouldSurrender() {
        return nations.isEmpty() && towns.isEmpty();
    }

    public Town getTown() {
        return town;
    }

    // TODO Make object serializable
    /*private void writeObject(ObjectOutputStream oos)
        throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(address.getHouseNumber());
    }

    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        Integer houseNumber = (Integer) ois.readObject();
        Address a = new Address();
        a.setHouseNumber(houseNumber);
        this.setAddress(a);
    }*/
}
