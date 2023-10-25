package net.feltmc.feltapi.api.tool;

import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public class FeltPlowItem extends FeltDiggerItem{
    public FeltPlowItem(float attackDamageModifier, float attackSpeedModifier, Tier tier, TagKey<Block> blocks, Properties properties, boolean torchingEnabled) {
        super(attackDamageModifier, attackSpeedModifier, tier, blocks, properties, torchingEnabled);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return FeltToolHelper.till(context);
    }
}
