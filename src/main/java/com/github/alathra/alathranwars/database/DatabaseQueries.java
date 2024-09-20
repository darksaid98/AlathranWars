package com.github.alathra.alathranwars.database;

import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.battle.siege.SiegePhase;
import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.WarBuilder;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.conflict.war.WarCreationException;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.conflict.war.side.SideBuilder;
import com.github.alathra.alathranwars.conflict.war.side.SideCreationException;
import com.github.alathra.alathranwars.enums.battle.BattleSide;
import com.github.alathra.alathranwars.enums.battle.BattleTeam;
import com.github.alathra.alathranwars.utility.DB;
import com.github.alathra.alathranwars.utility.Logger;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.alathra.alathranwars.database.schema.Tables.*;
import static com.github.alathra.alathranwars.database.QueryUtil.*;

/**
 * A holder class for all SQL queries
 */
public abstract class DatabaseQueries {
    // Setters

    public static void saveAll() {
        try (
            Connection con = DB.getConnection();
        ) {
            DSLContext context = DB.getContext(con);

            for (War war : WarController.getInstance().getWars()) {
                context
                    .insertInto(LIST, LIST.UUID, LIST.NAME, LIST.LABEL, LIST.SIDE1, LIST.SIDE2, LIST.EVENT)
                    .values(
                        fromUUIDToBytes(war.getUUID()),
                        war.getName(),
                        war.getLabel(),
                        fromUUIDToBytes(war.getSide1().getUUID()),
                        fromUUIDToBytes(war.getSide2().getUUID()),
                        war.isEventWar() ? (byte) 1 : (byte) 0
                    )
                    .onDuplicateKeyUpdate()
                    .set(LIST.NAME, war.getName())
                    .set(LIST.LABEL, war.getLabel())
                    .set(LIST.SIDE1, fromUUIDToBytes(war.getSide1().getUUID()))
                    .set(LIST.SIDE2, fromUUIDToBytes(war.getSide2().getUUID()))
                    .execute();

                // Save sides
                for (Side side : war.getSides()) {
                    saveSide(con, side);
                }

                // Save sieges
                for (Siege siege : war.getSieges()) {
                    saveSiege(con, siege);
                }
            }
        } catch (SQLException e) {
            Logger.get().error("SQL Query failed to save data!", e);
        }
    }

    public static void saveSide(Connection con, Side side) {
        try {
            DSLContext context = DB.getContext(con);

            context
                .insertInto(SIDES, SIDES.WAR, SIDES.UUID, SIDES.SIDE, SIDES.TEAM, SIDES.NAME, SIDES.TOWN, SIDES.SIEGE_GRACE, SIDES.RAID_GRACE)
                .values(
                    fromUUIDToBytes(side.getWar().getUUID()),
                    fromUUIDToBytes(side.getUUID()),
                    side.getSide().toString(),
                    side.getTeam().toString(),
                    side.getName(),
                    fromUUIDToBytes(side.getTown().getUUID()),
                    LocalDateTime.ofInstant(side.getSiegeGrace(), ZoneOffset.UTC),
                    LocalDateTime.ofInstant(side.getRaidGrace(), ZoneOffset.UTC)
                )
                .onDuplicateKeyUpdate()
                .set(SIDES.SIDE, side.getSide().toString())
                .set(SIDES.TEAM, side.getTeam().toString())
                .set(SIDES.NAME, side.getName())
                .set(SIDES.TOWN, fromUUIDToBytes(side.getTown().getUUID()))
                .set(SIDES.SIEGE_GRACE, LocalDateTime.ofInstant(side.getSiegeGrace(), ZoneOffset.UTC))
                .set(SIDES.RAID_GRACE, LocalDateTime.ofInstant(side.getRaidGrace(), ZoneOffset.UTC))
                .execute();

            saveSideNations(con, side);
            saveSideTowns(con, side);
            saveSidePlayers(con, side);
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query failed to save side!", e);
        }
    }

