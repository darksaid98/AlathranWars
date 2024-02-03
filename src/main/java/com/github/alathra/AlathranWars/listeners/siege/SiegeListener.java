package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.conflict.war.War;
import com.github.alathra.AlathranWars.conflict.war.side.Side;
import com.github.alathra.AlathranWars.enums.battle.BattleType;
import com.github.alathra.AlathranWars.enums.battle.BattleVictoryReason;
import com.github.alathra.AlathranWars.events.battle.BattleResultEvent;
import com.github.alathra.AlathranWars.events.battle.BattleStartEvent;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class SiegeListener implements Listener {
    private final static Title.Times TITLE_TIMES = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500));
    private final static Sound SOUND_VICTORY = Sound.sound(Key.key("item.goat_horn.sound.0"), Sound.Source.VOICE, 0.5f, 1.0F);
    private final static Sound SOUND_DEFEAT = Sound.sound(Key.key("entity.wither.death"), Sound.Source.VOICE, 0.5f, 1.0F);
    private final static List<Sound> SOUND_GOATHORNS = List.of(
        Sound.sound(Key.key("item.goat_horn.sound.0"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
        Sound.sound(Key.key("item.goat_horn.sound.2"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
        Sound.sound(Key.key("item.goat_horn.sound.3"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
        Sound.sound(Key.key("item.goat_horn.sound.7"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F))
    );

    /**
     * On battle start UI handling.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBattleStart(BattleStartEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        final Title defTitle = Title.title(
            ColorParser.of("<red><u><b><town>")
                .parseMinimessagePlaceholder("town", siege.getTown().getName())
                .build(),
            ColorParser.of("<gray><i>Is under siege, defend!")
                .build(),
            TITLE_TIMES
        );
        final Title attTitle = Title.title(
            ColorParser.of("<red><u><b><town>")
                .parseMinimessagePlaceholder("town", siege.getTown().getName())
                .build(),
            ColorParser.of("<gray><i>Has been put to siege, attack!")
                .build(),
            TITLE_TIMES
        );

        siege.getDefenderPlayers().forEach(player -> {
            player.showTitle(defTitle);
            player.playSound(SOUND_GOATHORNS.get(new Random().nextInt(1, SOUND_GOATHORNS.size())));
        });

        siege.getAttackerPlayers().forEach(player -> {
            player.showTitle(attTitle);
            player.playSound(SOUND_GOATHORNS.get(new Random().nextInt(1, SOUND_GOATHORNS.size())));
        });
    }

    /**
     * On battle end UI handling.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBattleResult(BattleResultEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        switch (e.getBattleVictor()) {
            case ATTACKER -> {
                if (e.getBattleVictoryReason().equals(BattleVictoryReason.OPPONENT_RETREAT)) {
                    Bukkit.broadcast(
                        ColorParser.of("<prefix>The town of <town> has surrendered.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .parseMinimessagePlaceholder("town", siege.getTown().getName())
                            .build()
                    );
                } else {
                    Bukkit.broadcast(ColorParser.of("<prefix>The town of <town> has been sacked and placed under occupation by the armies of <attacker>!")
                        .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                        .parseMinimessagePlaceholder("town", siege.getTown().getName())
                        .parseMinimessagePlaceholder("attacker", siege.getAttackerSide().getName())
                        .build());
                }

                final Title vicAttackTitle = Title.title(
                    ColorParser.of("<green><u><b>Victory")
                        .build(),
                    ColorParser.of("<gray><i><town> has been captured!")
                        .parseMinimessagePlaceholder("town", siege.getTown().getName())
                        .build(),
                    TITLE_TIMES
                );
                final Title losAttackTitle = Title.title(
                    ColorParser.of("<red><u><b>Defeat")
                        .build(),
                    ColorParser.of("<gray><i><town> has been lost!")
                        .parseMinimessagePlaceholder("town", siege.getTown().getName())
                        .build(),
                    TITLE_TIMES
                );
                siege.getAttackers().forEach(player -> {
                    player.showTitle(vicAttackTitle);
                    player.playSound(SOUND_VICTORY);
                });
                siege.getDefenders().forEach(player -> {
                    player.showTitle(losAttackTitle);
                    player.playSound(SOUND_DEFEAT);
                });
            }
            case DEFENDER -> {
                if (e.getBattleVictoryReason().equals(BattleVictoryReason.OPPONENT_RETREAT)) {
                    Bukkit.broadcast(
                        ColorParser.of("<prefix>The siege at <town> has been abandoned by the attackers.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .parseMinimessagePlaceholder("town", siege.getTown().getName())
                            .build()
                    );
                } else {
                    Bukkit.broadcast(
                        ColorParser.of("<prefix>The siege of <town> has been lifted by the defenders!")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .parseMinimessagePlaceholder("town", siege.getTown().getName())
                            .build()
                    );
                }

                final Title vicDefendTitle = Title.title(
                    ColorParser.of("<red><u><b>Defeat")
                        .build(),
                    ColorParser.of("<gray><i>We failed to capture <town>!")
                        .parseMinimessagePlaceholder("town", siege.getTown().getName())
                        .build(),
                    TITLE_TIMES
                );
                final Title losDefendTitle = Title.title(
                    ColorParser.of("<green><u><b>Victory")
                        .build(),
                    ColorParser.of("<gray><i><town> has been made safe!")
                        .parseMinimessagePlaceholder("town", siege.getTown().getName())
                        .build(),
                    TITLE_TIMES
                );
                siege.getAttackers().forEach(player -> {
                    player.showTitle(vicDefendTitle);
                    player.playSound(SOUND_DEFEAT);
                });
                siege.getDefenders().forEach(player -> {
                    player.showTitle(losDefendTitle);
                    player.playSound(SOUND_VICTORY);
                });
            }
            case DRAW -> {
                Bukkit.broadcast(
                    ColorParser.of("<prefix>The siege of <town> has ended in a draw!")
                        .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                        .parseMinimessagePlaceholder("town", siege.getTown().getName())
                        .build()
                );

                final Title drawTitle = Title.title(
                    ColorParser.of("<yellow><u><b>Draw")
                        .build(),
                    ColorParser.of("<gray><i>The siege at <town> has ended!")
                        .parseMinimessagePlaceholder("town", siege.getTown().getName())
                        .build(),
                    TITLE_TIMES
                );
                siege.getAttackers().forEach(player -> {
                    player.showTitle(drawTitle);
                    player.playSound(SOUND_DEFEAT);
                });
                siege.getDefenders().forEach(player -> {
                    player.showTitle(drawTitle);
                    player.playSound(SOUND_DEFEAT);
                });
            }
        }
    }

    /**
     * On battle end handle occupation results.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBattleResultOccupy(BattleResultEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        switch (e.getBattleVictor()) {
            case ATTACKER -> {
                War war = e.getWar();

                if (war.isEventWar()) return;

                Town town = siege.getTown();
                Side townSide = war.getTownSide(town);

                if (townSide == null) return;

                final boolean isLiberation = siege.getAttackerSide().equals(townSide); // Is the town being liberated or occupied

                if (isLiberation) { // Liberation siege
                    if (townSide.isTownSurrendered(town)) {
                        war.unsurrenderTown(town);
                    }
                } else { // Occupation siege
                    if (!townSide.isTownSurrendered(town)) {
                        war.surrenderTown(town);
                    }
                }
            }
            case DEFENDER, DRAW -> {
            }
        }
    }

    /**
     * On battle end handle war score distribution.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBattleResultWarScore(BattleResultEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        switch (e.getBattleVictor()) {
            case ATTACKER -> {
                siege.getAttackerSide().addScore(50);
                siege.getDefenderSide().addScore(5);
            }
            case DEFENDER -> {
                siege.getDefenderSide().addScore(10);
            }
            case DRAW -> {
            }
        }
    }
}
