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

import static net.minecraft.server.command.CommandManager.literal;
import static tatters.TattersMain.log;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import tatters.TattersMain;
import tatters.config.TattersConfig;

public class TattersCommand {

    public static void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(TattersMain.MOD_ID).requires((source) -> source.hasPermissionLevel(2))
                .then(literal("help").executes(TattersCommand::help))
                .then(literal("lobby").executes(TattersCommand::lobby))
                .then(literal("home").then(playerArgument().executes(TattersCommand::home)))
                .then(literal("regen").then(playerArgument().executes(TattersCommand::regen)))
                .then(literal("visit").then(playerArgument().executes(TattersCommand::visit)))
                .then(literal("team").then(playerArgument().then(teamArgument().executes(TattersCommand::team))))
                .then(literal("list").executes(TattersCommand::list))
                .then(literal("reload").executes(TattersCommand::reload)));
    }

    public static int help(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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

    public static int reload(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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

    public static int list(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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

    public static int lobby(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Skyblock lobby = skyblocks.getLobby();
            teleport(lobby, player);
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception e) {
            throw handleError(e);
        }
    }

    public static int home(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            final ServerPlayerEntity player = playerParameter(context);
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

    public static int regen(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            final ServerPlayerEntity player = playerParameter(context);
            final Skyblock skyblock = skyblocks.createSkyblock(player);
            skyblock.setPlayerSpawn(player);
            teleport(skyblock, player);
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception e) {
            throw handleError(e);
        }
    }

    public static int visit(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final ServerPlayerEntity toVisit = playerParameter(context);
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

    public static int team(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            final Skyblocks skyblocks = getSkyblocks(context);
            final ServerPlayerEntity player = playerParameter(context);
            final Team team = teamParameter(context);
            Skyblock skyblock = skyblocks.getSkyblock(team);
            if (skyblock == null) {
                skyblock = skyblocks.createSkyblock(team);
            }
            skyblocks.getWorld().getScoreboard().addPlayerToTeam(player.getEntityName(), team);
            skyblock.setPlayerSpawn(player);
            teleport(skyblock, player);
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception e) {
            throw handleError(e);
        }
    }

    public static Skyblocks getSkyblocks(final CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        final Skyblocks skyblocks = Skyblocks.getSkyblocks(context.getSource().getWorld());
        if (skyblocks == null) {
            throw error("tatters.command.wrongworld");
        }
        return skyblocks;
    }

    public static void teleport(final Skyblock skyblock, final ServerPlayerEntity player)
            throws CommandSyntaxException {
        if (!skyblock.teleport(player)) {
            throw error("tatters.command.noteleport");
        }
    }

    public static RequiredArgumentBuilder<ServerCommandSource, EntitySelector> playerArgument() {
        return CommandManager.argument("player", EntityArgumentType.player());
    }

    public static ServerPlayerEntity playerParameter(final CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        return EntityArgumentType.getPlayer(context, "player");
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> teamArgument() {
        return CommandManager.argument("team", TeamArgumentType.team());
    }

    public static Team teamParameter(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return TeamArgumentType.getTeam(context, "team");
    }

    public static void feedback(final CommandContext<ServerCommandSource> context, final String feedback) {
        feedback(context, feedback, false);
    }

    public static void feedbackOps(final CommandContext<ServerCommandSource> context, final String feedback) {
        feedback(context, feedback, true);
    }

    public static void feedback(final CommandContext<ServerCommandSource> context, final String feedback,
            final boolean ops) {
        context.getSource().sendFeedback(new TranslatableText(feedback), ops);
    }

    public static CommandSyntaxException error(final String error) {
        return new SimpleCommandExceptionType(new TranslatableText(error)).create();
    }

    public static CommandSyntaxException handleError(final Exception e) {
        if (e instanceof CommandSyntaxException)
            return (CommandSyntaxException) e;
        log.error("Unexpected error in command", e);
        return new SimpleCommandExceptionType(new TranslatableText("tatters.command.error")).create();
    }
}
