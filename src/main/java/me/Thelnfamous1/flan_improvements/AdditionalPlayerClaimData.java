package me.Thelnfamous1.flan_improvements;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface AdditionalPlayerClaimData {

    @Nullable
    BlockPos editingCorner0();

    void setEditingCorner0(@Nullable BlockPos pos);
}
