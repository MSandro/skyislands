/*
 * This file is part of Tatters.
 * Copyright (c) 2021, warjort and others, All rights reserved.
 *
 * Tatters is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tatters is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Tatters.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package tatters.common;

import static net.minecraft.commands.Commands.literal;
import static tatters.TattersMain.log;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import tatters.TattersMain;
import tatters.config.TattersConfig;

public class TattersCommand {

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher, final boolean dedicated) {
        dispatcher.register(literal(TattersMain.MOD_ID).requires((source) -> source.hasPermission(2))
                .then(literal("help").executes(TattersCommand::help))
                .then(literal("lobby").executes(TattersCommand::lobby))
                .then(literal("home").then(playerArgument().executes(TattersCommand::home)))
                .then(literal("regen").then(playerArgument().executes(TattersCommand::regen)))
                .then(literal("visit").then(playerArgument().executes(TattersCommand::visit)))
                .then(literal("team").then(playerArgument().then(teamArgument().executes(TattersCommand::team))))
                .then(literal("list").executes(TattersCommand::list))
                .then(literal("reload").executes(TattersCommand::reload)));
    }

    public static int help(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            for (int i = 0; i < 8; ++i) {
                feedback(context, "tatters.command.help." + i);
            }
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception e) {
            throw handleError(e);
        }
    }

    public static int reload(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            if (TattersConfig.reload(false)) {
                feedback(context, "tatters.command.reloaded");
                return Command.SINGLE_SUCCESS;
            }
            feedback(context, "tatters.command.error");
            return 0;
        } catch (Exception e) {
            throw handleError(e);
        }
    }

    public static int list(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            for (Skyblock skyblock : skyblocks.listSkyblocks()) {
                log.info(skyblock.getUUID() + " " + skyblock.getName() + " " + skyblock.getSpawnPos());
            }
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception e) {
            throw handleError(e);
        }
    }

    public static int lobby(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            final ServerPlayer player = context.getSource().getPlayerOrException();
            final Skyblock lobby = skyblocks.getLobby();
            teleport(lobby, player);
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception e) {
            throw handleError(e);
        }
    }

    public static int home(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            final ServerPlayer player = playerParameter(context);
            Skyblock skyblock = skyblocks.getSkyblock(player);
            if (skyblock == null) {
                skyblock = skyblocks.createSkyblock(player);
                skyblock.setPlayerSpawn(player);
            }
            teleport(skyblock, player);
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception e) {
            throw handleError(e);
        }
    }

    public static int regen(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            final ServerPlayer player = playerParameter(context);
            final Skyblock skyblock = skyblocks.createSkyblock(player);
            skyblock.setPlayerSpawn(player);
            teleport(skyblock, player);
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception e) {
            throw handleError(e);
        }
    }

    public static int visit(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            final ServerPlayer player = context.getSource().getPlayerOrException();
            final ServerPlayer toVisit = playerParameter(context);
            final Skyblock skyblock = skyblocks.getSkyblock(toVisit);
            if (skyblock == null) {
                throw error("tatters.command.noskyblock");
            }
            teleport(skyblock, player);
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception e) {
            throw handleError(e);
        }
    }

    @SuppressWarnings("resource")
    public static int team(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            final ServerPlayer player = playerParameter(context);
            final PlayerTeam team = teamParameter(context);
            Skyblock skyblock = skyblocks.getSkyblock(team);
            if (skyblock == null) {
                skyblock = skyblocks.createSkyblock(team);
            }
            skyblocks.getWorld().getScoreboard().addPlayerToTeam(player.getScoreboardName(), team);
            skyblock.setPlayerSpawn(player);
            teleport(skyblock, player);
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception e) {
            throw handleError(e);
        }
    }

    @SuppressWarnings("resource")
    public static Skyblocks getSkyblocks(final CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        final Skyblocks skyblocks = Skyblocks.getSkyblocks(context.getSource().getLevel());
        if (skyblocks == null) {
            throw error("tatters.command.wrongworld");
        }
        return skyblocks;
    }

    public static void teleport(final Skyblock skyblock, final ServerPlayer player)
            throws CommandSyntaxException {
        if (!skyblock.teleport(player)) {
            throw error("tatters.command.noteleport");
        }
    }

    public static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> playerArgument() {
        return Commands.argument("player", EntityArgument.player());
    }

    public static ServerPlayer playerParameter(final CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return EntityArgument.getPlayer(context, "player");
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> teamArgument() {
        return Commands.argument("team", TeamArgument.team());
    }

    public static PlayerTeam teamParameter(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return TeamArgument.getTeam(context, "team");
    }

    public static void feedback(final CommandContext<CommandSourceStack> context, final String feedback) {
        feedback(context, feedback, false);
    }

    public static void feedbackOps(final CommandContext<CommandSourceStack> context, final String feedback) {
        feedback(context, feedback, true);
    }

    public static void feedback(final CommandContext<CommandSourceStack> context, final String feedback,
            final boolean ops) {
        context.getSource().sendSuccess(new TranslatableComponent(feedback), ops);
    }

    public static CommandSyntaxException error(final String error) {
        return new SimpleCommandExceptionType(new TranslatableComponent(error)).create();
    }

    public static CommandSyntaxException handleError(final Exception e) {
        if (e instanceof CommandSyntaxException)
            return (CommandSyntaxException) e;
        log.error("Unexpected error in command", e);
        return new SimpleCommandExceptionType(new TranslatableComponent("tatters.command.error")).create();
    }
}
