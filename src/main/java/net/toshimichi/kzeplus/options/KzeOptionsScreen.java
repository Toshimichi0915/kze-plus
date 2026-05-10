package net.toshimichi.kzeplus.options;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.toshimichi.kzeplus.KzePlus;

public class KzeOptionsScreen extends GameOptionsScreen {

    public KzeOptionsScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, Text.translatable("kze_plus.options.title"));
    }

    private static Text getPercentValueText(Text prefix, double value) {
        return Text.translatable("options.percent_value", prefix, (int) (value * 100.0));
    }

    @Override
    protected void addOptions() {
        KzeOptions options = KzePlus.getInstance().getOptions();
        SimpleOption<?>[] arr = {
                VisibilityMode.createSimpleOption("kze_plus.options.visibility_on_sneak", options.getHideOnSneak(), options::setHideOnSneak),
                VisibilityMode.createSimpleOption("kze_plus.options.visibility_on_sprint", options.getHideOnSprint(), options::setHideOnSprint),
                SimpleOption.ofBoolean("kze_plus.options.no_fog", options.isNoFog(), options::setNoFog),
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
        };

        body.addAll(arr);
    }

    @Override
    public void close() {
        KzePlus.getInstance().saveOptions();
        super.close();
    }
}
