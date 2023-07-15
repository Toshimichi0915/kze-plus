package net.toshimichi.kzeplus.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.toshimichi.kzeplus.options.VisibilityMode;
import net.toshimichi.kzeplus.utils.KzeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    public void setVisibility(CallbackInfoReturnable<Boolean> cir) {
        if (!KzeUtils.shouldHide((Entity) (Object) this)) return;

        VisibilityMode mode = KzeUtils.getVisibilityMode();
        if (mode == VisibilityMode.FULL) return;

        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    public void setInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!KzeUtils.shouldHide((Entity) (Object) this)) return;

        VisibilityMode mode = KzeUtils.getVisibilityMode();
        if (mode == VisibilityMode.FULL) return;
        cir.setReturnValue(false);
        cir.cancel();
    }
}
