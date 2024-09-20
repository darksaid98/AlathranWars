CREATE TABLE IF NOT EXISTS "${tablePrefix}list" (
     "uuid" BINARY(16) NOT NULL,
     "name" TINYTEXT NOT NULL,
     "label" TINYTEXT NOT NULL,
     "side1" BINARY(16) NOT NULL,
     "side2" BINARY(16) NOT NULL,
     "event" TINYINT(1) NOT NULL DEFAULT 0,
     PRIMARY KEY ("uuid")
);
CREATE INDEX "${tablePrefix}list_side2" ON "${tablePrefix}list" ("side2");
CREATE INDEX "${tablePrefix}list_side1" ON "${tablePrefix}list" ("side1");

CREATE TABLE IF NOT EXISTS "${tablePrefix}sides" (
      "war" BINARY(16) NOT NULL,
      "uuid" BINARY(16) NOT NULL,
      "side" TINYTEXT NOT NULL,
      "team" TINYTEXT NOT NULL,
      "name" TINYTEXT NOT NULL,
      "town" BINARY(16) NOT NULL,
      "siege_grace" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      "raid_grace" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY ("uuid"),
      FOREIGN KEY ("war") REFERENCES "${tablePrefix}list"("uuid") ON DELETE CASCADE
);
CREATE INDEX "${tablePrefix}sides_town_index" ON "${tablePrefix}sides" ("town");
CREATE INDEX "${tablePrefix}sides_war_index" ON "${tablePrefix}sides" ("war");

CREATE TABLE IF NOT EXISTS "${tablePrefix}sides_nations" (
      "side" BINARY(16) NOT NULL,
      "nation" BINARY(16) NOT NULL,
      "surrendered" TINYINT(1) NOT NULL DEFAULT 0,
      FOREIGN KEY ("side") REFERENCES "${tablePrefix}sides"("uuid") ON DELETE CASCADE
);
CREATE INDEX "${tablePrefix}sides_nations_side_index" ON "${tablePrefix}sides_nations" ("side");

CREATE TABLE IF NOT EXISTS "${tablePrefix}sides_players" (
      "side" BINARY(16) NOT NULL,
      "player" BINARY(16) NOT NULL,
      "surrendered" TINYINT(1) NOT NULL DEFAULT 0,
      FOREIGN KEY ("side") REFERENCES "${tablePrefix}sides"("uuid") ON DELETE CASCADE
);
CREATE INDEX "${tablePrefix}sides_players_side_index" ON "${tablePrefix}sides_players" ("side");

CREATE TABLE IF NOT EXISTS "${tablePrefix}sides_towns" (
        "side" BINARY(16) NOT NULL,
        "town" BINARY(16) NOT NULL,
        "surrendered" TINYINT(1) NOT NULL DEFAULT 0,
        FOREIGN KEY ("side") REFERENCES "${tablePrefix}sides"("uuid") ON DELETE CASCADE
);
CREATE INDEX "${tablePrefix}sides_towns_side_index" ON "${tablePrefix}sides_towns" ("side");

CREATE TABLE IF NOT EXISTS "${tablePrefix}sieges" (
       "war" BINARY(16) NOT NULL,
       "uuid" BINARY(16) NOT NULL,
       "town" BINARY(16) NOT NULL,
       "siege_leader" BINARY(16) NOT NULL,
       "end_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       "last_touched" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       "siege_progress" INT(11) NOT NULL,
       "phase_current" TINYTEXT NOT NULL,
       "phase_progress" INT(11) NOT NULL,
       "phase_start_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       PRIMARY KEY ("uuid"),
       FOREIGN KEY ("war") REFERENCES "${tablePrefix}list"("uuid") ON DELETE CASCADE
);
CREATE INDEX "${tablePrefix}sieges_war_index" ON "${tablePrefix}sieges" ("war");

CREATE TABLE IF NOT EXISTS "${tablePrefix}siege_players" (
     "siege" BINARY(16) NOT NULL,
     "player" BINARY(16) NOT NULL,
     "team" TINYINT(1) NOT NULL,
      FOREIGN KEY ("siege") REFERENCES "${tablePrefix}sieges"("uuid") ON DELETE CASCADE
);
CREATE INDEX "${tablePrefix}siege_players_siege_index" ON "${tablePrefix}siege_players" ("siege");
