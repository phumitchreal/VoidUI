package com.voidlessstar.modtitle.mixin;

import com.biaocraft.biaohudx.forge.hud.HealthBarRender;
import com.voidlessstar.modtitle.hud.HotbarHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HealthBarRender.class)
public class HealthBarOffsetMixin {

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V", remap = false, at = @At("HEAD"), cancellable = true)
    private void modtitle$shift(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        int off = HotbarHudState.getOffsetPx();
        if (off == 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.gameMode == null || !mc.gameMode.canHurtPlayer()) {
            return;
        }
        ci.cancel();
        Player player = mc.player;
        float x = graphics.guiWidth() / 2.0F - 91.0F;
        float y = graphics.guiHeight() - 39.0F + off;
        ((HealthBarRender) (Object) this).renderHealthBar(graphics, partialTick, x, y, player);
    }
}
