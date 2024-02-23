CREATE TABLE IF NOT EXISTS ${tablePrefix}list (
     uuid ${uuidType} NOT NULL DEFAULT uuid(),
     "name" TINYTEXT NOT NULL,
     label TINYTEXT NOT NULL,
     side1 ${uuidType} NOT NULL DEFAULT uuid(),
     side2 ${uuidType} NOT NULL DEFAULT uuid(),
     event TINYINT(1) NOT NULL DEFAULT 0,
     PRIMARY KEY (uuid),
     INDEX side2 (side2),
     INDEX side1 (side1)
)${tableDefaults};

CREATE TABLE IF NOT EXISTS ${tablePrefix}sides (
      war ${uuidType} NOT NULL,
      uuid ${uuidType} NOT NULL,
      side TINYTEXT NOT NULL,
      team TINYTEXT NOT NULL,
      "name" TINYTEXT NOT NULL,
      town ${uuidType} NOT NULL,
      siegeGrace TIMESTAMP NOT NULL,
      raidGrace TIMESTAMP NOT NULL,
      PRIMARY KEY (uuid),
      INDEX town (town),
      INDEX war (war),
      CONSTRAINT war FOREIGN KEY (war) REFERENCES ${tablePrefix}list (uuid) ON UPDATE CASCADE ON DELETE CASCADE
)${tableDefaults};

CREATE TABLE IF NOT EXISTS ${tablePrefix}sides_nations (
      side ${uuidType} NOT NULL,
      nation ${uuidType} NOT NULL,
      surrendered TINYINT(1) NOT NULL DEFAULT 0,
      INDEX side_nations (side),
      CONSTRAINT side_nations FOREIGN KEY (side) REFERENCES ${tablePrefix}sides (uuid) ON UPDATE CASCADE ON DELETE CASCADE
)${tableDefaults};

CREATE TABLE IF NOT EXISTS ${tablePrefix}sides_players (
      side ${uuidType} NOT NULL,
      player ${uuidType} NOT NULL,
      surrendered TINYINT(1) NOT NULL DEFAULT 0,
      INDEX side_players (side),
      CONSTRAINT side_players FOREIGN KEY (side) REFERENCES ${tablePrefix}sides (uuid) ON UPDATE CASCADE ON DELETE CASCADE
)${tableDefaults};

CREATE TABLE IF NOT EXISTS ${tablePrefix}sides_towns (
        side ${uuidType} NOT NULL,
        town ${uuidType} NOT NULL,
        surrendered TINYINT(1) NOT NULL DEFAULT 0,
        INDEX side_towns (side),
        CONSTRAINT side_towns FOREIGN KEY (side) REFERENCES ${tablePrefix}sides (uuid) ON UPDATE CASCADE ON DELETE CASCADE
)${tableDefaults};

CREATE TABLE IF NOT EXISTS ${tablePrefix}sieges (
       war ${uuidType} NOT NULL,
       uuid ${uuidType} NOT NULL,
       town ${uuidType} NOT NULL,
       siegeLeader ${uuidType} NOT NULL,
       endTime TIMESTAMP NOT NULL,
       lastTouched TIMESTAMP NOT NULL,
       siegeProgress INT(11) NOT NULL,
       PRIMARY KEY (uuid),
       INDEX siege_war (war),
       CONSTRAINT siege_war FOREIGN KEY (war) REFERENCES ${tablePrefix}list (uuid) ON UPDATE CASCADE ON DELETE CASCADE
)${tableDefaults};

CREATE TABLE IF NOT EXISTS ${tablePrefix}siege_players (
     siege ${uuidType} NOT NULL,
     player ${uuidType} NOT NULL,
     team TINYINT(1) NOT NULL,
     INDEX siege_players (siege),
     CONSTRAINT siege_players FOREIGN KEY (siege) REFERENCES ${tablePrefix}sieges (uuid) ON UPDATE CASCADE ON DELETE CASCADE
)${tableDefaults};
