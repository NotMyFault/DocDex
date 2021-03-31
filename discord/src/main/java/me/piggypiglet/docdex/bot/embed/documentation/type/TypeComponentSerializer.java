package me.piggypiglet.docdex.bot.embed.documentation.type;

import com.google.common.collect.Lists;
import me.piggypiglet.docdex.documentation.objects.DocumentedObject;
import me.piggypiglet.docdex.documentation.objects.DocumentedTypes;
import me.piggypiglet.docdex.documentation.objects.type.TypeMetadata;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// ------------------------------
// Copyright (c) PiggyPiglet 2020
// https://www.piggypiglet.me
// ------------------------------
public final class TypeComponentSerializer {
    private static final Map<TypeComponents, Function<TypeMetadata, Set<String>>> COMPONENT_GETTERS = new EnumMap<>(TypeComponents.class);

    static {
        COMPONENT_GETTERS.put(TypeComponents.EXTENSIONS, TypeMetadata::getExtensions);
        COMPONENT_GETTERS.put(TypeComponents.IMPLEMENTATIONS, TypeMetadata::getImplementations);
        COMPONENT_GETTERS.put(TypeComponents.ALL_IMPLEMENTATIONS, TypeMetadata::getAllImplementations);
        COMPONENT_GETTERS.put(TypeComponents.SUB_INTERFACES, TypeMetadata::getSubInterfaces);
        COMPONENT_GETTERS.put(TypeComponents.SUB_CLASSES, TypeMetadata::getSubClasses);
        COMPONENT_GETTERS.put(TypeComponents.IMPLEMENTING_CLASSES, TypeMetadata::getImplementingClasses);
        COMPONENT_GETTERS.put(TypeComponents.METHODS, TypeMetadata::getMethods);
        COMPONENT_GETTERS.put(TypeComponents.FIELDS, TypeMetadata::getFields);
    }

    private TypeComponentSerializer() {
        throw new AssertionError("This class cannot be instantiated.");
    }

    @NotNull
    public static List<EmbedBuilder> toEmbeds(@NotNull final DocumentedObject object, @NotNull final TypeComponents component) {
        if (!DocumentedTypes.isType(object.getType())) {
            return Collections.emptyList();
        }

        final TypeMetadata metadata = (TypeMetadata) object.getMetadata();

        if (metadata == null || component == null || !COMPONENT_GETTERS.containsKey(component)) {
            System.out.println("Looks like either the metadata, component, or corresponding component value was null in TypeComponentSerializer.");
            System.out.println(metadata);
            System.out.println(component);
        }

        final List<List<String>> pages = Lists.partition(new ArrayList<>(COMPONENT_GETTERS.get(component).apply(metadata)), 15);

        return pages.stream()
                .map(page -> String.join("\n", page))
                .map(page -> new EmbedBuilder().addField(component.getFormattedPlural() + ':', "```\n" + page + "```", false))
                .collect(Collectors.toList());
    }
}
