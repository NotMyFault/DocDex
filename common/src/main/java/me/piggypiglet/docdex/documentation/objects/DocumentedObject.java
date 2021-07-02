package me.piggypiglet.docdex.documentation.objects;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import me.piggypiglet.docdex.documentation.objects.adaptation.MetadataAdapter;
import me.piggypiglet.docdex.documentation.utils.DataUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// ------------------------------
// Copyright (c) PiggyPiglet 2020
// https://www.piggypiglet.me
// ------------------------------
public final class DocumentedObject {
    private final String link;
    private final DocumentedTypes type;
    @SerializedName("package") private final String packaj;
    private final String name;
    private final String description;
    private final String strippedDescription;
    private final Set<String> annotations;
    private final boolean deprecated;
    private final String deprecationMessage;
    private final Set<String> modifiers;
    private final String since;
    @JsonAdapter(MetadataAdapter.class) private final Object metadata;

    private DocumentedObject(@NotNull final String link, @NotNull final DocumentedTypes type,
                             @NotNull final String packaj, @NotNull final String name,
                             @NotNull final String description, @NotNull final String strippedDescription,
                             @NotNull final Set<String> annotations, final boolean deprecated,
                             @NotNull final String deprecationMessage, @NotNull final Set<String> modifiers,
                             @NotNull final String since, @NotNull final Object metadata) {
        this.link = link;
        this.type = type;
        this.packaj = packaj;
        this.name = name;
        this.description = description;
        this.strippedDescription = strippedDescription;
        this.annotations = annotations;
        this.deprecated = deprecated;
        this.deprecationMessage = deprecationMessage;
        this.modifiers = modifiers;
        this.since = since;
        this.metadata = metadata;
    }

    @NotNull
    public String getLink() {
        return link;
    }

    @NotNull
    public DocumentedTypes getType() {
        return type;
    }

    @NotNull
    public String getPackage() {
        return packaj;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getStrippedDescription() {
        return strippedDescription;
    }

    @NotNull
    public Set<String> getAnnotations() {
        return annotations;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    @NotNull
    public String getDeprecationMessage() {
        return deprecationMessage;
    }

    @NotNull
    public Set<String> getModifiers() {
        return modifiers;
    }

    @NotNull
    public String getSince() {
        return since;
    }

    @NotNull
    public Object getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DocumentedObject object = (DocumentedObject) o;
        return deprecated == object.deprecated && link.equals(object.link) && type == object.type && packaj.equals(object.packaj) && name.equals(object.name) && description.equals(object.description) && strippedDescription.equals(object.strippedDescription) && annotations.equals(object.annotations) && deprecationMessage.equals(object.deprecationMessage) && modifiers.equals(object.modifiers) && since.equals(object.since) && metadata.equals(object.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link, type, packaj, name, description, strippedDescription, annotations, deprecated, deprecationMessage, modifiers, since, metadata);
    }

    @Override
    public String toString() {
        return "DocumentedObject{" +
                "link='" + link + '\'' +
                ", type=" + type +
                ", packaj='" + packaj + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", strippedDescription='" + strippedDescription + '\'' +
                ", annotations=" + annotations +
                ", deprecated=" + deprecated +
                ", deprecationMessage='" + deprecationMessage + '\'' +
                ", modifiers=" + modifiers +
                ", since=" + since +
                ", metadata=" + metadata +
                '}';
    }

    @SuppressWarnings("UnusedReturnValue")
    public abstract static class Builder<T extends Builder<T>> {
        @SuppressWarnings("unchecked")
        private final T instance = (T) this;

        private String link = "";
        private DocumentedTypes type;
        private String packaj;
        private String name;
        private String description = "";
        private String strippedDescription = "";
        private final Set<String> annotations = new HashSet<>();
        private boolean deprecated = false;
        private String deprecationMessage = "";
        private final Set<String> modifiers = new LinkedHashSet<>();
        private String since = "";

        protected Builder() {}

        @NotNull
        public T link(@NotNull final String value) {
            link = value;
            return instance;
        }

        @NotNull
        public T type(@NotNull final DocumentedTypes value) {
            type = value;
            return instance;
        }

        @NotNull
        public T packaj(@NotNull final String value) {
            packaj = value;
            return instance;
        }

        @NotNull
        public T name(@NotNull final String value) {
            name = DataUtils.removeTypeParams(value).replace("\r", "");
            return instance;
        }

        @NotNull
        public T description(@NotNull final String value) {
            description = value;
            return instance;
        }

        @NotNull
        public T strippedDescription(@NotNull final String value) {
            strippedDescription = value;
            return instance;
        }

        @NotNull
        public T annotations(@NotNull final String @NotNull ... values) {
            Collections.addAll(annotations, values);
            return instance;
        }

        @NotNull
        public T annotations(@NotNull final Collection<String> values) {
            annotations.addAll(values);
            return instance;
        }

        @NotNull
        public T deprecated(final boolean value) {
            deprecated = value;
            return instance;
        }

        @NotNull
        public T deprecationMessage(@NotNull final String value) {
            deprecationMessage = value;
            return instance;
        }

        @NotNull
        public T modifiers(@NotNull final String @NotNull ... values) {
            Collections.addAll(modifiers, values);
            return instance;
        }

        @NotNull
        public T modifiers(@NotNull final Collection<String> values) {
            modifiers.addAll(values);
            return instance;
        }

        @NotNull
        public T since(@NotNull final String value) {
            since = value;
            return instance;
        }

        @NotNull
        public abstract DocumentedObject build();

        @NotNull
        protected final DocumentedObject build(@NotNull final Object metadata) {
            return new DocumentedObject(
                    link, type, packaj, name, description, strippedDescription, annotations,
                    deprecated, deprecationMessage, modifiers, since, metadata
            );
        }

        @NotNull
        public String getLink() {
            return link;
        }

        @NotNull
        public DocumentedTypes getType() {
            return type;
        }

        @NotNull
        public String getPackage() {
            return packaj;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getDescription() {
            return description;
        }

        @NotNull
        public String getStrippedDescription() {
            return strippedDescription;
        }

        @NotNull
        public Set<String> getAnnotations() {
            return annotations;
        }

        public boolean isDeprecated() {
            return deprecated;
        }

        @NotNull
        public String getDeprecationMessage() {
            return deprecationMessage;
        }

        @NotNull
        public Set<String> getModifiers() {
            return modifiers;
        }

        @NotNull
        public String getSince() {
            return since;
        }
    }
}
