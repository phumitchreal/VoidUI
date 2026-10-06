package com.voidlessstar.modtitle.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Gui.class)
public interface GuiAccessor {

    @Invoker("renderPlayerHealth")
    void modtitle$renderPlayerHealth(GuiGraphics graphics);
}
