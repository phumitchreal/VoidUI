package com.voidlessstar.modtitle.mixin;

import com.mojang.blaze3d.font.GlyphInfo;
import com.voidlessstar.modtitle.ModTitle;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.flirora.caxton.font.CaxtonTypeface;

@Mixin(CaxtonTypeface.class)
public class CaxtonTypefaceProbeMixin {

    private boolean modtitle$loggedSpace;
    private boolean modtitle$loggedA;
    private boolean modtitle$loggedVowel;
    private boolean modtitle$loggedTone;
    private boolean modtitle$loggedKo;

    private boolean modtitle$relevant(int codePoint) {
        return codePoint == ' ' || codePoint == 'A' || codePoint == 'ก' || codePoint == 0x0E31 || codePoint == 0x0E48;
    }

    private String modtitle$hole(int codePoint) {
        String name = modtitle$loggedA ? "" : (codePoint == 'A' ? " [A] " : "");
        return name;
    }

    @Inject(method = "supportsCodePoint(ILnet/minecraft/network/chat/Style;)Z", at = @At("RETURN"), require = 0, remap = false)
    private void modtitle$logSupport(int codePoint, Style style, CallbackInfoReturnable<Boolean> cir) {
        switch (codePoint) {
            case 'A' -> {
                if (!modtitle$loggedA) {
                    modtitle$loggedA = true;
                    ModTitle.LOGGER.info("[modtitle] caxton supports 'A' -> {} {}", cir.getReturnValue(), style);
                }
            }
            case 0x0E01 -> {
                if (!modtitle$loggedKo) {
                    modtitle$loggedKo = true;
                    ModTitle.LOGGER.info("[modtitle] caxton supports 'ก' -> {}", cir.getReturnValue());
                }
            }
            case 0x0E31 -> {
                if (!modtitle$loggedVowel) {
                    modtitle$loggedVowel = true;
                    ModTitle.LOGGER.info("[modtitle] caxton supports 'ั'(0E31) -> {}", cir.getReturnValue());
                }
            }
            case 0x0E48 -> {
                if (!modtitle$loggedTone) {
                    modtitle$loggedTone = true;
                    ModTitle.LOGGER.info("[modtitle] caxton supports '่'(0E48) -> {}", cir.getReturnValue());
                }
            }
            case ' ' -> {
                if (!modtitle$loggedSpace) {
                    modtitle$loggedSpace = true;
                    ModTitle.LOGGER.info("[modtitle] caxton supports ' ' -> {}", cir.getReturnValue());
                }
            }
            default -> {
            }
        }
    }

    @Inject(method = "getGlyph(I)Lcom/mojang/blaze3d/font/GlyphInfo;", at = @At("RETURN"), require = 0, remap = false)
    private void modtitle$logGlyph(int codePoint, CallbackInfoReturnable<GlyphInfo> cir) {
        GlyphInfo info = cir.getReturnValue();
        String cls = info == null ? "NULL" : info.getClass().getName();
        if (codePoint == 'A' && !modtitle$loggedA) {
            ModTitle.LOGGER.info("[modtitle] caxton glyph 'A' class -> {}", cls);
        } else if (codePoint == 0x0E01 && !modtitle$loggedKo) {
            ModTitle.LOGGER.info("[modtitle] caxton glyph 'ก' class -> {}", cls);
        }
    }
}