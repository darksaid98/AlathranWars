package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class AdminCommands implements CommandExecutor {

    static {

    }

    public AdminCommands(final Main plugin) {
        plugin.getCommand("alathrawaradmin").setExecutor((CommandExecutor)this);
        plugin.getCommand("awa").setExecutor((CommandExecutor)this);
    }

    /**
     * //edit war/event in real time, side can be "both" to effect both
     * -modify raid score [war] [town] [value]
     * -modify raid homeblock [war] [town] (x) (Z)
     * -modify raid gather [war] [town] [town]
     * -modify raid phase [war] [town] [phase] //"next" to move to next phase
     * -modify raid loot [war] [town] (x) (z) [value,looted,ticks,reset] [amt] //no coords just does current chunk, reset deletes it from the list
     * -modify raid time [war] [town] [add/set] [value]
     * -modify raid owner [war] [town] [add/set] [value]
     * -modify raid move [war] [town] [newWar] //low priority, moves raid to other war
     * -modify raid clearActive [war] [town] //low priority
     *
     * -modify siege score [war] [town] [side] [amt]
     * -modify siege homeblock [war] [town] (x) (Z)
     * -modify siege time [war] [town] [add/set/max] [value] //max modified the max length
     * -modify siege owner [war] [town] [add/set] [value]
     * -modify siege move [war] [town] [newWar] //low priority, moves siege to other war
     *
     * -modify war score [war] [side] [amt]
     * -modify war side [war]  [side] [name]
     * -modify war name [war] [name]
     * -modify war add town [war] [town]
     * -modify war add nation [war] [nation]
     * -modify war surrender town [war] [town] //adds town to surrender list
     * -modify war surrender nation [war] [town] //adds all towns to surrender list
     * -modify war raidTime [add,set,reset] [war] [town] [amt] //set when last raid was
     *
     * // low priority idea
     * -info war score [war]
     * -info war surrenderedTowns [war]
     * -info war raids [war]
     * -info war sieges [war]
     * -info war towns [war]
     * -info war lastRaidTime [war]
     *
     * -info raid homeblock [war] [town]
     * -info raid lootedTownBlocks [war] [town]
     * -info raid timeSinceLastRaid [town]
     *
     * -info siege homeblock [war] [town]
     *
     * //force make event or war, if owner isnt defined, idk havent decided
     * -create siege [war] [town] (owner)
     * -create raid [war] [raidTown] (gatherTown) (owner)
     * -create war [name] [side1] [side2]
     *
     * //force end a war/event, can declare winner side, or no winner
     * -force end war [name] (side/victor)
     * -force end siege [war] [town] (side/victor)
     * -force end raid [war] [town] (side/victor)
     *
     * //force player into or out of a war/event
     * -force join war [player] [war] [side]
     * -force join siege [player] [war] [town] (side)
     * -force join raid [player] [war] [town] (side)
     * -force leave war [war] [player] (timeout)
     * -force leave siege [war] [player] (timeout)
     * -force leave raid  [war] [player] (timeout) //kicks from raid party
     *
     *
     * //exclude players from join a raid or war for a time
     * //raid subcommand prevents joining or being raided
     * -exclude player war [war] (side) (timeout)
     * -exclude town war [war] (side) (timeout)
     * -exclude nation war [war] (side) (timeout)
     *
     * -exclude player raid [join/target] [war] (timeout)
     * -exclude town raid [join/target] [war] (timeout)
     * -exclude nation raid [join/target] [war] (timeout)
     *
     * // Ultra low priority idea
     * -rule raidersRespawnAtGatherTown [true/false]
     * -rule siegersRespawnAtTown [true/false]
     * -rule anyoneCanJoinRaid [true/false]
     *
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player p = (Player) sender;
        if (p.hasPermission("!AlathraWar.admin")) {
            fail(p, args, "permissions");
            return false;
        }

        if (args.length == 0) {
            fail(p, args, "syntax");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                help(p, args);
            }
        }
        return false;
    }

    private static void help(Player p, String[] args) {
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin start [war] [town]");
    }

    private static void fail(Player p, String[] args, String type) {
        switch (type) {
            case "permissions": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("&cYou do not have permission to do this"));
                return;
            }
            case "syntax": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
                return;
            }
            default: {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
            }
        }
    }
}
