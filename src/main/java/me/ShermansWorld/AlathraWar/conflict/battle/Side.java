package me.ShermansWorld.AlathraWar.conflict.battle;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.conflict.War;
import me.ShermansWorld.AlathraWar.enums.BattleSide;
import me.ShermansWorld.AlathraWar.enums.BattleTeam;
import me.ShermansWorld.AlathraWar.holder.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Side {
    private UUID warUUID;
    private UUID uuid;
    private final BattleSide side;
    private final BattleTeam team;
    private final String name; // A town or nation name

    // TODO Use Town & Nation UUID's when saving to config
    private final Town town; // The initiating town or capital of the nation
    private final Set<Town> towns; // A list of participating Towns
    private final Set<Nation> nations; // A list of participating Nations
    private final Set<UUID> playersIncludingOffline; // A list of all participating player UUID, (Online & Offline)
    private final Set<Player> players; // A list of the players who are currently online
    // TODO Figure out how to work this stuff, FYI Towns and Nations have UUID's

    private final Set<Town> surrenderedTowns;
    private final Set<Nation> surrenderedNations;
    private final Set<UUID> surrenderedPlayersIncludingOffline;

    private static final Duration SIEGE_COOLDOWN = Duration.ofMinutes(120);
    private static final Duration RAID_COOLDOWN = Duration.ofMinutes(30);
    private Instant siegeGrace = Instant.EPOCH; // The time after which this side can be besieged
    private Instant raidGrace = Instant.EPOCH; // The time after which this side can be raided

    private int score = 0;

    public Side(UUID warUUID, Town town, BattleSide side, BattleTeam team) {
        this.warUUID = warUUID;
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

    public Side(UUID warUUID, Nation nation, BattleSide side, BattleTeam team) {
        this.warUUID = warUUID;
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

    public Set<Player> getPlayers() {
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

    public void addTown(Town town) {
        if (isTownOnSide(town)) return;

        towns.add(town);

        town.getResidents().forEach((Resident resident) -> addPlayer(resident.getUUID()));
    }

    public void addNation(Nation nation) {
        if (isNationOnSide(nation)) return;

        nations.add(nation);

        nation.getTowns().forEach(this::addTown);
    }

    public void removeTown(Town town) {
        if (!isTownOnSide(town)) return;

        towns.remove(town);

        town.getResidents().forEach((Resident resident) -> removePlayer(resident.getUUID()));
    }

    public void removeNation(Nation nation) {
        if (!isNationOnSide(nation)) return;

        nations.remove(nation);

        nation.getTowns().forEach(this::removeTown);
    }

    public void surrenderTown(Town town) {
        if (isTownSurrendered(town)) return;

        towns.remove(town);

        town.getResidents().forEach((Resident resident) -> surrenderPlayer(resident.getUUID()));

        surrenderedTowns.add(town);
    }

    public void surrenderNation(Nation nation) {
        if (isNationSurrendered(nation)) return;

        nations.remove(nation);

        nation.getTowns().forEach(this::surrenderTown);

        surrenderedNations.add(nation);
    }

    public boolean isPlayerOnSide(Player p) {
        return isPlayerOnSide(p.getUniqueId());
    }

    public boolean isPlayerOnSide(UUID uuid) {
        return playersIncludingOffline.contains(uuid) || surrenderedPlayersIncludingOffline.contains(uuid);
    }

    public boolean isPlayerSurrendered(Player p) {
        return isPlayerSurrendered(p.getUniqueId());
    }

    public boolean isPlayerSurrendered(UUID uuid) {
        return surrenderedPlayersIncludingOffline.contains(uuid);
    }

    public void addPlayer(Player p) {
        if (isPlayerOnSide(p)) return;

        playersIncludingOffline.add(p.getUniqueId());
        calculateOnlinePlayers();
    }

    public void addPlayer(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline())
            addPlayer(offlinePlayer.getPlayer());

        if (offlinePlayer.hasPlayedBefore()) {
            if (isPlayerOnSide(offlinePlayer.getUniqueId())) return;
            playersIncludingOffline.add(offlinePlayer.getUniqueId());
            calculateOnlinePlayers();
        }
    }

    public void addPlayer(UUID uuid) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        addPlayer(offlinePlayer);
    }

    public void removePlayer(Player p) {
        playersIncludingOffline.remove(p.getUniqueId());
        calculateOnlinePlayers();
    }

    public void removePlayer(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline())
            removePlayer(offlinePlayer.getPlayer());

        if (offlinePlayer.hasPlayedBefore()) {
            playersIncludingOffline.remove(offlinePlayer.getUniqueId());
            calculateOnlinePlayers();
        }
    }

    public void removePlayer(UUID uuid) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        removePlayer(offlinePlayer);
    }

    public void surrenderPlayer(Player p) {
        surrenderPlayer(p.getUniqueId());
    }

    public void surrenderPlayer(UUID uuid) {
        if (isPlayerSurrendered(uuid)) return;

        removePlayer(uuid);
        surrenderedPlayersIncludingOffline.add(uuid);
    }

    public void calculateOnlinePlayers() {
        final Set<UUID> players = getPlayersIncludingOffline();
        final Set<Player> onlinePlayers = new HashSet<>();

        for (UUID uuid : players) {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.isOnline())
                onlinePlayers.add(offlinePlayer.getPlayer());
        }

        this.players.clear();
        this.players.addAll(onlinePlayers);
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

    private UUID generateUUID() {
        UUID uuid = UUID.randomUUID();

        while (getWar().getSide(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        return UUID.randomUUID();
    }

    public boolean equals(UUID uuid) {
        return this.uuid.equals(uuid);
    }

    public boolean equals(War war) {
        return this.warUUID.equals(war.getUUID());
    }

    public boolean equals(Side side) {
        return this.uuid.equals(side.getUUID());
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
