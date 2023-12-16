package com.github.alathra.AlathranWars.db;

import com.github.alathra.AlathranWars.conflict.*;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.battle.BattleSide;
import com.github.alathra.AlathranWars.enums.battle.BattleTeam;
import com.github.alathra.AlathranWars.utility.DB;
import com.github.alathra.AlathranWars.utility.Logger;
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

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.alathra.AlathranWars.db.schema.Tables.*;

/**
 * A holder class for all SQL queries
 */
public abstract class DatabaseQueries {
    private interface LambdaInterface {
        void execute(Connection connection, DSLContext dslContext) throws SQLException, DataAccessException;
    }

    private static void transactionWrapper(Connection con, LambdaInterface lambda) throws SQLException, DataAccessException {
        DSLContext context = DB.getContext(con);

        final Savepoint savePoint = con.setSavepoint();
        final boolean autoCommit = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            lambda.execute(con, context);

            con.commit();
        } catch (SQLException | DataAccessException e) {
            con.rollback(savePoint);
        } finally {
            con.setAutoCommit(autoCommit);
        }
    }

    public static byte[] convertUUIDToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    public static UUID convertBytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }

    // Setters

    public static void saveAll() {
        try (
            Connection con = DB.getConnection();
        ) {
            DSLContext context = DB.getContext(con);

            for (War war : WarManager.getInstance().getWars()) {
                context
//                    .insertInto(LIST)
//                    .set(LIST.UUID, DatabaseQueries.convertUUIDToBytes(war.getUUID()))
//                    .set(LIST.NAME, war.getName())
//                    .set(LIST.LABEL, war.getLabel())
//                    .set(LIST.SIDE1, DatabaseQueries.convertUUIDToBytes(war.getSide1().getUUID()))
//                    .set(LIST.SIDE2, DatabaseQueries.convertUUIDToBytes(war.getSide2().getUUID()))
//                    .set(LIST.EVENT, war.isEvent() ? (byte) 1 : (byte) 0)
                    .insertInto(LIST, LIST.UUID, LIST.NAME, LIST.LABEL, LIST.SIDE1, LIST.SIDE2, LIST.EVENT)
                    .values(
                        DatabaseQueries.convertUUIDToBytes(war.getUUID()),
                        war.getName(),
                        war.getLabel(),
                        DatabaseQueries.convertUUIDToBytes(war.getSide1().getUUID()),
                        DatabaseQueries.convertUUIDToBytes(war.getSide2().getUUID()),
                        war.isEvent() ? (byte) 1 : (byte) 0
                    )
                    .onDuplicateKeyUpdate()
                    .set(LIST.NAME, war.getName())
                    .set(LIST.LABEL, war.getLabel())
                    .set(LIST.SIDE1, DatabaseQueries.convertUUIDToBytes(war.getSide1().getUUID()))
                    .set(LIST.SIDE2, DatabaseQueries.convertUUIDToBytes(war.getSide2().getUUID()))
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
                .insertInto(SIDES, SIDES.WAR, SIDES.UUID, SIDES.SIDE, SIDES.TEAM, SIDES.NAME, SIDES.TOWN, SIDES.SIEGEGRACE, SIDES.RAIDGRACE)
                .values(
                    convertUUIDToBytes(side.getWar().getUUID()),
                    convertUUIDToBytes(side.getUUID()),
                    side.getSide().toString(),
                    side.getTeam().toString(),
                    side.getName(),
                    convertUUIDToBytes(side.getTown().getUUID()),
                    LocalDateTime.ofInstant(side.getSiegeGrace(), ZoneOffset.UTC),
                    LocalDateTime.ofInstant(side.getRaidGrace(), ZoneOffset.UTC)
                )
                .onDuplicateKeyUpdate()
                .set(SIDES.SIDE, side.getSide().toString())
                .set(SIDES.TEAM, side.getTeam().toString())
                .set(SIDES.NAME, side.getName())
                .set(SIDES.TOWN, convertUUIDToBytes(side.getTown().getUUID()))
                .set(SIDES.SIEGEGRACE, LocalDateTime.ofInstant(side.getSiegeGrace(), ZoneOffset.UTC))
                .set(SIDES.RAIDGRACE, LocalDateTime.ofInstant(side.getRaidGrace(), ZoneOffset.UTC))
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
            transactionWrapper(con, (connection, context) -> {
                context
                    .deleteFrom(SIDES_NATIONS)
                    .where(SIDES_NATIONS.SIDE.equal(convertUUIDToBytes(side.getUUID())))
                    .execute();

                for (Nation nation : side.getNations()) {
                    context.insertInto(SIDES_NATIONS, SIDES_NATIONS.SIDE, SIDES_NATIONS.NATION, SIDES_NATIONS.SURRENDERED)
                        .values(
                            convertUUIDToBytes(side.getUUID()),
                            convertUUIDToBytes(nation.getUUID()),
                            (byte) 0
                        )
                        .execute();
                }

                for (Nation nation : side.getSurrenderedNations()) {
                    context.insertInto(SIDES_NATIONS, SIDES_NATIONS.SIDE, SIDES_NATIONS.NATION, SIDES_NATIONS.SURRENDERED)
                        .values(
                            convertUUIDToBytes(side.getUUID()),
                            convertUUIDToBytes(nation.getUUID()),
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
            transactionWrapper(con, (connection, context) -> {
                context
                    .deleteFrom(SIDES_TOWNS)
                    .where(SIDES_TOWNS.SIDE.equal(convertUUIDToBytes(side.getUUID())))
                    .execute();

                for (Town town : side.getTowns()) {
                    context.insertInto(SIDES_TOWNS, SIDES_TOWNS.SIDE, SIDES_TOWNS.TOWN, SIDES_TOWNS.SURRENDERED)
                        .values(
                            convertUUIDToBytes(side.getUUID()),
                            convertUUIDToBytes(town.getUUID()),
                            (byte) 0
                        )
                        .execute();
                }

                for (Town town : side.getSurrenderedTowns()) {
                    context.insertInto(SIDES_TOWNS, SIDES_TOWNS.SIDE, SIDES_TOWNS.TOWN, SIDES_TOWNS.SURRENDERED)
                        .values(
                            convertUUIDToBytes(side.getUUID()),
                            convertUUIDToBytes(town.getUUID()),
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
            transactionWrapper(con, (connection, context) -> {
                context
                    .deleteFrom(SIDES_PLAYERS)
                    .where(SIDES_PLAYERS.SIDE.equal(convertUUIDToBytes(side.getUUID())))
                    .execute();

                for (UUID uuid : side.getPlayersIncludingOffline()) {
                    context.insertInto(SIDES_PLAYERS, SIDES_PLAYERS.SIDE, SIDES_PLAYERS.PLAYER, SIDES_PLAYERS.SURRENDERED)
                        .values(
                            convertUUIDToBytes(side.getUUID()),
                            convertUUIDToBytes(uuid),
                            (byte) 0
                        )
                        .execute();
                }

                for (UUID uuid : side.getSurrenderedPlayersIncludingOffline()) {
                    context.insertInto(SIDES_PLAYERS, SIDES_PLAYERS.SIDE, SIDES_PLAYERS.PLAYER, SIDES_PLAYERS.SURRENDERED)
                        .values(
                            convertUUIDToBytes(side.getUUID()),
                            convertUUIDToBytes(uuid),
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

            context.insertInto(SIEGES, SIEGES.WAR, SIEGES.UUID, SIEGES.TOWN, SIEGES.SIEGELEADER, SIEGES.ENDTIME, SIEGES.LASTTOUCHED, SIEGES.SIEGEPROGRESS)
                .values(
                    convertUUIDToBytes(siege.getWar().getUUID()),
                    convertUUIDToBytes(siege.getUUID()),
                    convertUUIDToBytes(siege.getTown().getUUID()),
                    convertUUIDToBytes(siege.getSiegeLeader().getUniqueId()),
                    LocalDateTime.ofInstant(siege.getEndTime(), ZoneOffset.UTC),
                    LocalDateTime.ofInstant(siege.getLastTouched(), ZoneOffset.UTC),
                    siege.getSiegeProgress()
                )
                .onDuplicateKeyUpdate()
                .set(SIEGES.WAR, convertUUIDToBytes(siege.getWar().getUUID()))
                .set(SIEGES.TOWN, convertUUIDToBytes(siege.getTown().getUUID()))
                .set(SIEGES.SIEGELEADER, convertUUIDToBytes(siege.getSiegeLeader().getUniqueId()))
                .set(SIEGES.ENDTIME, LocalDateTime.ofInstant(siege.getEndTime(), ZoneOffset.UTC))
                .set(SIEGES.LASTTOUCHED, LocalDateTime.ofInstant(siege.getLastTouched(), ZoneOffset.UTC))
                .set(SIEGES.SIEGEPROGRESS, siege.getSiegeProgress())
                .execute();

            saveSiegePlayers(con, siege);
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }

    public static void saveSiegePlayers(Connection con, Siege siege) {
        try {
            transactionWrapper(con, (connection, context) -> {
                context
                    .deleteFrom(SIEGE_PLAYERS)
                    .where(SIEGE_PLAYERS.SIEGE.equal(convertUUIDToBytes(siege.getUUID())))
                    .execute();

                for (UUID uuid : siege.getAttackerPlayersIncludingOffline()) {
                    context.insertInto(SIEGE_PLAYERS, SIEGE_PLAYERS.SIEGE, SIEGE_PLAYERS.PLAYER, SIEGE_PLAYERS.TEAM)
                        .values(
                            convertUUIDToBytes(siege.getUUID()),
                            convertUUIDToBytes(uuid),
                            (byte) 0
                        )
                        .execute();
                }

                for (UUID uuid : siege.getDefenderPlayersIncludingOffline()) {
                    context.insertInto(SIEGE_PLAYERS, SIEGE_PLAYERS.SIEGE, SIEGE_PLAYERS.PLAYER, SIEGE_PLAYERS.TEAM)
                        .values(
                            convertUUIDToBytes(siege.getUUID()),
                            convertUUIDToBytes(uuid),
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
                UUID uuid = convertBytesToUUID(r.get(LIST.UUID));
                String name = r.get(LIST.NAME);
                String label = r.get(LIST.LABEL);
                @Nullable Side side1 = loadSide(con, uuid, convertBytesToUUID(r.get(LIST.SIDE1)));
                @Nullable Side side2 = loadSide(con, uuid, convertBytesToUUID(r.get(LIST.SIDE2)));
                boolean event = r.get(LIST.EVENT, Boolean.class);

                wars.add( // TODO IllegalStateException
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
        }

        return wars;
    }

    @Contract("_, _, _ -> new")
    public static @Nullable Side loadSide(Connection con, UUID warUUID, UUID uuid) {
        try {
            DSLContext context = DB.getContext(con);

            Record r = context.select()
                .from(SIDES)
                .where(SIDES.WAR.equal(convertUUIDToBytes(warUUID)))
                .and(SIDES.UUID.equal(convertUUIDToBytes(uuid)))
                .fetchOne();

            if (r == null) return null;

            // Side data
            BattleSide side = BattleSide.valueOf(r.get(SIDES.SIDE));
            BattleTeam team = BattleTeam.valueOf(r.get(SIDES.TEAM));
            String name = r.get(SIDES.NAME);
            @Nullable Town town = TownyAPI.getInstance().getTown(convertBytesToUUID(r.get(SIDES.TOWN)));
            Instant siegeGrace = r.get(SIDES.SIEGEGRACE).toInstant(ZoneOffset.UTC);
            Instant raidGrace = r.get(SIDES.RAIDGRACE).toInstant(ZoneOffset.UTC);

            // Load players
            Set<UUID> players = new HashSet<>();
            Set<UUID> playersSurrendered = new HashSet<>();
            Result<Record> resultPlayers = context.select()
                .from(SIDES_PLAYERS)
                .where(SIDES_PLAYERS.SIDE.equal(convertUUIDToBytes(uuid)))
                .fetch();
            resultPlayers.forEach(
                record -> {
                    if (record.get(SIDES_PLAYERS.SURRENDERED).equals((byte) 0)) {
                        players.add(convertBytesToUUID(record.get(SIDES_PLAYERS.PLAYER)));
                    } else {
                        playersSurrendered.add(convertBytesToUUID(record.get(SIDES_PLAYERS.PLAYER)));
                    }
                }
            );

            // Load nations
            Set<Nation> nations = new HashSet<>();
            Set<Nation> nationsSurrendered = new HashSet<>();
            Result<Record> resultNations = context.select()
                .from(SIDES_NATIONS)
                .where(SIDES_NATIONS.SIDE.equal(convertUUIDToBytes(uuid)))
                .fetch();
            resultNations.forEach(
                record -> {
                    final UUID identifier = convertBytesToUUID(record.get(SIDES_NATIONS.NATION));
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
                .where(SIDES_TOWNS.SIDE.equal(convertUUIDToBytes(uuid)))
                .fetch();
            resultTowns.forEach(
                record -> {
                    final UUID identifier = convertBytesToUUID(record.get(SIDES_TOWNS.TOWN));
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
                .setPlayersIncludingOffline(players)
                .setSurrenderedTowns(townsSurrendered)
                .setSurrenderedNations(nationsSurrendered)
                .setSurrenderedPlayersIncludingOffline(playersSurrendered)
                .setSiegeGrace(siegeGrace)
                .setRaidGrace(raidGrace)
                .buildOld();
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
                .where(SIEGES.WAR.equal(convertUUIDToBytes(war.getUUID())))
                .fetch();

            for (Record r : result) {
                UUID uuid = convertBytesToUUID(r.get(SIEGES.UUID));
                @Nullable Town town = TownyAPI.getInstance().getTown(convertBytesToUUID(r.get(SIEGES.TOWN)));
                OfflinePlayer siegeLeader = Bukkit.getOfflinePlayer(convertBytesToUUID(r.get(SIEGES.SIEGELEADER)));
                Instant endTime = r.get(SIEGES.ENDTIME).toInstant(ZoneOffset.UTC);
                Instant lastTouched = r.get(SIEGES.LASTTOUCHED).toInstant(ZoneOffset.UTC);
                int siegeProgress = r.get(SIEGES.SIEGEPROGRESS);
                Set<UUID> attackersIncludingOffline = new HashSet<>();
                Set<UUID> defendersIncludingOffline = new HashSet<>();

                Result<Record> resultPlayers = context.select()
                    .from(SIEGE_PLAYERS)
                    .where(SIEGE_PLAYERS.SIEGE.equal(convertUUIDToBytes(uuid)))
                    .fetch();
                resultPlayers.forEach(
                    record -> {
                        if (record.get(SIEGE_PLAYERS.TEAM).equals((byte) 0)) {
                            attackersIncludingOffline.add(convertBytesToUUID(record.get(SIEGE_PLAYERS.PLAYER)));
                        } else {
                            defendersIncludingOffline.add(convertBytesToUUID(record.get(SIEGE_PLAYERS.PLAYER)));
                        }
                    }
                );

                sieges.add(new Siege(
                    war,
                    uuid,
                    town,
                    siegeLeader,
                    endTime,
                    lastTouched,
                    siegeProgress,
                    attackersIncludingOffline,
                    defendersIncludingOffline
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
                .where(LIST.UUID.equal(convertUUIDToBytes(war.getUUID())))
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
                .where(SIEGES.UUID.equal(convertUUIDToBytes(siege.getUUID())))
                .execute();
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }
}
