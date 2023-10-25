package net.feltmc.feltapi.api.tool;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;

public class FeltLumberaxe extends FeltDiggerItem{
    public FeltLumberaxe(float attackDamageModifier, float attackSpeedModifier, Tier tier, TagKey<Block> blocks, Properties properties, boolean torchingEnabled) {
        super(attackDamageModifier, attackSpeedModifier, tier, blocks, properties, torchingEnabled);
    }
}
