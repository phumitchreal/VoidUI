package com.voidlessstar.modtitle.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashRenderer.class)
public class SplashRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void modtitle$skipSplash(GuiGraphics graphics, int screenWidth, Font font, int color, CallbackInfo ci) {
        ci.cancel();
    }
}