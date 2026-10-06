package com.voidlessstar.modtitle.mixin;

import com.voidlessstar.modtitle.ModTitle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoRenderer.class)
public class LogoRendererMixin {

    private static final ResourceLocation CUSTOM_LOGO = new ResourceLocation(ModTitle.MODID, "textures/gui/title/logo.png");
    private static final int LOGO_TEXTURE_SIZE = 1024;

    @Inject(method = "renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IFI)V", at = @At("HEAD"), cancellable = true)
    private void modtitle$renderCustomLogo(GuiGraphics graphics, int screenWidth, float alpha, int heightOffset, CallbackInfo ci) {
        int size = Math.min(105, (int) (screenWidth * 0.20f));
        int x = screenWidth / 2 - size / 2;
        int y = 26;
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(CUSTOM_LOGO, x, y, size, size, 0, 0, LOGO_TEXTURE_SIZE, LOGO_TEXTURE_SIZE, LOGO_TEXTURE_SIZE, LOGO_TEXTURE_SIZE);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        ci.cancel();
    }
}