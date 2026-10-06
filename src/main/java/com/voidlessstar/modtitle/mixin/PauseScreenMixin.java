package com.voidlessstar.modtitle.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.voidlessstar.modtitle.ModTitle;
import com.voidlessstar.modtitle.gui.StyledButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Set;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {

    private static final ResourceLocation CUSTOM_LOGO = new ResourceLocation(ModTitle.MODID, "textures/gui/title/logo.png");
    private static final int LOGO_TEXTURE_SIZE = 1024;
    private static final Set<String> VANILLA_PAUSE_BUTTON_KEYS = Set.of(
        "menu.returnToGame", "gui.advancements", "gui.stats", "menu.sendFeedback",
        "menu.reportBugs", "menu.options", "menu.shareToLan", "menu.playerReporting",
        "fml.menu.mods", "menu.returnToMenu", "menu.disconnect");
    private static final Set<String> TITLE_KEYS = Set.of("menu.game", "menu.paused");

    protected PauseScreenMixin() {
        super(Component.literal(""));
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void modtitle$rebuildMenu(CallbackInfo ci) {
        for (GuiEventListener listener : new ArrayList<>(this.children())) {
            if (listener instanceof AbstractWidget widget
                && widget.getMessage().getContents() instanceof TranslatableContents contents
                && (VANILLA_PAUSE_BUTTON_KEYS.contains(contents.getKey()) || TITLE_KEYS.contains(contents.getKey()))) {
                this.removeWidget(widget);
            }
        }
        int bw = 200;
        int bh = 20;
        int cx = this.width / 2;
        int y = this.height / 2 - 42;
        this.addRenderableWidget(new StyledButton(cx - bw / 2, y, bw, bh, Component.translatable("menu.returnToGame"), btn -> this.onClose(), 0xFF55FF55));
        this.addRenderableWidget(new StyledButton(cx - bw / 2, y + 24, bw, bh, Component.translatable("menu.options"), btn -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options)), 0xFF55FFFF));
        this.addRenderableWidget(new StyledButton(cx - bw / 2, y + 48, bw, bh, this.minecraft.isLocalServer() ? Component.translatable("menu.returnToMenu") : Component.translatable("menu.disconnect"), btn -> this.modtitle$quit(), 0xFFFF5555));
    }

    private void modtitle$quit() {
        Minecraft mc = this.minecraft;
        if (mc.isLocalServer()) {
            mc.level.disconnect();
            mc.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
            mc.setScreen(new TitleScreen());
        } else if (mc.isConnectedToRealms()) {
            mc.clearLevel();
            mc.setScreen(new RealmsMainScreen(new TitleScreen()));
        } else {
            mc.clearLevel();
            mc.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/PauseScreen;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void modtitle$coloredBackground(PauseScreen self, GuiGraphics graphics) {
        graphics.fillGradient(0, 0, this.width, this.height, 0x33000000, 0x4D000000);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void modtitle$renderLogo(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        int size = Math.min(100, (int) (this.width * 0.18f));
        int x = this.width / 2 - size / 2;
        int y = 30;
        RenderSystem.enableBlend();
        graphics.setColor(1.0F, 1.0F, 1.0F, 0.9F);
        graphics.blit(CUSTOM_LOGO, x, y, size, size, 0, 0, LOGO_TEXTURE_SIZE, LOGO_TEXTURE_SIZE, LOGO_TEXTURE_SIZE, LOGO_TEXTURE_SIZE);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
