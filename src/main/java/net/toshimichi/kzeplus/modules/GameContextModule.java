package net.toshimichi.kzeplus.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.game.GameContext;
import net.toshimichi.kzeplus.context.weapon.WeaponContext;
import net.toshimichi.kzeplus.context.weapon.WeaponInfo;
import net.toshimichi.kzeplus.context.weapon.WeaponRegistry;
import net.toshimichi.kzeplus.events.ChatEvent;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.events.SoundPlayEvent;
import net.toshimichi.kzeplus.utils.GameRole;
import net.toshimichi.kzeplus.utils.KzeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameContextModule implements Module {

    private static final Identifier GAME_START_SOUND = Identifier.of("minecraft", "count.siren");
    private static final Identifier HUMAN_WIN_SOUND = Identifier.of("minecraft", "count.hwin");
    private static final Identifier ZOMBIE_WIN_SOUND = Identifier.of("minecraft", "count.zwin");
    private static final Pattern REWARD_PATTERN = Pattern.compile("》 +?合計 +?\\+?(\\d+?)円");
    private static final Pattern EXP_PATTERN = Pattern.compile("》 +?取得パーク経験値 +?\\+?(\\d+?) \\(ナイフ武器\\)");
    private static final Pattern DEFENSE_BONUS_PATTERN = Pattern.compile("》 +?防衛ボーナス +?\\+?(\\d+?)円");
    private static final Pattern HIT_BONUS_PATTERN = Pattern.compile("》 +?被弾ボーナス +?\\+?(\\d+?)円");
    private double prevHealth;

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
    }

    private void calcRewardPerHit(GameContext c1, GameContext c2) {
        if (!c1.isEnded()) return;
        if (!c2.isEnded()) return;

        String mainWeaponName1 = c1.getMainWeaponName();
        String subWeaponName1 = c1.getSubWeaponName();

        String mainWeaponName2 = c2.getMainWeaponName();
        String subWeaponName2 = c2.getSubWeaponName();

        if (mainWeaponName1 == null) return;
        if (subWeaponName1 == null) return;
        if (!mainWeaponName1.equals(mainWeaponName2)) return;
        if (!subWeaponName1.equals(subWeaponName2)) return;

        int mainHitCount1 = c1.getMainHitCount();
        int subHitCount1 = c1.getSubHitCount();
        int reward1 = c1.getDefenseBonus();
        if (reward1 >= 10000) return;

        int mainHitCount2 = c2.getMainHitCount();
        int subHitCount2 = c2.getSubHitCount();
        int reward2 = c2.getDefenseBonus();
        if (reward2 >= 10000) return;

        /* cramer's rule
         * mainHitCount1 * x1 + subHitCount1 * x2 = reward1
         * mainHitCount2 * x1 + subHitCount2 * x2 = reward2
         *
         * therefore
         * det = mainHitCount1 * subHitCount2 - subHitCount1 * mainHitCount2
         * x1 = (reward1 * subHitCount2 - reward2 * subHitCount1) / det
         * x2 = (reward2 * mainHitCount1 - reward1 * mainHitCount2) / det
         */
        int det = mainHitCount1 * subHitCount2 - subHitCount1 * mainHitCount2;
        if (det == 0) return;

        int x1 = (reward1 * subHitCount2 - reward2 * subHitCount1) / det;
        int x2 = (reward2 * mainHitCount1 - reward1 * mainHitCount2) / det;

        WeaponRegistry registry = KzePlus.getInstance().getWeaponRegistry();

        WeaponInfo i1 = c1.getMainWeapon();
        registry.save(new WeaponInfo(mainWeaponName1, i1.getMagazineSize(), i1.getReloadTicks(), i1.getBulletPerReload(), x1));

        WeaponInfo i2 = c1.getSubWeapon();
        registry.save(new WeaponInfo(subWeaponName1, i2.getMagazineSize(), i2.getReloadTicks(), i2.getBulletPerReload(), x2));
    }

    @EventTarget
    private void createGameContext(SoundPlayEvent e) {
        if (!e.getSound().getId().equals(GAME_START_SOUND)) return;
        KzePlus.getInstance().getGameContextRegistry().startGameContext();
    }

    @EventTarget
    private void updateWeapon(ClientTickEvent e) {
        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context == null) return;

        WeaponContext main = KzePlus.getInstance().getMainWeaponContext();
        WeaponContext sub = KzePlus.getInstance().getSubWeaponContext();

        if (main.getName() != null) context.setMainWeaponName(main.getName());
        if (sub.getName() != null) context.setSubWeaponName(sub.getName());
    }

    @EventTarget
    private void updateGameEnd(SoundPlayEvent e) {
        Identifier id = e.getSound().getId();
        if (!id.equals(HUMAN_WIN_SOUND) && !id.equals(ZOMBIE_WIN_SOUND)) return;

        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context == null) return;

        context.setHumanWin(id.equals(HUMAN_WIN_SOUND));
    }

    @EventTarget
    private void resetGameContext(ClientTickEvent e) {
        if (KzeUtils.isInGame()) return;
        KzePlus.getInstance().getGameContextRegistry().endGameContext();
    }

    @EventTarget
    private void updateDefenseBonus(SoundPlayEvent e) {
        if (e.getSound() != SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP) return;
        if (e.getPitch() != 0) return;

        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context == null) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        int slot = player.getInventory().selectedSlot;

        if (slot == 0) context.setMainHitCount(context.getMainHitCount() + 1);
        else if (slot == 1) context.setSubHitCount(context.getSubHitCount() + 1);
    }

    @EventTarget
    private void updateHitBonus(ClientTickEvent e) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        double health = player.getHealth();
        double prevHealth = this.prevHealth;

        this.prevHealth = health;
        if (prevHealth <= health) return;

        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context == null) return;

        if (GameRole.fromEntity(player) != GameRole.ZOMBIE) return;

        context.setShotCount(context.getShotCount() + 1);
        context.setDamage(context.getDamage() + prevHealth - health);
    }

    @EventTarget
    private void updateReward(ChatEvent e) {
        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context == null) return;

        String text = e.getText().getString();
        boolean end = false;

        Matcher matcher = REWARD_PATTERN.matcher(text);
        if (matcher.find()) {
            context.setReward(context.getReward() + Integer.parseInt(matcher.group(1)));
        }

        matcher = DEFENSE_BONUS_PATTERN.matcher(text);
        if (matcher.find()) {
            context.setDefenseBonus(context.getDefenseBonus() + Integer.parseInt(matcher.group(1)));
        }

        matcher = HIT_BONUS_PATTERN.matcher(text);
        if (matcher.find()) {
            context.setHitBonus(context.getHitBonus() + Integer.parseInt(matcher.group(1)));
        }

        matcher = EXP_PATTERN.matcher(text);
        if (matcher.find()) {
            context.setExp(context.getExp() + Integer.parseInt(matcher.group(1)));
            end = true;
        }

        if (end) {
            context.setEndedAt(System.currentTimeMillis());
            context.setEnded(true);

            GameContext last = KzePlus.getInstance().getGameContextRegistry().getLastGameContext();
            if (last != null) {
                calcRewardPerHit(context, last);
            }

            KzePlus.getInstance().getGameContextRegistry().endGameContext();
        }
    }
}
