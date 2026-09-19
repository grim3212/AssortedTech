package com.grim3212.assorted.tech.api.util;

import net.minecraft.util.StringRepresentable;

/** What a GPS sensor looks for at the position it watches. */
public enum GpsSensorMode implements StringRepresentable {
    PLAYERS("players"),
    MOBS("mobs"),
    ITEMS("items");

    public static final StringRepresentable.EnumCodec<GpsSensorMode> CODEC = StringRepresentable.fromEnum(GpsSensorMode::values);

    private final String name;

    GpsSensorMode(String name) {
        this.name = name;
    }

    public GpsSensorMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
