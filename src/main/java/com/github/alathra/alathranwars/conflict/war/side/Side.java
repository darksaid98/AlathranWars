package com.github.alathra.alathranwars.conflict.war.side;

import com.github.alathra.alathranwars.conflict.IAssociatedWar;
import com.github.alathra.alathranwars.conflict.IUnique;
import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.enums.battle.BattleSide;
import com.github.alathra.alathranwars.enums.battle.BattleTeam;
import com.github.alathra.alathranwars.hook.NameColorHandler;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class Side extends AbstractSideTeamManager implements IUnique<Side>, IAssociatedWar {
    private static final Duration SIEGE_COOLDOWN = Duration.ofMinutes(30);
    private static final Duration RAID_COOLDOWN = Duration.ofMinutes(30);
    private final UUID warUUID;
    private final UUID uuid;
    private final BattleSide side;
    private final BattleTeam team;
    private final String name; // A town name

    private final Town town; // The initiating town or capital of the nation
    private Instant siegeGrace = Instant.now().minus(SIEGE_COOLDOWN); // The time after which this side can be besieged
    private Instant raidGrace = Instant.now().minus(RAID_COOLDOWN); // The time after which this side can be raided

    private int score = 0;

    /**
     * Instantiates a existing Side.
     * @throws SideCreationException exception
     */
    @ApiStatus.Internal
    Side(
        UUID warUUID,
        UUID uuid,
        Government government,
        BattleSide side,
        BattleTeam team,
        String name,
        Set<Nation> nations,
        Set<Town> towns,
        Set<OfflinePlayer> players,
        Set<Nation> nationsSurrendered,
        Set<Town> townsSurrendered,
        Set<OfflinePlayer> playersSurrendered,
        Instant siegeGrace,
        Instant raidGrace
    ) throws SideCreationException {
        super(nations, towns, players, nationsSurrendered, townsSurrendered, playersSurrendered);
        this.warUUID = warUUID;
        this.uuid = uuid;
        this.side = side;
        this.team = team;
        this.name = name;
        this.siegeGrace = siegeGrace;
        this.raidGrace = raidGrace;

        if (government instanceof Nation nation) {
            this.town = nation.getCapital();
        } else if (government instanceof Town town2) {
            this.town = town2;
        } else {
            throw new SideCreationException("No town or nation specified!");
        }
    }

    /**
     * Instantiates a new Side.
     * @throws SideCreationException exception
     */
    @ApiStatus.Internal
    Side(
        UUID warUUID,
        UUID uuid,
        Government government,
        BattleSide side,
        BattleTeam team
    ) throws SideCreationException {
        super();
        this.warUUID = warUUID;
        this.uuid = uuid;
        this.side = side;
        this.team = team;

        if (government instanceof Nation nation) {
            this.town = nation.getCapital();
            add(nation);
        } else if (government instanceof Town town2) {
            this.town = town2;
            add(town2);
        } else {
            throw new SideCreationException("No town or nation specified!");
        }

        this.name = this.town.getName();
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

    // Participant management

    public void applyNameTags() {
        getPlayersOnlineAll().forEach(
            player -> NameColorHandler.getInstance().calculatePlayerColors(player)
        );
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

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean equals(UUID uuid) {
        return getUUID().equals(uuid);
    }

    public boolean equals(Side side) {
        return getUUID().equals(side.getUUID());
    }

    public boolean equals(String sideName) {
        return this.name.equals(sideName);
    }

    // Associated war

    @Override
    public boolean equals(War war) {
        return this.warUUID.equals(war.getUUID());
    }

    @Override
    public War getWar() {
        return WarController.getInstance().getWar(warUUID);
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

    public Town getTown() {
        return town;
    }

    // Player management

    @Override
    public void add(OfflinePlayer p) {
        super.add(p);

        // Add player to battles
        getWar().getSieges().forEach(siege -> {
            if (siege.getAttackerSide().equals(this)) {
                siege.addPlayer(p, BattleSide.ATTACKER);
            } else if (siege.getDefenderSide().equals(this)) {
                siege.addPlayer(p, BattleSide.DEFENDER);
            }
        });
        // TODO Raids
        /*getWar().getRaids().forEach(raid -> {
            if (raid.getAttackerSide().equals(this)) {
                raid.addPlayer(p, BattleSide.ATTACKER);
            } else if (raid.getDefenderSide().equals(this)) {
                raid.addPlayer(p, BattleSide.DEFENDER);
            }
        });*/
    }

    @Override
    public void remove(OfflinePlayer p) {
        super.remove(p);

        // Remove player from battles
        getWar().getSieges().forEach(siege -> {
            if (siege.getAttackerSide().equals(this)) {
                siege.removePlayer(p, BattleSide.ATTACKER);
            } else if (siege.getDefenderSide().equals(this)) {
                siege.removePlayer(p, BattleSide.DEFENDER);
            }
        });
        // TODO Raids
        /*getWar().getRaids().forEach(raid -> {
            if (raid.getAttackerSide().equals(this)) {
                raid.removePlayer(p, BattleSide.ATTACKER);
            } else if (raid.getDefenderSide().equals(this)) {
                raid.removePlayer(p, BattleSide.DEFENDER);
            }
        });*/
    }
}
