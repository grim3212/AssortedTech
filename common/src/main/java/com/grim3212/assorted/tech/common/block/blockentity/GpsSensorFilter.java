package com.grim3212.assorted.tech.common.block.blockentity;

import com.grim3212.assorted.tech.api.util.GpsSensorMode;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A GPS sensor's filter entries, parsed when they change rather than for every entity every tick.
 * An entry is a player name or a mob or item id, and on the upgraded sensor one starting with # is a
 * tag: an entity type tag for mobs, an item tag for items. The sensor sees anything matching any
 * entry. An entry that names nothing simply never matches; {@link #problem} is how the screen says so.
 */
public final class GpsSensorFilter {

    public static final GpsSensorFilter ANY = new GpsSensorFilter(List.of(), false);

    private final Set<String> names = new HashSet<>();
    private final Set<Identifier> ids = new HashSet<>();
    private final List<TagKey<EntityType<?>>> entityTags = new ArrayList<>();
    private final List<TagKey<Item>> itemTags = new ArrayList<>();
    private final boolean empty;

    private GpsSensorFilter(List<String> entries, boolean allowTags) {
        this.empty = entries.isEmpty();
        for (String entry : entries) {
            if (isTag(entry)) {
                Identifier tag = Identifier.tryParse(entry.substring(1));
                if (allowTags && tag != null) {
                    this.entityTags.add(TagKey.create(Registries.ENTITY_TYPE, tag));
                    this.itemTags.add(TagKey.create(Registries.ITEM, tag));
                }
            } else {
                this.names.add(entry.toLowerCase(Locale.ROOT));
                Identifier id = Identifier.tryParse(entry);
                if (id != null) {
                    this.ids.add(id);
                }
            }
        }
    }

    /** A plain sensor's one typed entry, where a tag is not allowed and so matches nothing. */
    public static GpsSensorFilter single(String entry) {
        return entry.isEmpty() ? ANY : new GpsSensorFilter(List.of(entry), false);
    }

    public static GpsSensorFilter of(List<String> entries) {
        return entries.isEmpty() ? ANY : new GpsSensorFilter(entries, true);
    }

    public static boolean isTag(String entry) {
        return entry.startsWith("#");
    }

    /**
     * How an entry is stored: ids and tags with their namespace written out, so {@code pig} and
     * {@code minecraft:pig} are the same entry. Player names are kept as typed.
     */
    public static String normalize(GpsSensorMode mode, String entry) {
        String stripped = entry.strip();
        if (mode == GpsSensorMode.PLAYERS) {
            return stripped;
        }
        boolean tag = isTag(stripped);
        Identifier id = Identifier.tryParse(tag ? stripped.substring(1) : stripped);
        return id == null ? stripped : (tag ? "#" : "") + id;
    }

    /** Nothing to narrow by, so the mode's own rule decides alone. */
    public boolean isEmpty() {
        return this.empty;
    }

    public boolean matchesPlayer(String name) {
        return this.names.contains(name.toLowerCase(Locale.ROOT));
    }

    public boolean matchesMob(Holder<EntityType<?>> type) {
        return this.ids.stream().anyMatch(type::is) || this.entityTags.stream().anyMatch(type::is);
    }

    public boolean matchesItem(Holder<Item> item) {
        return this.ids.stream().anyMatch(item::is) || this.itemTags.stream().anyMatch(item::is);
    }

    /**
     * Why an entry can never match in this mode, or null when it is fine. Tags are looked up in the
     * registries, which the client has too once tags have synced.
     */
    public static @Nullable Component problem(GpsSensorMode mode, String entry, boolean upgraded) {
        if (entry.isEmpty()) {
            return null;
        }
        if (isTag(entry)) {
            if (mode == GpsSensorMode.PLAYERS) {
                return Component.translatable("gps_sensor.screen.problem.player_tag");
            }
            if (!upgraded) {
                return Component.translatable("gps_sensor.screen.problem.tag");
            }
            Identifier tag = Identifier.tryParse(entry.substring(1));
            boolean known = tag != null && (mode == GpsSensorMode.MOBS
                    ? BuiltInRegistries.ENTITY_TYPE.get(TagKey.create(Registries.ENTITY_TYPE, tag)).isPresent()
                    : BuiltInRegistries.ITEM.get(TagKey.create(Registries.ITEM, tag)).isPresent());
            return known ? null : Component.translatable("gps_sensor.screen.problem.unknown_tag", entry);
        }
        boolean known = switch (mode) {
            case PLAYERS -> StringUtil.isValidPlayerName(entry);
            case MOBS -> isKnown(BuiltInRegistries.ENTITY_TYPE, entry);
            case ITEMS -> isKnown(BuiltInRegistries.ITEM, entry);
        };
        return known ? null : Component.translatable("gps_sensor.screen.problem.unknown", entry);
    }

    private static boolean isKnown(Registry<?> registry, String entry) {
        Identifier id = Identifier.tryParse(entry);
        return id != null && registry.containsKey(id);
    }
}