    public static void saveSideNations(Connection con, Side side) {
        try {
            transaction(con, (connection, context) -> {
                context
                    .deleteFrom(SIDES_NATIONS)
                    .where(SIDES_NATIONS.SIDE.equal(fromUUIDToBytes(side.getUUID())))
                    .execute();

                for (Nation nation : side.getNations()) {
                    context.insertInto(SIDES_NATIONS, SIDES_NATIONS.SIDE, SIDES_NATIONS.NATION, SIDES_NATIONS.SURRENDERED)
                        .values(
                            fromUUIDToBytes(side.getUUID()),
                            fromUUIDToBytes(nation.getUUID()),
                            (byte) 0
                        )
                        .execute();
                }

                for (Nation nation : side.getNationsSurrendered()) {
                    context.insertInto(SIDES_NATIONS, SIDES_NATIONS.SIDE, SIDES_NATIONS.NATION, SIDES_NATIONS.SURRENDERED)
                        .values(
                            fromUUIDToBytes(side.getUUID()),
                            fromUUIDToBytes(nation.getUUID()),
                            (byte) 1
                        )
                        .execute();
                }
            });
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query failed to save side nations!", e);
        }
    }

    public static void saveSideTowns(Connection con, Side side) {
        try {
            transaction(con, (connection, context) -> {
                context
                    .deleteFrom(SIDES_TOWNS)
                    .where(SIDES_TOWNS.SIDE.equal(fromUUIDToBytes(side.getUUID())))
                    .execute();

                for (Town town : side.getTowns()) {
                    context.insertInto(SIDES_TOWNS, SIDES_TOWNS.SIDE, SIDES_TOWNS.TOWN, SIDES_TOWNS.SURRENDERED)
                        .values(
                            fromUUIDToBytes(side.getUUID()),
                            fromUUIDToBytes(town.getUUID()),
                            (byte) 0
                        )
                        .execute();
                }

                for (Town town : side.getTownsSurrendered()) {
                    context.insertInto(SIDES_TOWNS, SIDES_TOWNS.SIDE, SIDES_TOWNS.TOWN, SIDES_TOWNS.SURRENDERED)
                        .values(
                            fromUUIDToBytes(side.getUUID()),
                            fromUUIDToBytes(town.getUUID()),
                            (byte) 1
                        )
                        .execute();
                }
            });
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query failed to save side towns!", e);
        }
    }

    public static void saveSidePlayers(Connection con, Side side) {
        try {
            transaction(con, (connection, context) -> {
                context
                    .deleteFrom(SIDES_PLAYERS)
                    .where(SIDES_PLAYERS.SIDE.equal(fromUUIDToBytes(side.getUUID())))
                    .execute();

                for (OfflinePlayer p : side.getPlayersAll()) {
                    context.insertInto(SIDES_PLAYERS, SIDES_PLAYERS.SIDE, SIDES_PLAYERS.PLAYER, SIDES_PLAYERS.SURRENDERED)
                        .values(
                            fromUUIDToBytes(side.getUUID()),
                            fromUUIDToBytes(p.getUniqueId()),
                            (byte) 0
                        )
                        .execute();
                }

                for (OfflinePlayer p : side.getPlayersSurrendered()) {
                    context.insertInto(SIDES_PLAYERS, SIDES_PLAYERS.SIDE, SIDES_PLAYERS.PLAYER, SIDES_PLAYERS.SURRENDERED)
                        .values(
                            fromUUIDToBytes(side.getUUID()),
                            fromUUIDToBytes(p.getUniqueId()),
                            (byte) 1
                        )
                        .execute();
                }
            });
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query failed to save side players!", e);
        }
    }

