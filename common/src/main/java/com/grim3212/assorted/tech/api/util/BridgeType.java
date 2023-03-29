package com.grim3212.assorted.tech.api.util;

import net.minecraft.util.StringRepresentable;

public enum BridgeType implements StringRepresentable {

    LASER("laser", 0x00FF00, true),
    ACCEL("accel", 0x0078FF, true),
    TRICK("trick", 0xffff00, false),
    DEATH("death", 0xff0000, false),
    GRAVITY("gravity", 0xffffff, false);

    private final String unlocalized;
    private final int renderColor;
    private final boolean isSolid;

    private BridgeType(String unlocalized, int renderColor, boolean isSolid) {
        this.unlocalized = unlocalized;
        this.renderColor = renderColor;
        this.isSolid = isSolid;
    }

    public int getRenderColor() {
        return renderColor;
    }

    public String getUnlocalized() {
        return unlocalized;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public static BridgeType[] getValues() {
        return values;
    }

    public static final BridgeType values[] = values();

    @Override
    public String getSerializedName() {
        return this.unlocalized;
    }
}
