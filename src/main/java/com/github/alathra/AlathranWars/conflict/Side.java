package com.github.alathra.AlathranWars.conflict;

import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.battle.BattleSide;
import com.github.alathra.AlathranWars.enums.battle.BattleTeam;
import com.github.alathra.AlathranWars.hooks.NameColorHandler;
import com.palmergames.bukkit.towny.object.Government;
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
    private static final Duration SIEGE_COOLDOWN = Duration.ofMinutes(30);
    private static final Duration RAID_COOLDOWN = Duration.ofMinutes(30);
    private final UUID warUUID;
    private final UUID uuid;
    private final BattleSide side;
    private final BattleTeam team;
    private final String name; // A town name

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
        Government government,
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
    ) throws SideCreationException {
        this.warUUID = warUUID;
        this.uuid = uuid;
        this.side = side;
        this.team = team;
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

        if (government instanceof Nation nation) {
            this.town = nation.getCapital();
        } else if (government instanceof Town town2) {
            this.town = town2;
        } else {
            throw new SideCreationException("No town or nation specified!");
        }

        calculateOnlinePlayers();
    }

    /*public Side(UUID warUUID, UUID uuid, Town town, boolean isNation, BattleSide side, BattleTeam team) {
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

        if (isNation)
            addNation(Objects.requireNonNull(this.town.getNationOrNull()));
        else
            addTown(this.town);

        calculateOnlinePlayers();
    }*/

    public Side(UUID warUUID, UUID uuid, Government government, BattleSide side, BattleTeam team) throws SideCreationException {
        this.warUUID = warUUID;
        this.uuid = uuid;
        this.side = side;
        this.team = team;

        this.towns = new HashSet<>();
        this.nations = new HashSet<>();
        this.playersIncludingOffline = new HashSet<>();
        this.players = new HashSet<>();

        this.surrenderedTowns = new HashSet<>();
        this.surrenderedNations = new HashSet<>();
        this.surrenderedPlayersIncludingOffline = new HashSet<>();

        if (government instanceof Nation nation) {
            this.town = nation.getCapital();
            addNation(nation);
        } else if (government instanceof Town town2) {
            this.town = town2;
            addTown(town2);
        } else {
            throw new SideCreationException("No town or nation specified!");
        }

        this.name = this.town.getName();

        calculateOnlinePlayers();
    }

    /*public Side(UUID warUUID, UUID uuid, Town town, BattleSide side, BattleTeam team) {
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
    }*/

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

    public void unsurrenderTown(@NotNull Town town) {
        if (!isTownSurrendered(town)) return;

        addTown(town);

        surrenderedTowns.remove(town);

        town.getResidents().forEach((Resident resident) -> unsurrenderPlayer(resident.getUUID()));
    }

    public void unsurrenderNation(@NotNull Nation nation) {
        if (!isNationSurrendered(nation)) return;

        addNation(nation);

        surrenderedNations.remove(nation);

        nation.getTowns().forEach(this::unsurrenderTown);
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
        if (getWar() != null) { // We need to check this as on side creation War doesn't exist
            for (@NotNull Siege siege : getWar().getSieges()) {
                if (siege.getAttackerSide().equals(this)) {
                    siege.addPlayer(uuid, BattleSide.ATTACKER);
                } else {
                    siege.addPlayer(uuid, BattleSide.DEFENDER);
                }
            }
        }

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
        if (getWar() != null) { // We need to check this as on side creation War doesn't exist
            for (@NotNull Siege siege : getWar().getSieges()) {
                if (siege.getAttackerSide().equals(this)) {
                    siege.removePlayer(uuid);
                } else {
                    siege.removePlayer(uuid);
                }
            }
        }

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

    public void unsurrenderPlayer(@NotNull Player p) {
        unsurrenderPlayer(p.getUniqueId());
    }

    public void unsurrenderPlayer(@NotNull UUID uuid) {
        if (!isPlayerSurrendered(uuid)) return;

        addPlayer(uuid);
        surrenderedPlayersIncludingOffline.remove(uuid);
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
        players.forEach(p -> NameColorHandler.getInstance().getPlayerNameColor(p));
        surrenderedPlayersIncludingOffline.stream()
            .filter((UUID uuid) -> Bukkit.getPlayer(uuid) != null)
            .map(Bukkit::getPlayer)
            .toList()
            .forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));
    }

    // Graces

    public Instant getSiegeGrace() {
        return siegeGrace;
    }

    public void setSiegeGrace() {
        siegeGrace = Instant.now().plus(SIEGE_COOLDOWN);
    }

    public boolean isSiegeGraceActive() {
        return siegeGrace.isAfter(Instant.now());
    }

    public Duration getSiegeGraceCooldown() {
        return Duration.between(Instant.now(), siegeGrace);
    }

    public Instant getRaidGrace() {
        return raidGrace;
    }

    public void setRaidGrace() {
        raidGrace = Instant.now().plus(RAID_COOLDOWN);
    }

    public boolean isRaidGraceActive() {
        return raidGrace.isAfter(Instant.now());
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

    @Nullable
    public War getWar() {
        return WarController.getInstance().getWar(warUUID); // The war must exist, or this object wouldn't
    }

    public void processSurrenders() {
        if (shouldSurrender())
            surrenderWar();
    }

    private void surrenderWar() {
        War war = getWar();
        if (war != null)
            getWar().defeat(this);
    }

    private boolean shouldSurrender() {
        return nations.isEmpty() && towns.isEmpty();
    }

    public boolean shouldNationSurrender(Nation nation) {
        final int townsQty = nation.getNumTowns();
//        List<Town> towns = nation.getTowns();

        final int surrenderedTownsQty = nation.getTowns().stream().filter(this::isTownSurrendered).mapToInt(value -> 1).sum();

        /*int surrenderedTownsQty = 0;
        for (Town town : towns) {
            if (isTownSurrendered(town))
                surrenderedTownsQty++;
        }*/

        return surrenderedTownsQty == townsQty;
    }

    public Town getTown() {
        return town;
    }
}