    private static void saveSiege(Connection con, Siege siege) {
        try {
            DSLContext context = DB.getContext(con);

            context.insertInto(SIEGES, SIEGES.WAR, SIEGES.UUID, SIEGES.TOWN, SIEGES.SIEGE_LEADER, SIEGES.END_TIME, SIEGES.LAST_TOUCHED, SIEGES.SIEGE_PROGRESS, SIEGES.PHASE_CURRENT, SIEGES.PHASE_PROGRESS, SIEGES.PHASE_START_TIME)
                .values(
                    fromUUIDToBytes(siege.getWar().getUUID()),
                    fromUUIDToBytes(siege.getUUID()),
                    fromUUIDToBytes(siege.getTown().getUUID()),
                    fromUUIDToBytes(siege.getSiegeLeader().getUniqueId()),
                    LocalDateTime.ofInstant(siege.getEndTime(), ZoneOffset.UTC),
                    LocalDateTime.ofInstant(siege.getLastTouched(), ZoneOffset.UTC),
                    siege.getProgressManager().get(),
                    siege.getPhaseManager().get().name(),
                    siege.getPhaseManager().getProgress(),
                    LocalDateTime.ofInstant(siege.getPhaseManager().getStartTime(), ZoneOffset.UTC)
                )
                .onDuplicateKeyUpdate()
                .set(SIEGES.WAR, fromUUIDToBytes(siege.getWar().getUUID()))
                .set(SIEGES.TOWN, fromUUIDToBytes(siege.getTown().getUUID()))
                .set(SIEGES.SIEGE_LEADER, fromUUIDToBytes(siege.getSiegeLeader().getUniqueId()))
                .set(SIEGES.END_TIME, LocalDateTime.ofInstant(siege.getEndTime(), ZoneOffset.UTC))
                .set(SIEGES.LAST_TOUCHED, LocalDateTime.ofInstant(siege.getLastTouched(), ZoneOffset.UTC))
                .set(SIEGES.SIEGE_PROGRESS, siege.getProgressManager().get())
                .set(SIEGES.PHASE_CURRENT, siege.getPhaseManager().get().name())
                .set(SIEGES.PHASE_PROGRESS, siege.getPhaseManager().getProgress())
                .set(SIEGES.PHASE_START_TIME, LocalDateTime.ofInstant(siege.getPhaseManager().getStartTime(), ZoneOffset.UTC))
                .execute();

            saveSiegePlayers(con, siege);
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }

    public static void saveSiegePlayers(Connection con, Siege siege) {
        try {
            transaction(con, (connection, context) -> {
                context
                    .deleteFrom(SIEGE_PLAYERS)
                    .where(SIEGE_PLAYERS.SIEGE.equal(fromUUIDToBytes(siege.getUUID())))
                    .execute();

                for (UUID uuid : siege.getPlayers(BattleSide.ATTACKER).stream().map(OfflinePlayer::getUniqueId).toList()) {
                    context.insertInto(SIEGE_PLAYERS, SIEGE_PLAYERS.SIEGE, SIEGE_PLAYERS.PLAYER, SIEGE_PLAYERS.TEAM)
                        .values(
                            fromUUIDToBytes(siege.getUUID()),
                            fromUUIDToBytes(uuid),
                            (byte) 0
                        )
                        .execute();
                }

                for (UUID uuid : siege.getPlayers(BattleSide.DEFENDER).stream().map(OfflinePlayer::getUniqueId).toList()) {
                    context.insertInto(SIEGE_PLAYERS, SIEGE_PLAYERS.SIEGE, SIEGE_PLAYERS.PLAYER, SIEGE_PLAYERS.TEAM)
                        .values(
                            fromUUIDToBytes(siege.getUUID()),
                            fromUUIDToBytes(uuid),
                            (byte) 1
                        )
                        .execute();
                }
            });
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }

    // Getters
    public static @NotNull Set<War> loadAll() {
        Set<War> wars = new HashSet<>();

        try (
            Connection con = DB.getConnection();
        ) {
            DSLContext context = DB.getContext(con);

            Result<Record> result = context.select()
                .from(LIST)
                .fetch();

            for (Record r : result) {
                UUID uuid = fromBytesToUUID(r.get(LIST.UUID));
                String name = r.get(LIST.NAME);
                String label = r.get(LIST.LABEL);
                @Nullable Side side1 = loadSide(con, uuid, fromBytesToUUID(r.get(LIST.SIDE1)));
                @Nullable Side side2 = loadSide(con, uuid, fromBytesToUUID(r.get(LIST.SIDE2)));
                boolean event = r.get(LIST.EVENT, Boolean.class);

                wars.add(
                    new WarBuilder()
                        .setUuid(uuid)
                        .setName(name)
                        .setLabel(label)
                        .setSide1(side1)
                        .setSide2(side2)
                        .setSieges(new HashSet<>())
                        .setRaids(new HashSet<>())
                        .setEvent(event)
                        .resume()
                );
            }

            for (War war : wars) {
                @NotNull Set<Siege> sieges = loadSieges(con, war);
                war.setSieges(sieges);
            }
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        } catch (WarCreationException e) {
            Logger.get().error("Failed to re-create war from database!", e);
        }

        return wars;
    }

    @Contract("_, _, _ -> new")
    public static @Nullable Side loadSide(Connection con, UUID warUUID, UUID uuid) {
        try {
            DSLContext context = DB.getContext(con);

            Record r = context.select()
                .from(SIDES)
                .where(SIDES.WAR.equal(fromUUIDToBytes(warUUID)))
                .and(SIDES.UUID.equal(fromUUIDToBytes(uuid)))
                .fetchOne();

            if (r == null) return null;

            // Side data
            BattleSide side = BattleSide.valueOf(r.get(SIDES.SIDE));
            BattleTeam team = BattleTeam.valueOf(r.get(SIDES.TEAM));
            String name = r.get(SIDES.NAME);
            @Nullable Town town = TownyAPI.getInstance().getTown(fromBytesToUUID(r.get(SIDES.TOWN)));
            Instant siegeGrace = r.get(SIDES.SIEGE_GRACE).toInstant(ZoneOffset.UTC);
            Instant raidGrace = r.get(SIDES.RAID_GRACE).toInstant(ZoneOffset.UTC);

            // Load players
            Set<UUID> players = new HashSet<>();
            Set<UUID> playersSurrendered = new HashSet<>();
            Result<Record> resultPlayers = context.select()
                .from(SIDES_PLAYERS)
                .where(SIDES_PLAYERS.SIDE.equal(fromUUIDToBytes(uuid)))
                .fetch();
            resultPlayers.forEach(
                record -> {
                    if (record.get(SIDES_PLAYERS.SURRENDERED).equals((byte) 0)) {
                        players.add(fromBytesToUUID(record.get(SIDES_PLAYERS.PLAYER)));
                    } else {
                        playersSurrendered.add(fromBytesToUUID(record.get(SIDES_PLAYERS.PLAYER)));
                    }
                }
            );

            // Load nations
            Set<Nation> nations = new HashSet<>();
            Set<Nation> nationsSurrendered = new HashSet<>();
            Result<Record> resultNations = context.select()
                .from(SIDES_NATIONS)
                .where(SIDES_NATIONS.SIDE.equal(fromUUIDToBytes(uuid)))
                .fetch();
            resultNations.forEach(
                record -> {
                    final UUID identifier = fromBytesToUUID(record.get(SIDES_NATIONS.NATION));
                    if (record.get(SIDES_NATIONS.SURRENDERED).equals((byte) 0)) {
                        @Nullable Nation nation = TownyAPI.getInstance().getNation(identifier);
                        if (nation != null)
                            nations.add(nation);
                    } else {
                        @Nullable Nation nation = TownyAPI.getInstance().getNation(identifier);
                        if (nation != null)
                            nationsSurrendered.add(nation);
                    }
                }
            );

            // Load towns
            Set<Town> towns = new HashSet<>();
            Set<Town> townsSurrendered = new HashSet<>();
            Result<Record> resultTowns = context.select()
                .from(SIDES_TOWNS)
                .where(SIDES_TOWNS.SIDE.equal(fromUUIDToBytes(uuid)))
                .fetch();
            resultTowns.forEach(
                record -> {
                    final UUID identifier = fromBytesToUUID(record.get(SIDES_TOWNS.TOWN));
                    if (record.get(SIDES_TOWNS.SURRENDERED).equals((byte) 0)) {
                        @Nullable Town town2 = TownyAPI.getInstance().getTown(identifier);
                        if (town2 != null)
                            towns.add(town2);
                    } else {
                        @Nullable Town town2 = TownyAPI.getInstance().getTown(identifier);
                        if (town2 != null)
                            townsSurrendered.add(town2);
                    }
                }
            );

            return new SideBuilder() // TODO IllegalStateException
                .setWarUUID(warUUID)
                .setUuid(uuid)
                .setLeader(town)
                .setSide(side)
                .setTeam(team)
                .setName(name)
                .setTowns(towns)
                .setNations(nations)
                .setPlayers(players)
                .setTownsSurrendered(townsSurrendered)
                .setNationsSurrendered(nationsSurrendered)
                .setPlayersSurrendered(playersSurrendered)
                .setSiegeGrace(siegeGrace)
                .setRaidGrace(raidGrace)
                .rebuild();
        } catch (SideCreationException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
        return null;
    }

    public static @NotNull Set<Siege> loadSieges(Connection con, War war) {
        Set<Siege> sieges = new HashSet<>();

        try {
            DSLContext context = DB.getContext(con);

            Result<Record> result = context.select()
                .from(SIEGES)
                .where(SIEGES.WAR.equal(fromUUIDToBytes(war.getUUID())))
                .fetch();

            for (Record r : result) {
                UUID uuid = fromBytesToUUID(r.get(SIEGES.UUID));
                @Nullable Town town = TownyAPI.getInstance().getTown(fromBytesToUUID(r.get(SIEGES.TOWN)));
                OfflinePlayer siegeLeader = Bukkit.getOfflinePlayer(fromBytesToUUID(r.get(SIEGES.SIEGE_LEADER)));
                Instant endTime = r.get(SIEGES.END_TIME).toInstant(ZoneOffset.UTC);
                Instant lastTouched = r.get(SIEGES.LAST_TOUCHED).toInstant(ZoneOffset.UTC);
                int siegeProgress = r.get(SIEGES.SIEGE_PROGRESS);
                Set<UUID> attackersIncludingOffline = new HashSet<>();
                Set<UUID> defendersIncludingOffline = new HashSet<>();

                Result<Record> resultPlayers = context.select()
                    .from(SIEGE_PLAYERS)
                    .where(SIEGE_PLAYERS.SIEGE.equal(fromUUIDToBytes(uuid)))
                    .fetch();
                resultPlayers.forEach(
                    record -> {
                        if (record.get(SIEGE_PLAYERS.TEAM).equals((byte) 0)) {
                            attackersIncludingOffline.add(fromBytesToUUID(record.get(SIEGE_PLAYERS.PLAYER)));
                        } else {
                            defendersIncludingOffline.add(fromBytesToUUID(record.get(SIEGE_PLAYERS.PLAYER)));
                        }
                    }
                );

                SiegePhase phase;
                try {
                    phase = SiegePhase.valueOf(r.get(SIEGES.PHASE_CURRENT));
                } catch (IllegalArgumentException e) {
                    phase = SiegePhase.SIEGE;
                }
                int phaseProgress = r.get(SIEGES.PHASE_PROGRESS);
                Instant phaseStartTime = r.get(SIEGES.PHASE_START_TIME).toInstant(ZoneOffset.UTC);



                sieges.add(new Siege(
                    war,
                    uuid,
                    town,
                    siegeLeader,
                    endTime,
                    lastTouched,
                    siegeProgress,
                    attackersIncludingOffline,
                    defendersIncludingOffline,
                    phase,
                    phaseProgress,
                    phaseStartTime
                ));
            }
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }

        return sieges;
    }

    // Deleters

    public static void deleteWar(War war) {
        try (
            Connection con = DB.getConnection();
        ) {
            DSLContext context = DB.getContext(con);

            context
                .deleteFrom(LIST)
                .where(LIST.UUID.equal(fromUUIDToBytes(war.getUUID())))
                .execute();
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }

    public static void deleteSiege(Siege siege) {
        try (
            Connection con = DB.getConnection();
        ) {
            DSLContext context = DB.getContext(con);

            context
                .deleteFrom(SIEGES)
                .where(SIEGES.UUID.equal(fromUUIDToBytes(siege.getUUID())))
                .execute();
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }
}
