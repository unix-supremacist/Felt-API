package net.feltmc.feltapi.api.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FeltDiggerItem extends DiggerItem {
    boolean torchingEnabled;
    public FeltDiggerItem(float attackDamageModifier, float attackSpeedModifier, Tier tier, TagKey<Block> blocks, Properties properties, boolean torchingEnabled) {
        super(attackDamageModifier, attackSpeedModifier, tier, blocks, properties);
        this.torchingEnabled = torchingEnabled;
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        FeltToolHelper.destroyAreaFromFacing(level, entity, pos, 1, 0);
        return super.mineBlock(itemStack, level, state, pos, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (torchingEnabled){
            return FeltToolHelper.torch(context);
        } else {
            return InteractionResult.PASS;
        }
    }
}
