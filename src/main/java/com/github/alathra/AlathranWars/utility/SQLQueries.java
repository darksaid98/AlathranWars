package com.github.alathra.AlathranWars.utility;

import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.BattleSide;
import com.github.alathra.AlathranWars.enums.BattleTeam;
import com.github.alathra.AlathranWars.holder.WarManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SQLQueries {
    public static void initDB() {
        try (final Statement statement = DB.get().createStatement()) {
            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_list` (
                      `uuid` uuid NOT NULL DEFAULT uuid(),
                      `name` tinytext NOT NULL,
                      `label` tinytext NOT NULL,
                      `side1` uuid NOT NULL DEFAULT uuid(),
                      `side2` uuid NOT NULL DEFAULT uuid(),
                      PRIMARY KEY (`uuid`),
                      KEY `side2` (`side2`),
                      KEY `side1` (`side1`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_sides` (
                      `war` uuid NOT NULL,
                      `uuid` uuid NOT NULL,
                      `side` tinytext NOT NULL,
                      `team` tinytext NOT NULL,
                      `name` tinytext NOT NULL,
                      `town` uuid NOT NULL,
                      `siegeGrace` timestamp NOT NULL,
                      `raidGrace` timestamp NOT NULL,
                      PRIMARY KEY (`uuid`),
                      KEY `town` (`town`),
                      KEY `war` (`war`),
                      CONSTRAINT `war` FOREIGN KEY (`war`) REFERENCES `wars_list` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_sides_nations` (
                      `side` uuid NOT NULL,
                      `nation` uuid NOT NULL,
                      KEY `side_nations` (`side`),
                      CONSTRAINT `side_nations` FOREIGN KEY (`side`) REFERENCES `wars_sides` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_sides_nations_surrendered` (
                      `side` uuid NOT NULL,
                      `nation` uuid NOT NULL,
                      KEY `side_nations_surrendered` (`side`),
                      CONSTRAINT `side_nations_surrendered` FOREIGN KEY (`side`) REFERENCES `wars_sides` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_sides_players` (
                      `side` uuid NOT NULL,
                      `player` uuid NOT NULL,
                      KEY `side_players` (`side`),
                      CONSTRAINT `side_players` FOREIGN KEY (`side`) REFERENCES `wars_sides` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_sides_players_surrendered` (
                      `side` uuid NOT NULL,
                      `player` uuid NOT NULL,
                      KEY `side_players_surrendered` (`side`),
                      CONSTRAINT `side_players_surrendered` FOREIGN KEY (`side`) REFERENCES `wars_sides` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_sides_towns` (
                      `side` uuid NOT NULL,
                      `town` uuid NOT NULL,
                      KEY `side_towns` (`side`),
                      CONSTRAINT `side_towns` FOREIGN KEY (`side`) REFERENCES `wars_sides` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_sides_towns_surrendered` (
                      `side` uuid NOT NULL,
                      `town` uuid NOT NULL,
                      KEY `side_towns_surrendered` (`side`),
                      CONSTRAINT `side_towns_surrendered` FOREIGN KEY (`side`) REFERENCES `wars_sides` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_sieges` (
                      `war` uuid NOT NULL,
                      `uuid` uuid NOT NULL,
                      `town` uuid NOT NULL,
                      `siegeLeader` uuid NOT NULL,
                      `endTime` timestamp NOT NULL,
                      `siegeProgress` int(11) NOT NULL,
                      PRIMARY KEY (`uuid`),
                      KEY `siege_war` (`war`),
                      CONSTRAINT `siege_war` FOREIGN KEY (`war`) REFERENCES `wars_list` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_sieges_players_attackers` (
                      `siege` uuid NOT NULL,
                      `player` uuid NOT NULL,
                      KEY `siege_players_attackers` (`siege`),
                      CONSTRAINT `siege_players_attackers` FOREIGN KEY (`siege`) REFERENCES `wars_sieges` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.addBatch("""
                    CREATE TABLE IF NOT EXISTS `wars_sieges_players_defenders` (
                      `siege` uuid NOT NULL,
                      `player` uuid NOT NULL,
                      KEY `siege_players_defenders` (`siege`),
                      CONSTRAINT `siege_players_defenders` FOREIGN KEY (`siege`) REFERENCES `wars_sieges` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
                """);

            statement.executeLargeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveAll() {
        try (
            Connection con = DB.get();
            PreparedStatement warStatement = con.prepareStatement("INSERT INTO `wars_list` (`uuid`, `name`, `label`, `side1`, `side2`) VALUES (?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE `name` = ?, `label` = ?, `side1` = ?, `side2` = ?")
        ) {
            // Save wars
            for (War war : WarManager.getInstance().getWars()) {
                // Insert
                warStatement.setString(1, war.getUUID().toString());
                warStatement.setString(2, war.getName());
                warStatement.setString(3, war.getLabel());
                warStatement.setString(4, war.getSide1().getUUID().toString());
                warStatement.setString(5, war.getSide2().getUUID().toString());

                // Update
                warStatement.setString(6, war.getName());
                warStatement.setString(7, war.getLabel());
                warStatement.setString(8, war.getSide1().getUUID().toString());
                warStatement.setString(9, war.getSide2().getUUID().toString());

                warStatement.executeUpdate();

                // Save sides
                for (Side side : war.getSides()) {
                    saveSide(con, side);
                }

                // TODO Save sieges
//                for (Siege siege : war.getSieges()) {
//
//                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveSide(Connection con, Side side) throws SQLException {
        try (
            PreparedStatement sideStatement = con.prepareStatement("INSERT INTO `wars_sides` (`war`, `uuid`, `side`, `team`, `name`, `town`, `siegeGrace`, `raidGrace`) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE `side` = ?, `team` = ?, `name` = ?, `town` = ?, `siegeGrace` = ?, `raidGrace` = ?");
        )
        {
            // Insert
            sideStatement.setString(1, side.getWar().getUUID().toString());
            sideStatement.setString(2, side.getUUID().toString());
            sideStatement.setString(3, side.getSide().toString());
            sideStatement.setString(4, side.getTeam().toString());
            sideStatement.setString(5, side.getName());
            sideStatement.setString(6, side.getTown().getUUID().toString());
            sideStatement.setTimestamp(7, Timestamp.from(side.getSiegeGrace()));
            sideStatement.setTimestamp(8, Timestamp.from(side.getRaidGrace()));

            // Update
            sideStatement.setString(9, side.getSide().toString());
            sideStatement.setString(10, side.getTeam().toString());
            sideStatement.setString(11, side.getName());
            sideStatement.setString(12, side.getTown().getUUID().toString());
            sideStatement.setTimestamp(13, Timestamp.from(side.getSiegeGrace()));
            sideStatement.setTimestamp(14, Timestamp.from(side.getRaidGrace()));

            sideStatement.executeUpdate();

            saveSideNations(con, side, false);
            saveSideNations(con, side, true);
            saveSideTowns(con, side, false);
            saveSideTowns(con, side, true);
            saveSidePlayers(con, side, false);
            saveSidePlayers(con, side, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveSideNations(Connection con, Side side, boolean surrendered) throws SQLException {
        try (
            PreparedStatement deleteStatement = con.prepareStatement("DELETE FROM `%s` WHERE `side` = ?".formatted(surrendered ? "wars_sides_nations_surrendered" : "wars_sides_nations"));
            PreparedStatement insertStatement = con.prepareStatement("INSERT INTO `%s` (`side`, `nation`) VALUES (?, ?)".formatted(surrendered ? "wars_sides_nations_surrendered" : "wars_sides_nations"));
        ) {
            try {
                // Transaction start
                con.setAutoCommit(false);

                // Delete old
                deleteStatement.setString(1, side.getUUID().toString());
                deleteStatement.executeUpdate();

                // Insert new
                for (Nation nation : (surrendered ? side.getSurrenderedNations() : side.getNations())) {
                    insertStatement.setString(1, side.getUUID().toString());
                    insertStatement.setString(2, nation.getUUID().toString());
                    insertStatement.executeUpdate();
                }

                // Transaction end
                con.commit();
            } catch(Exception e) {
                con.rollback();
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private static void saveSideTowns(Connection con, Side side, boolean surrendered) throws SQLException {
        try (
            PreparedStatement deleteStatement = con.prepareStatement("DELETE FROM `%s` WHERE `side` = ?".formatted(surrendered ? "wars_sides_towns_surrendered" : "wars_sides_towns"));
            PreparedStatement insertStatement = con.prepareStatement("INSERT INTO `%s` (`side`, `town`) VALUES (?, ?)".formatted(surrendered ? "wars_sides_towns_surrendered" : "wars_sides_towns"));
        ) {
            try {
                // Transaction start
                con.setAutoCommit(false);

                // Delete old
                deleteStatement.setString(1, side.getUUID().toString());
                deleteStatement.executeUpdate();

                // Insert new
                for (Town town : (surrendered ? side.getSurrenderedTowns() : side.getTowns())) {
                    insertStatement.setString(1, side.getUUID().toString());
                    insertStatement.setString(2, town.getUUID().toString());
                    insertStatement.executeUpdate();
                }

                // Transaction end
                con.commit();
            } catch(Exception e) {
                con.rollback();
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private static void saveSidePlayers(Connection con, Side side, boolean surrendered) throws SQLException {
        try (
            PreparedStatement deleteStatement = con.prepareStatement("DELETE FROM `%s` WHERE `side` = ?".formatted(surrendered ? "wars_sides_players_surrendered" : "wars_sides_players"));
            PreparedStatement insertStatement = con.prepareStatement("INSERT INTO `%s` (`side`, `player`) VALUES (?, ?)".formatted(surrendered ? "wars_sides_players_surrendered" : "wars_sides_players"));
        ) {
            try {
                // Transaction start
                con.setAutoCommit(false);

                // Delete old
                deleteStatement.setString(1, side.getUUID().toString());
                deleteStatement.executeUpdate();

                // Insert new
                for (UUID uuid : (surrendered ? side.getSurrenderedPlayersIncludingOffline() : side.getPlayersIncludingOffline())) {
                    insertStatement.setString(1, side.getUUID().toString());
                    insertStatement.setString(2, uuid.toString());
                    insertStatement.executeUpdate();
                }

                // Transaction end
                con.commit();
            } catch(Exception e) {
                con.rollback();
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public static Set<War> loadAll() {
        Set<War> wars = new HashSet<>();

        try (
            Connection con = DB.get();
            PreparedStatement warStatement = con.prepareStatement("SELECT * FROM `wars_list`")
        ) {
            // Load all wars
            ResultSet warsResult = warStatement.executeQuery();

            if (warsResult == null)
                throw new SQLException("Failed to get wars from DB");

            while (warsResult.next()) {
                // War data
                UUID uuid = UUID.fromString(warsResult.getString("uuid"));
                String name = warsResult.getString("name");
                String label = warsResult.getString("label");
                UUID side1UUID = UUID.fromString(warsResult.getString("side1"));
                UUID side2UUID = UUID.fromString(warsResult.getString("side2"));
                Side side1 = loadSide(con, uuid, side1UUID);
                Side side2 = loadSide(con, uuid, side2UUID);

                // TODO Siege data

                wars.add(new War(
                    uuid,
                    name,
                    label,
                    side1,
                    side2,
                    new HashSet<>(),
                    new HashSet<>()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return wars;
    }

    public static Side loadSide(Connection con, UUID warUUID, UUID uuid) {
        try (PreparedStatement warStatement = con.prepareStatement("SELECT * FROM `wars_sides` WHERE `war` = ? AND `uuid` = ?")) {
            // Load side
            warStatement.setString(1, warUUID.toString());
            warStatement.setString(2, uuid.toString());
            ResultSet sideResult = warStatement.executeQuery();

            if (sideResult == null)
                throw new SQLException("Failed to get side from DB");

            sideResult.next();

            // Side data
            BattleSide side = BattleSide.valueOf(sideResult.getString("side"));
            BattleTeam team = BattleTeam.valueOf(sideResult.getString("team"));
            String name = sideResult.getString("name");
            Town town = TownyAPI.getInstance().getTown(UUID.fromString(sideResult.getString("town")));
            Instant siegeGrace = sideResult.getTimestamp("siegeGrace").toInstant();
            Instant raidGrace = sideResult.getTimestamp("raidGrace").toInstant();

            Set<UUID> players = loadSidePlayers(con, uuid, false);
            Set<UUID> playersSurrendered = loadSidePlayers(con, uuid, true);
            Set<Nation> nations = loadSideNations(con, uuid, false);
            Set<Nation> nationsSurrendered = loadSideNations(con, uuid, true);
            Set<Town> towns = loadSideTowns(con, uuid, false);
            Set<Town> townsSurrendered = loadSideTowns(con, uuid, true);

            return new Side(
                warUUID,
                uuid,
                town,
                side,
                team,
                name,
                towns,
                nations,
                players,
                townsSurrendered,
                nationsSurrendered,
                playersSurrendered,
                siegeGrace,
                raidGrace
            );
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Set<UUID> loadSidePlayers(Connection con, UUID sideUUID, boolean surrendered) {
        Set<UUID> players = new HashSet<>();

        try (PreparedStatement warStatement = con.prepareStatement("SELECT * FROM `%s` WHERE `side` = ?".formatted(surrendered ? "wars_sides_players_surrendered" : "wars_sides_players"))) {
            warStatement.setString(1, sideUUID.toString());
            ResultSet warsResult = warStatement.executeQuery();

            if (warsResult == null)
                throw new SQLException("Failed to get players from DB");

            while (warsResult.next()) {
                players.add(UUID.fromString(warsResult.getString("player")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return players;
    }

    public static Set<Nation> loadSideNations(Connection con, UUID sideUUID, boolean surrendered) {
        Set<Nation> nations = new HashSet<>();

        try (PreparedStatement warStatement = con.prepareStatement("SELECT * FROM `%s` WHERE `side` = ?".formatted(surrendered ? "wars_sides_nations_surrendered" : "wars_sides_nations"))) {
            warStatement.setString(1, sideUUID.toString());
            ResultSet warsResult = warStatement.executeQuery();

            if (warsResult == null)
                throw new SQLException("Failed to get nations from DB");

            while (warsResult.next()) {
                final UUID uuid = UUID.fromString(warsResult.getString("nation"));

                @Nullable Nation nation = TownyAPI.getInstance().getNation(uuid);
                if (nation == null) continue;

                nations.add(nation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nations;
    }

    public static Set<Town> loadSideTowns(Connection con, UUID sideUUID, boolean surrendered) {
        Set<Town> towns = new HashSet<>();

        try (PreparedStatement warStatement = con.prepareStatement("SELECT * FROM `%s` WHERE `side` = ?".formatted(surrendered ? "wars_sides_towns_surrendered" : "wars_sides_towns"))) {
            warStatement.setString(1, sideUUID.toString());
            ResultSet warsResult = warStatement.executeQuery();

            if (warsResult == null)
                throw new SQLException("Failed to get towns from DB");

            while (warsResult.next()) {
                final UUID uuid = UUID.fromString(warsResult.getString("town"));

                @Nullable Town town = TownyAPI.getInstance().getTown(uuid);
                if (town == null) continue;

                towns.add(town);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return towns;
    }

    public static void deleteWar(War war) {
        try (
            Connection con = DB.get();
            PreparedStatement warStatement = con.prepareStatement("DELETE FROM `wars_list` WHERE `uuid` = ?");
        ) {
            // Delete war data
            warStatement.setString(1, war.getUUID().toString());
            warStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSiege(Siege siege) {
        try (
            Connection con = DB.get();
            PreparedStatement siegesStatement = con.prepareStatement("DELETE FROM `wars_sieges` WHERE `uuid` = ?");
        ) {
            // Delete siege data
            siegesStatement.setString(1, siege.getUUID().toString());
            siegesStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
