/*
 * This file is part of Tatters.
 * Copyright (c) 2021, warjort and others, All rights reserved.
 *
 * Tatters is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tatters is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Tatters.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package tatters.client;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;
import net.minecraft.world.gen.GeneratorOptions;
import tatters.TattersMain;
import tatters.common.TattersChunkGenerator;
import tatters.config.SkyblockConfig;
import tatters.config.TattersConfig;

@Environment(EnvType.CLIENT)
public class TattersLevelScreen extends Screen {
    private static final Text LOBBY_TEXT = new TranslatableText("tatters.gui.lobby");
    private static final Text SKYBLOCK_TEXT = new TranslatableText("tatters.gui.skyblock");

    private final Screen parent;
    private final GeneratorOptions generatorOptions;
    private SkyblockListWidget lobbySelectionList;
    private SkyblockListWidget skyblockSelectionList;
    private List<SkyblockConfig> skyblockConfigs;
    private ButtonWidget confirmButton;

    public TattersLevelScreen(final Screen parent, final GeneratorOptions generatorOptions) {
        super(new TranslatableText("generator.tatters"));
        this.parent = parent;
        this.generatorOptions = generatorOptions;
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }

    @Override
    protected void init() {
        // Ensure configuration is up-to-date with file system
        TattersConfig config;
        try {
            TattersConfig.reload(true);
            config = TattersConfig.getConfig();
            this.skyblockConfigs = config.getActiveSkyblockConfigs();
        }
        catch (Throwable e) {
            TattersMain.log.error(e);
            SystemToast.show(this.client.getToastManager(), SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER, SKYBLOCK_TEXT, new TranslatableText("tatters.gui.error"));
            this.client.openScreen(this.parent);
            return;
        }

        this.client.keyboard.setRepeatEvents(true);
        this.lobbySelectionList = new SkyblockListWidget(config.getLobbyConfig(false), true, 0);
        this.children.add(this.lobbySelectionList);
        this.skyblockSelectionList = new SkyblockListWidget(config.getSkyblockConfig(), false, this.width / 2);
        this.children.add(this.skyblockSelectionList);
        this.confirmButton = this.addButton(
                new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, ScreenTexts.DONE, (buttonWidget) -> {
                    try {
                        TattersConfig tattersConfig = TattersConfig.getConfig();
                        tattersConfig.lobby = this.lobbySelectionList.selection == null ? ""
                                : this.lobbySelectionList.selection.fileName;
                        tattersConfig.skyblock = this.skyblockSelectionList.selection.fileName;
                        tattersConfig.save();
                        final TattersChunkGenerator generator = (TattersChunkGenerator) this.generatorOptions.getChunkGenerator();
                        generator.updateConfig();
                    }
                    catch (Throwable e) {
                        TattersMain.log.error(e);
                        SystemToast.show(this.client.getToastManager(), SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER, SKYBLOCK_TEXT, new TranslatableText("tatters.gui.error"));
                    }
                    this.client.openScreen(this.parent);
                }));
        this.addButton(
                new ButtonWidget(this.width / 2 + 5, this.height - 28, 150, 20, ScreenTexts.CANCEL, (buttonWidget) -> {
                    this.client.openScreen(this.parent);
                }));
        this.lobbySelectionList.autoSelect();
        this.skyblockSelectionList.autoSelect();
    }

    private void refreshConfirmButton() {
        this.confirmButton.active = this.skyblockSelectionList.getSelected() != null;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.lobbySelectionList.render(matrices, mouseX, mouseY, delta);
        this.skyblockSelectionList.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
        drawCenteredText(matrices, this.textRenderer, LOBBY_TEXT, (int) (this.width * .25), 28, 10526880);
        drawCenteredText(matrices, this.textRenderer, SKYBLOCK_TEXT, (int) (this.width * .75), 28, 10526880);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    class SkyblockListWidget extends EntryListWidget<TattersLevelScreen.SkyblockListWidget.SkyblockItem> {

        final boolean includeNone;
        SkyblockConfig selection;

        private SkyblockListWidget(final SkyblockConfig selection, final boolean includeNone, final int left) {
            super(TattersLevelScreen.this.client, TattersLevelScreen.this.width / 2, TattersLevelScreen.this.height,
                    40, TattersLevelScreen.this.height - 37, 16);
            setLeftPos(left);
            this.selection = selection;
            this.includeNone = includeNone;
            if (this.includeNone) {
                this.addEntry(new SkyblockItem(null));
            }
            TattersLevelScreen.this.skyblockConfigs.stream().forEach((skyblockConfig) -> {
                this.addEntry(new SkyblockItem(skyblockConfig));
            });
        }

        @Override
        protected boolean isFocused() {
            return TattersLevelScreen.this.getFocused() == this;
        }

        @Override
        public void setSelected(final SkyblockItem skyblockItem) {
            super.setSelected(skyblockItem);
            if (skyblockItem != null) {
                this.selection = skyblockItem.skyblockConfig;
            }

            TattersLevelScreen.this.refreshConfirmButton();
        }

        public void autoSelect() {
            setSelected(this.children().stream().filter(entry -> sameConfig(entry.skyblockConfig, this.selection)).findFirst()
                    .orElse(null));
        }

        // Override wrong behaviour in parent class
        @Override
        protected int getScrollbarPositionX() {
            return this.left + this.width / 2 + 124;
        }

        @Environment(EnvType.CLIENT)
        class SkyblockItem extends EntryListWidget.Entry<TattersLevelScreen.SkyblockListWidget.SkyblockItem> {
            private final SkyblockConfig skyblockConfig;
            private final Text text;

            public SkyblockItem(final SkyblockConfig skyblockConfig) {
                this.skyblockConfig = skyblockConfig;
                final String name = this.skyblockConfig == null ? "--" : skyblockConfig.name;
                if (Language.getInstance().hasTranslation(name)) {
                    this.text = new TranslatableText(name);
                } else {
                    this.text = new LiteralText(name);
                }
            }

            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight,
                    int mouseX, int mouseY, boolean hovered, float tickDelta) {
                DrawableHelper.drawTextWithShadow(matrices, TattersLevelScreen.this.textRenderer, this.text, x + 5,
                        y + 2, this.skyblockConfig == SkyblockListWidget.this.selection ? 16777215 : 10526880);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    SkyblockListWidget.this.setSelected(this);
                    return true;
                } 
                return false;
            }
        }
    }

    static boolean sameConfig(final SkyblockConfig one, final SkyblockConfig two) {
        if (one == null) {
            return two == null;
        }
        return two != null ?  one.fileName.equals(two.fileName) : false;
    }
}
