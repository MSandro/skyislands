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

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import tatters.TattersMain;
import tatters.common.TattersChunkGenerator;
import tatters.config.SkyblockConfig;
import tatters.config.TattersConfig;

@Environment(EnvType.CLIENT)
public class TattersLevelScreen extends Screen {
    private static final Component LOBBY_TEXT = new TranslatableComponent("tatters.gui.lobby");
    private static final Component SKYBLOCK_TEXT = new TranslatableComponent("tatters.gui.skyblock");

    private final Screen parent;
    private final WorldGenSettings generatorOptions;
    private SkyblockListWidget lobbySelectionList;
    private SkyblockListWidget skyblockSelectionList;
    private List<SkyblockConfig> skyblockConfigs;
    private Button confirmButton;

    public TattersLevelScreen(final Screen parent, final WorldGenSettings generatorOptions) {
        super(new TranslatableComponent("generator.tatters"));
        this.parent = parent;
        this.generatorOptions = generatorOptions;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
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
            SystemToast.add(this.minecraft.getToasts(), SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, SKYBLOCK_TEXT, new TranslatableComponent("tatters.gui.error"));
            this.minecraft.setScreen(this.parent);
            return;
        }

        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.lobbySelectionList = new SkyblockListWidget(config.getLobbyConfig(false), true, 0);
        this.children.add(this.lobbySelectionList);
        this.skyblockSelectionList = new SkyblockListWidget(config.getSkyblockConfig(), false, this.width / 2);
        this.children.add(this.skyblockSelectionList);
        this.confirmButton = this.addButton(
                new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, (buttonWidget) -> {
                    try {
                        TattersConfig tattersConfig = TattersConfig.getConfig();
                        tattersConfig.lobby = this.lobbySelectionList.selection == null ? ""
                                : this.lobbySelectionList.selection.fileName;
                        tattersConfig.skyblock = this.skyblockSelectionList.selection.fileName;
                        tattersConfig.save();
                        final TattersChunkGenerator generator = (TattersChunkGenerator) this.generatorOptions.overworld();
                        generator.updateConfig();
                    }
                    catch (Throwable e) {
                        TattersMain.log.error(e);
                        SystemToast.add(this.minecraft.getToasts(), SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, SKYBLOCK_TEXT, new TranslatableComponent("tatters.gui.error"));
                    }
                    this.minecraft.setScreen(this.parent);
                }));
        this.addButton(
                new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, (buttonWidget) -> {
                    this.minecraft.setScreen(this.parent);
                }));
        this.lobbySelectionList.autoSelect();
        this.skyblockSelectionList.autoSelect();
    }

    private void refreshConfirmButton() {
        this.confirmButton.active = this.skyblockSelectionList.getSelected() != null;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(0);
        this.lobbySelectionList.render(matrices, mouseX, mouseY, delta);
        this.skyblockSelectionList.render(matrices, mouseX, mouseY, delta);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 8, 16777215);
        drawCenteredString(matrices, this.font, LOBBY_TEXT, (int) (this.width * .25), 28, 10526880);
        drawCenteredString(matrices, this.font, SKYBLOCK_TEXT, (int) (this.width * .75), 28, 10526880);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    class SkyblockListWidget extends ObjectSelectionList<TattersLevelScreen.SkyblockListWidget.SkyblockItem> {

        final boolean includeNone;
        SkyblockConfig selection;

        private SkyblockListWidget(final SkyblockConfig selection, final boolean includeNone, final int left) {
            super(TattersLevelScreen.this.minecraft, TattersLevelScreen.this.width / 2, TattersLevelScreen.this.height,
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
        protected int getScrollbarPosition() {
            return this.x0 + this.width / 2 + 124;
        }

        @Environment(EnvType.CLIENT)
        class SkyblockItem extends ObjectSelectionList.Entry<TattersLevelScreen.SkyblockListWidget.SkyblockItem> {
            private final SkyblockConfig skyblockConfig;
            private final Component text;

            public SkyblockItem(final SkyblockConfig skyblockConfig) {
                this.skyblockConfig = skyblockConfig;
                final String name = this.skyblockConfig == null ? "--" : skyblockConfig.name;
                if (Language.getInstance().has(name)) {
                    this.text = new TranslatableComponent(name);
                } else {
                    this.text = new TextComponent(name);
                }
            }

            @Override
            public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight,
                    int mouseX, int mouseY, boolean hovered, float tickDelta) {
                drawString(matrices, TattersLevelScreen.this.font, this.text, x + 5,
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
