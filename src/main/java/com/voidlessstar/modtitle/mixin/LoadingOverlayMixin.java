package com.voidlessstar.modtitle.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.voidlessstar.modtitle.ModTitle;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {

    private static final ResourceLocation CUSTOM_LOGO = new ResourceLocation(ModTitle.MODID, "textures/gui/title/logo.png");
    private static final int LOGO_TEXTURE_SIZE = 1024;
    private static final ResourceLocation DOT = new ResourceLocation(ModTitle.MODID, "textures/gui/title/dot.png");
    private static final int DOT_TEXTURE_SIZE = 64;

    private static final int DOT_TOP = 0xFF9FA8DA;

    @Shadow private float currentProgress;
    @Shadow private long fadeOutStart;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V", ordinal = 0), require = 1)
    private void modtitle$renderLoadingScreen(GuiGraphics graphics, ResourceLocation location, int x, int y, int uOffset, int vOffset, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        int cx = w / 2;

        float alpha = 1.0F;
        if (this.fadeOutStart != -1L) {
            alpha = Mth.clamp(1.0F - (float) (Util.getMillis() - this.fadeOutStart) / 1000.0F, 0.0F, 1.0F);
        }
        if (alpha <= 0.0F) {
            return;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.fillGradient(0, 0, w, h, withAlpha(0xFF000000, alpha), withAlpha(0xFF000000, alpha));

        int logoSize = Mth.clamp(Math.min(125, (int) (w * 0.14f)), 56, 140);
        int logoX = cx - logoSize / 2;
        int logoY = (int) (h * 0.30f) - logoSize / 2;

        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(CUSTOM_LOGO, logoX, logoY, logoSize, logoSize, 0, 0, LOGO_TEXTURE_SIZE, LOGO_TEXTURE_SIZE, LOGO_TEXTURE_SIZE, LOGO_TEXTURE_SIZE);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        this.drawDotLoader(graphics, cx, h - 48, alpha);
    }

    private void drawDotLoader(GuiGraphics graphics, int cx, int y, float alpha) {
        int dot = 7;
        int step = 13;
        int startX = cx - step;
        long t = Util.getMillis();
        int period = 640;
        for (int i = 0; i < 3; i++) {
            int dx = startX + i * step;
            int phase = (int) ((t + i * (period / 3)) % period);
            float half = period / 2.0f;
            float k = 1.0f - (float) Math.abs(phase - half) / half;
            k = k * k;
            int c = mixColor(DOT_TOP, 0xFFF6F4FF, k);
            float cr = ((c >>> 16) & 255) / 255.0f;
            float cg = ((c >>> 8) & 255) / 255.0f;
            float cb = (c & 255) / 255.0f;
            float ca = (60 + 195 * k) / 255.0f * alpha;
            RenderSystem.setShaderColor(cr, cg, cb, ca);
            graphics.blit(DOT, dx, y, dot, dot, 0, 0, DOT_TEXTURE_SIZE, DOT_TEXTURE_SIZE, DOT_TEXTURE_SIZE, DOT_TEXTURE_SIZE);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static int mixColor(int c1, int c2, float t) {
        int a = (int) (((c1 >>> 24) & 255) + (((c2 >>> 24) & 255) - ((c1 >>> 24) & 255)) * t);
        int r = (int) (((c1 >>> 16) & 255) + (((c2 >>> 16) & 255) - ((c1 >>> 16) & 255)) * t);
        int g = (int) (((c1 >>> 8) & 255) + (((c2 >>> 8) & 255) - ((c1 >>> 8) & 255)) * t);
        int b = (int) ((c1 & 255) + ((c2 & 255) - (c1 & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int withAlpha(int argb, float a) {
        int alpha = (int) (((argb >>> 24) & 0xFF) * a);
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V", ordinal = 1), require = 1)
    private void modtitle$skipSecondPass(GuiGraphics graphics, ResourceLocation location, int x, int y, int uOffset, int vOffset, float u, float v, int width, int height, int textureWidth, int textureHeight) {
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V", ordinal = 0), require = 1)
    private void modtitle$skipRedFill(GuiGraphics graphics, RenderType renderType, int x1, int y1, int x2, int y2, int color) {
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V", ordinal = 1), require = 1)
    private void modtitle$skipRedFill2(GuiGraphics graphics, RenderType renderType, int x1, int y1, int x2, int y2, int color) {
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"), require = 1)
    private void modtitle$clearBlack(float r, float g, float b, float a) {
        GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;drawProgressBar(Lnet/minecraft/client/gui/GuiGraphics;IIIIF)V"), require = 1)
    private void modtitle$skipProgressBar(LoadingOverlay overlay, GuiGraphics graphics, int x1, int y1, int x2, int y2, float alpha) {
    }
}