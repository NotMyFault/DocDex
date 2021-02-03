package me.piggypiglet.docdex.bot.commands.implementations.documentation;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.util.Types;
import me.piggypiglet.docdex.bot.commands.framework.BotCommand;
import me.piggypiglet.docdex.bot.embed.documentation.DocumentationObjectSerializer;
import me.piggypiglet.docdex.config.Config;
import me.piggypiglet.docdex.db.server.Server;
import me.piggypiglet.docdex.documentation.IndexURLBuilder;
import me.piggypiglet.docdex.documentation.objects.DocumentedObjectResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// ------------------------------
// Copyright (c) PiggyPiglet 2020
// https://www.piggypiglet.me
// ------------------------------
public abstract class DocumentationCommand extends BotCommand {
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int BAD_GATEWAY = 502;

    private static final Pattern DISALLOWED_CHARACTERS = Pattern.compile("[^a-zA-Z0-9.$%_#\\-, ()]");
    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("([ ](?![^(]*\\)))");
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private static final Type OBJECT_LIST = Types.listOf(DocumentedObjectResult.class);

    private final Config config;

    protected DocumentationCommand(@NotNull final Set<String> matches, @NotNull final String description,
                                   @NotNull final Config config) {
        super(matches, "[javadoc] [limit/$(first result)] <query>", description);
        this.config = config;
    }

    @Nullable
    @Override
    public RestAction<Message> run(final @NotNull User user, final @NotNull Message message,
                                   @NotNull final Server server, final int start) {
        final List<String> args = args(message, start);
        final MessageChannel channel = message.getChannel();

        if (DISALLOWED_CHARACTERS.matcher(String.join(" ", args)).find()) {
            queueAndDelete(channel.sendMessage("You have disallowed characters in your query. Allowed characters: `a-zA-Z0-9.$%_#-, ()`"));
            return null;
        }

        if (args.isEmpty() || args.get(0).isBlank()) {
            queueAndDelete(channel.sendMessage("**Incorrect usage:**\nCorrect usage is: " +
                    message.getContentRaw().substring(0, start) + "[javadoc] [limit/$(first result)] <query>"));
            return null;
        }

        final AtomicReference<String> javadoc = new AtomicReference<>(server.getDefaultJavadoc());
        final AtomicInteger limit = new AtomicInteger();
        final AtomicBoolean returnClosest = new AtomicBoolean(false);
        final String query;

        if (args.size() == 1) {
            query = String.join(" ", args);
        } else if (args.size() == 2) {
            final String first = args.get(0);

            try {
                limit.set(Integer.parseInt(first));
            } catch (NumberFormatException exception) {
                if (first.equals("$")) {
                    returnClosest.set(true);
                } else {
                    javadoc.set(first);
                }
            }

            query = String.join(" ", args.subList(1, args.size()));
        } else {
            javadoc.set(args.get(0));

            final String second = args.get(1);

            try {
                limit.set(Integer.parseInt(second));
            } catch (NumberFormatException exception) {
                if (second.equals("$")) {
                    returnClosest.set(true);
                } else {
                    queueAndDelete(message.getChannel().sendMessage("Invalid limit."));
                    return null;
                }
            }

            query = String.join(" ", args.subList(2, args.size()));
        }

        final IndexURLBuilder urlBuilder = new IndexURLBuilder()
                .javadoc(javadoc.get().toLowerCase())
                .query(query.replace(" ", "%20"))
                .algorithm(server.getAlgorithm());

        if (limit.get() != 0) {
            urlBuilder.limit(limit.get());
        }

        final String uri = config.getUrl() + urlBuilder.build();
        final HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                .build();
        final HttpResponse<String> response;

        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            LOGGER.error("Interrupted.", exception);
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException exception) {
            LOGGER.error("Something went wrong when requesting from " + uri, exception);
            return null;
        }

        final String body = response.body();

        if (response.statusCode() == SERVICE_UNAVAILABLE) {
            queueAndDelete(channel.sendMessage(body));
            return null;
        }

        if (response.statusCode() == BAD_GATEWAY) {
            queueAndDelete(channel.sendMessage("I am currently under maintenance."));
            return null;
        }

        if (body.equalsIgnoreCase("null")) {
            queueAndDelete(channel.sendMessage("Unknown javadoc: " + javadoc.get() + '.'));
            return null;
        }

        //noinspection unchecked
        final List<Map.Entry<DocumentedObjectResult, EmbedBuilder>> objects = ((List<DocumentedObjectResult>) GSON.fromJson(body, OBJECT_LIST)).stream()
                .map(result -> Map.entry(result, DocumentationObjectSerializer.toEmbed(user, javadoc.get(), result.getObject())))
                .collect(Collectors.toList());

        return execute(message, objects, (objects.size() == 1 && limit.get() == 0) || returnClosest.get());
    }

    @Nullable
    protected abstract RestAction<Message> execute(final @NotNull Message message, final @NotNull List<Map.Entry<DocumentedObjectResult, EmbedBuilder>> objects,
                                                   final boolean returnFirst);

    @NotNull
    @Override
    protected List<String> args(final @NotNull Message message, final int start) {
        return Arrays.asList(ARGUMENT_PATTERN.split(message.getContentRaw().substring(start).trim()));
    }

    private static void queueAndDelete(@NotNull final MessageAction message) {
        message.queue(sentMessage -> sentMessage.delete().queueAfter(20, TimeUnit.SECONDS));
    }
}
