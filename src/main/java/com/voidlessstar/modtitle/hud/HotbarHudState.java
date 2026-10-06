package com.voidlessstar.modtitle.hud;

public final class HotbarHudState {

    public static final int OFFSET_PX = 22;

    public static float anim = 0.0F;

    private HotbarHudState() {
    }

    public static int getOffsetPx() {
        return Math.round(OFFSET_PX * anim);
    }
}
