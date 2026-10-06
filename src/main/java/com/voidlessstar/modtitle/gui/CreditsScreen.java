package com.voidlessstar.modtitle.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IConfigurable;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CreditsScreen extends Screen {

    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final int VERSION_COLOR = 0xFFA9A9A9;
    private static final int ID_COLOR = 0xFF7A7A7A;
    private static final int AUTHOR_COLOR = 0xFFB0BEC5;
    private static final int TITLE_COLOR = 0xFFB39DDB;

    private final Screen parent;
    private CreditsList list;

    public CreditsScreen(Screen parent) {
        super(Component.translatable("modtitle.credits.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<IModInfo> mods = ModList.get().getMods().stream()
                .sorted(Comparator.comparing(mod -> mod.getDisplayName(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        int listWidth = Math.min(380, this.width - 40);
        int listX = (this.width - listWidth) / 2;
        int listY = 54;
        int listHeight = this.height - 54 - 66;

        this.list = new CreditsList(this.minecraft, listWidth, listHeight, listY, 22, mods);
        this.list.setLeftPos(listX);

        this.addRenderableWidget(this.list);
        this.addRenderableWidget(new StyledButton(this.width / 2 - 100, this.height - 40, 200, 20,
                Component.translatable("modtitle.credits.back"), btn -> this.onClose(), TITLE_COLOR));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xC0182038, 0xD00A1220);
        graphics.drawCenteredString(this.font, Component.translatable("modtitle.credits.title"), this.width / 2, 14, TITLE_COLOR);
        graphics.drawCenteredString(this.font, Component.translatable("modtitle.credits.company"), this.width / 2, 27, ID_COLOR);
        graphics.drawCenteredString(this.font, Component.translatable("modtitle.credits.count", this.list.modtitle$getItemCount()), this.width / 2, 40, VERSION_COLOR);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private class CreditsList extends ObjectSelectionList<CreditsEntry> {

        CreditsList(Minecraft minecraft, int width, int height, int y, int itemHeight, List<IModInfo> mods) {
            super(minecraft, width, height, y, itemHeight, 0);
            for (IModInfo mod : mods) {
                this.addEntry(new CreditsEntry(mod));
            }
        }

        public int modtitle$getItemCount() {
            return this.getItemCount();
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRight() + 2;
        }

        @Override
        public int getRowWidth() {
            return this.width;
        }
    }

    private class CreditsEntry extends ObjectSelectionList.Entry<CreditsEntry> {

        private final IModInfo mod;

        CreditsEntry(IModInfo mod) {
            this.mod = mod;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {            String displayName = this.mod.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = this.mod.getModId();
            }
            String version = this.mod.getVersion().toString();

            graphics.drawString(font, displayName, left + 4, top + 3, NAME_COLOR, false);
            graphics.drawString(font, "v" + version, left + 4 + font.width(displayName) + 8, top + 3, VERSION_COLOR, false);

            String line = this.mod.getModId();
            String authors = ((IConfigurable) this.mod).<String>getConfigElement("authors").orElse(null);
            if (authors != null && !authors.isEmpty()) {
                line = line + " - " + authors;
            }
            graphics.drawString(font, line, left + 4, top + 13, ID_COLOR, false);
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }
    }
}
