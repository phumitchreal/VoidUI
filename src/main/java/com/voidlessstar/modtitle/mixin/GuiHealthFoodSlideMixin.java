package com.voidlessstar.modtitle.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.voidlessstar.modtitle.ModTitle;
import com.voidlessstar.modtitle.hud.HotbarHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiHealthFoodSlideMixin {

    private static final int FOOD_BAR_WIDTH = 78;
    private static final int FOOD_BAR_HEIGHT = 6;

    private boolean modtitle$foodBarDrawn;

    @Redirect(method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void modtitle$renderPlayerHealthShift(Gui self, GuiGraphics graphics) {
        this.modtitle$foodBarDrawn = false;
        ((GuiAccessor) self).modtitle$renderPlayerHealth(graphics);
    }

    @Redirect(method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V"))
    private void modtitle$noExpBar(Gui self, GuiGraphics graphics, int i) {
        ModTitle.LOGGER.info("modtitle noExpBar handler fired");
    }

    @Redirect(method = "renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void modtitle$foodIconBlit(GuiGraphics graphics, ResourceLocation rl, int x, int y, int u, int v, int w, int h) {
        if (v == 27 || v == 144) {
            if (!this.modtitle$foodBarDrawn) {
                this.modtitle$foodBarDrawn = true;
                ModTitle.LOGGER.info("modtitle food blit u={} v={} at x={} y={} -> drawing bar", u, v, x, y);
                this.modtitle$renderFoodBar(graphics);
            } else {
                ModTitle.LOGGER.info("modtitle food blit u={} v={} at x={} y={} -> skipped", u, v, x, y);
            }
            return;
        }
        graphics.blit(rl, x, y, u, v, w, h);
    }

    private void modtitle$renderFoodBar(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        int x = mc.getWindow().getGuiScaledWidth() / 2 + 13;
        int y = mc.getWindow().getGuiScaledHeight() - 39;
        int food = player.getFoodData().getFoodLevel();
        float sat = player.getFoodData().getSaturationLevel();
        ModTitle.LOGGER.info("modtitle renderFoodBar food={} sat={} at x={} y={}", food, sat, x, y);
        graphics.fill(x - 1, y - 1, x - 1 + FOOD_BAR_WIDTH, y - 1 + FOOD_BAR_HEIGHT, 0x660000FF);
        int foodW = Math.round(Math.min(food / 20.0F, 1.0F) * FOOD_BAR_WIDTH);
        if (foodW > 0) {
            graphics.fill(x - 1, y - 1, x - 1 + foodW, y - 1 + FOOD_BAR_HEIGHT, 0xFFFF0000);
        }
        int satW = Math.round(Math.min(sat / 20.0F, 1.0F) * FOOD_BAR_WIDTH);
        if (satW > 0) {
            graphics.fill(x - 1, y - 1, x - 1 + satW, y - 1 + FOOD_BAR_HEIGHT, 0xCCCCCC00);
        }
        String text = String.valueOf(food);
        Font font = mc.font;
        int tw = Math.round(font.width(text) * 0.8F);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x + FOOD_BAR_WIDTH / 2.0F + 2 - tw / 2.0F, y + 2.5F, 0.0F);
        pose.scale(0.8F, 0.8F, 1.0F);
        graphics.drawString(font, text, 0, 0, 0xFFFFFF, false);
        pose.popPose();
    }
}
