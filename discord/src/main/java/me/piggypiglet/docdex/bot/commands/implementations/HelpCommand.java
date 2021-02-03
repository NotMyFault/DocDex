package me.piggypiglet.docdex.bot.commands.implementations;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import me.piggypiglet.docdex.bot.commands.framework.BotCommand;
import me.piggypiglet.docdex.bot.embed.pagination.PaginationManager;
import me.piggypiglet.docdex.bot.embed.pagination.objects.Pagination;
import me.piggypiglet.docdex.bot.embed.utils.EmbedUtils;
import me.piggypiglet.docdex.db.server.Server;
import me.piggypiglet.docdex.scanning.annotations.Hidden;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

// ------------------------------
// Copyright (c) PiggyPiglet 2020
// https://www.piggypiglet.me
// ------------------------------
@Hidden
public final class HelpCommand extends BotCommand {
    private static final MessageEmbed EMBED = new EmbedBuilder()
            .setColor(EmbedUtils.COLOUR)
            .setAuthor("Help:", null, EmbedUtils.ICON)
            .build();

    private final Set<BotCommand> commands;
    private final Set<Server> servers;
    private final String defaultPrefix;
    private final PaginationManager paginationManager;

    @Inject
    public HelpCommand(@NotNull @Named("jda commands") final Set<BotCommand> commands, @NotNull final Set<Server> servers,
                       @NotNull @Named("default") final Server defaultServer, @NotNull final PaginationManager paginationManager) {
        super(Set.of("help"), "", "This page.");
        this.commands = commands;
        this.servers = servers;
        this.defaultPrefix = defaultServer.getPrefix();
        this.paginationManager = paginationManager;
    }

    @Nullable
    @Override
    public RestAction<Message> execute(final @NotNull User user, final @NotNull Message message) {
        final String prefix;

        if (message.isFromGuild()) {
            prefix = servers.stream()
                    .filter(server -> server.getId().equals(message.getGuild().getId()))
                    .findAny()
                    .map(Server::getPrefix)
                    .orElse(defaultPrefix);
        } else {
            prefix = defaultPrefix;
        }

        final List<String> helpMessages = new ArrayList<>();

        commands.stream().filter(command -> command != this).forEach(command -> {
            final String aliases = "**Command(s):** " + Lists.partition(new ArrayList<>(command.getMatches()), 3).stream()
                    .map(list -> String.join(", ", list.stream().map(match -> prefix + match).collect(Collectors.toSet())))
                    .collect(Collectors.joining("\n⠀⠀⠀⠀⠀ ⠀⠀⠀⠀"));
            final String usage = "**Example:** " + prefix + command.getMatches().iterator().next() + ' ' + command.getUsage();
            final String description = "**Description:** " + command.getDescription();
            helpMessages.add(aliases + '\n' + usage + '\n' + description + '\n');
        });

        helpMessages.sort(Comparator.comparingInt(String::length));

        final List<MessageEmbed> pages = Lists.partition(helpMessages, 4).stream()
                .map(list -> String.join("\n", list))
                .map(page -> new EmbedBuilder(EMBED).setDescription(page).build())
                .collect(Collectors.toList());
        final Pagination pagination = Pagination.builder()
                .pages(pages)
                .author(user.getId())
                .build();

        return Optional.ofNullable(pagination.send(message)).map(messageRestAction -> messageRestAction.map(success -> {
            paginationManager.addPaginatedMessage(success.getId(), pagination);
            return success;
        })).orElse(null);
    }
}
