package me.Thelnfamous1.flan_improvements.mixin;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ParticleIndicators;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.player.display.ClaimDisplay;
import me.Thelnfamous1.flan_improvements.AdditionalPlayerClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = PlayerClaimData.class, remap = false)
public abstract class PlayerClaimDataMixin implements AdditionalPlayerClaimData {
    @Shadow @Final private ServerPlayer player;

    @Unique
    @Nullable
    private BlockPos editingCorner0;
    @Unique
    private int[] cornerRenderPos0;

    @Unique
    @Override
    public BlockPos editingCorner0() {
        return this.editingCorner0;
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lio/github/flemmli97/flan/player/PlayerClaimData;confirmTick:I", opcode = Opcodes.GETFIELD, ordinal = 0))
    private void handleSecondCornerRenderPos(Claim currentClaim, Consumer<Claim> cons, CallbackInfo ci){
        if (this.cornerRenderPos0 != null) {
            if (this.cornerRenderPos0[1] != this.cornerRenderPos0[2]) {
                this.player.connection.send(new ClientboundLevelParticlesPacket(ParticleIndicators.SETCORNER, true, (double)this.cornerRenderPos0[0] + 0.5, (double)this.cornerRenderPos0[2] + 0.25, (double)this.cornerRenderPos0[3] + 0.5, 0.0F, 0.25F, 0.0F, 0.0F, 2));
            }

            this.player.connection.send(new ClientboundLevelParticlesPacket(ParticleIndicators.SETCORNER, true, (double)this.cornerRenderPos0[0] + 0.5, (double)this.cornerRenderPos0[1] + 0.25, (double)this.cornerRenderPos0[3] + 0.5, 0.0F, 0.25F, 0.0F, 0.0F, 2));
        }
    }

    @Inject(method = "setEditingCorner", at = @At("TAIL"))
    private void handleSetEditingCorner(BlockPos pos, CallbackInfo ci){
        this.setEditingCorner0(null);
    }

    @Unique
    @Override
    public void setEditingCorner0(@Nullable BlockPos pos) {
        if (pos != null) {
            for(BlockState state = this.player.level.getBlockState(pos); state.isAir() || state.getMaterial().isReplaceable(); state = this.player.level.getBlockState(pos)) {
                pos = pos.below();
            }

            this.cornerRenderPos0 = ClaimDisplay.getPosFrom(this.player.getLevel(), pos.getX(), pos.getZ(), pos.getY());
        } else {
            this.cornerRenderPos0 = null;
        }

        this.editingCorner0 = pos;
    }
}
