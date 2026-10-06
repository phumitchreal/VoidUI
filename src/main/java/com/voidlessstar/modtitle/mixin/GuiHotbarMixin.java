package com.voidlessstar.modtitle.mixin;

import com.voidlessstar.modtitle.hud.HotbarHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiHotbarMixin {

    private static final int HIDE_AFTER_TICKS = 100;

    private int modtitle$lastSelected = -1;
    private int modtitle$idleTicks = 0;
    private boolean modtitle$pushed = false;

    @Inject(method = "tick(Z)V", at = @At("TAIL"))
    private void modtitle$onTick(boolean ignored, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            HotbarHudState.anim = 0.0F;
            return;
        }
        int selected = mc.player.getInventory().selected;
        if (selected != this.modtitle$lastSelected) {
            this.modtitle$lastSelected = selected;
            this.modtitle$idleTicks = 0;
        } else if (this.modtitle$idleTicks < HIDE_AFTER_TICKS) {
            this.modtitle$idleTicks++;
        }
        float target = this.modtitle$idleTicks >= HIDE_AFTER_TICKS ? 1.0F : 0.0F;
        if (HotbarHudState.anim < target) {
            HotbarHudState.anim = Math.min(target, HotbarHudState.anim + 0.1F);
        } else if (HotbarHudState.anim > target) {
            HotbarHudState.anim = Math.max(target, HotbarHudState.anim - 0.1F);
        }
    }

    @Inject(method = "renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V", at = @At("HEAD"), cancellable = true)
    private void modtitle$hotbarHead(float partialTick, GuiGraphics graphics, CallbackInfo ci) {
        if (HotbarHudState.anim >= 1.0F) {
            ci.cancel();
            return;
        }
        if (HotbarHudState.anim > 0.0F) {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, HotbarHudState.OFFSET_PX * HotbarHudState.anim, 0.0F);
            this.modtitle$pushed = true;
        }
    }

    @Inject(method = "renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V", at = @At("TAIL"))
    private void modtitle$hotbarTail(float partialTick, GuiGraphics graphics, CallbackInfo ci) {
        if (this.modtitle$pushed) {
            graphics.pose().popPose();
            this.modtitle$pushed = false;
        }
    }
}
