package com.voidlessstar.modtitle.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class StyledButton extends Button {

    public StyledButton(int x, int y, int width, int height, Component message, OnPress onPress, int color) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.setFGColor(color);
    }
}
