package me.Thelnfamous1.flan_improvements.mixin;

import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.player.PlayerClaimData;
import me.Thelnfamous1.flan_improvements.AdditionalPlayerClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemInteractEvents.class, remap = false)
public class ItemInteractEventsMixin {

    @Inject(method = "claimLandHandling",
            at = @At(value = "INVOKE",
                    target = "Lio/github/flemmli97/flan/claim/ClaimStorage;createClaim(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/server/level/ServerPlayer;)Z"), cancellable = true)
    private static void delayClaimCreationForCommand(ServerPlayer player, BlockPos target, CallbackInfo ci){
        ci.cancel();
        PlayerClaimData data = PlayerClaimData.get(player);
        ((AdditionalPlayerClaimData)data).setEditingCorner0(target);
    }

}
