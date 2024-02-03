package com.github.alathra.AlathranWars.commands;

import com.github.alathra.AlathranWars.AlathranWars;
import com.github.alathra.AlathranWars.hooks.NameColorHandler;
import com.github.alathra.AlathranWars.items.WarItemRegistry;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AdminCommands {
    public AdminCommands() {
        new CommandAPICommand("alathranwarsadmin")
            .withAliases("awa", "wa", "waradmin")
            .withPermission("AlathranWars.admin")
            .withSubcommands(
                commandItem(),
                commandWar(),
                commandSiege(),
                commandNames()
            )
            .executes((sender, args) -> {
                if (args.count() == 0)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "Invalid Arguments.").build());
            })
            .register();
    }

    private CommandAPICommand commandItem() {
        return new CommandAPICommand("item")
            .withArguments(
                new StringArgument("item")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(
                            CommandUtil.getWarItems()
                        )
                    ),
                new IntegerArgument("amount").setOptional(true),
                new PlayerArgument("player").setOptional(true)
            )
            .executesPlayer((Player sender, CommandArguments args) -> {
                // Parse item
                if (!(args.get("item") instanceof String argItem))
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Invalid item argument.").build());

                @Nullable ItemStack stack;
                if (argItem.contains(AlathranWars.getInstance().getName().toLowerCase())) {
                    stack = WarItemRegistry.getInstance().getOrNullNamespace(argItem);
                } else {
                    stack = WarItemRegistry.getInstance().getOrNull(argItem);
                }

                if (stack == null)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The item is not an AlathranWars item.").build());

                // Parse amount
                int argAmount = (int) args.getOptional("amount").orElse(1);

                if (argAmount > 64)
                    argAmount = 64;
                else if (argAmount < 1) {
                    argAmount = 1;
                }
                stack.setAmount(argAmount);

                // Determine who gets the item
                Player argPlayer = (Player) args.getOptional("player").orElse(sender);

                // Give
                argPlayer.getInventory().addItem(stack);

                final @Nullable Component itemName = stack.getItemMeta() == null ? ColorParser.of(stack.toString()).build() : stack.getItemMeta().displayName();
                sender.sendMessage(
                    ColorParser.of(
                            "<prefix>Gave <amount>x <item> to <player>."
                        )
                        .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                        .parseMinimessagePlaceholder("amount", String.valueOf(stack.getAmount()))
                        .parseMinimessagePlaceholder("item", itemName)
                        .parseMinimessagePlaceholder("player", argPlayer.getName())
                        .build()
                );
            });
    }

    private CommandAPICommand commandWar() {
        return new CommandAPICommand("war")
            .withSubcommands(
                WarCommands.commandCreate(true),
                WarCommands.commandDelete(true),
                WarCommands.commandJoin(true),
                WarCommands.commandJoinNear(),
                WarCommands.commandSurrender(true),
                WarCommands.commandList(),
                WarCommands.commandInfo(),
                WarCommands.commandKick()
            );
    }

    private CommandAPICommand commandSiege() {
        return new CommandAPICommand("siege")
            .withSubcommands(
                SiegeCommands.commandStart(true),
                SiegeCommands.commandStop(true),
                SiegeCommands.commandAbandon(true),
                SiegeCommands.commandSurrender(true),
                SiegeCommands.commandList()
            );
    }

    private CommandAPICommand commandNames() {
        return new CommandAPICommand("updatenames")
            .executesPlayer((player, commandArguments) -> {
                Bukkit.getOnlinePlayers().forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));
            });
    }
}
