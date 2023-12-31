package net.toshimichi.kzeplus.options;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.toshimichi.kzeplus.KzePlus;

public class KzeOptionsScreen extends GameOptionsScreen {

    private OptionListWidget list;

    public KzeOptionsScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, Text.translatable("kze_plus.options.title"));
    }

    private static Text getPercentValueText(Text prefix, double value) {
        return Text.translatable("options.percent_value", prefix, (int) (value * 100.0));
    }

    @Override
    protected void init() {
        KzeOptions options = KzePlus.getInstance().getOptions();
        SimpleOption<?>[] arr = {
                VisibilityMode.createSimpleOption("kze_plus.options.visibility_on_sneak", options.getHideOnSneak(), options::setHideOnSneak),
                VisibilityMode.createSimpleOption("kze_plus.options.visibility_on_sprint", options.getHideOnSprint(), options::setHideOnSprint),
                SimpleOption.ofBoolean("kze_plus.options.full_bright", options.isFullBright(), options::setFullBright),
                SimpleOption.ofBoolean("kze_plus.options.no_fog", options.isNoFog(), options::setNoFog),
                SimpleOption.ofBoolean("kze_plus.options.hide_magazine_message", options.isHideMagazineMessage(), options::setHideMagazineMessage),
                SimpleOption.ofBoolean("kze_plus.options.hide_kill_message", options.isHideKillMessage(), options::setHideKillMessage),
                SimpleOption.ofBoolean("kze_plus.options.show_kill_log", options.isShowKillLog(), options::setShowKillLog),
                SimpleOption.ofBoolean("kze_plus.options.show_timer", options.isShowTimer(), options::setShowTimer),
                SimpleOption.ofBoolean("kze_plus.options.show_reload_progress", options.isShowReloadProgress(), options::setShowReloadProgress),
                SimpleOption.ofBoolean("kze_plus.options.show_weapon_info", options.isShowWeaponInfo(), options::setShowWeaponInfo),
                new SimpleOption<>("kze_plus.options.gun_sound_volume", SimpleOption.emptyTooltip(), KzeOptionsScreen::getPercentValueText,
                        SimpleOption.DoubleSliderCallbacks.INSTANCE, options.getGunSoundVolume(), options::setGunSoundVolume),
                new SimpleOption<>("kze_plus.options.damage_sound_volume", SimpleOption.emptyTooltip(), KzeOptionsScreen::getPercentValueText,
                        SimpleOption.DoubleSliderCallbacks.INSTANCE, options.getDamageSoundVolume(), options::setDamageSoundVolume),
                new SimpleOption<>("kze_plus.options.hit_sound_volume", SimpleOption.emptyTooltip(), KzeOptionsScreen::getPercentValueText,
                        SimpleOption.DoubleSliderCallbacks.INSTANCE, options.getHitSoundVolume(), options::setHitSoundVolume),
                SimpleOption.ofBoolean("kze_plus.options.show_reward", options.isShowReward(), options::setShowReward),
                SimpleOption.ofBoolean("kze_plus.options.show_exp", options.isShowExp(), options::setShowExp),
                SimpleOption.ofBoolean("kze_plus.options.show_play_time", options.isShowPlayTime(), options::setShowPlayTime),
                SimpleOption.ofBoolean("kze_plus.options.show_team", options.isShowTeam(), options::setShowTeam),
                SimpleOption.ofBoolean("kze_plus.options.show_bonus", options.isShowBonus(), options::setShowBonus),
                SimpleOption.ofBoolean("kze_plus.options.show_next_vote", options.isShowNextVote(), options::setShowNextVote),
                SimpleOption.ofBoolean("kze_plus.options.show_next_vote_only_when_available", options.isShowNextVoteOnlyWhenAvailable(), options::setShowNextVoteOnlyWhenAvailable),
                SimpleOption.ofBoolean("kze_plus.options.hide_vote_message", options.isHideVoteMessage(), options::setHideVoteMessage),
        };

        list = new OptionListWidget(client, width, height, 32, height - 32, 25);
        list.addAll(arr);

        addSelectableChild(list);
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (buttonWidget) -> close())
                .dimensions(width / 2 - 100, height - 27, 200, 20)
                .build());
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) return true;
        if (list.mouseReleased(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        render(matrices, list, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        KzePlus.getInstance().saveOptions();
        super.close();
    }
}
