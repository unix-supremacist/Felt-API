package net.feltmc.feltapi.mixin.tool;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AxeItem.class)
public interface AxeMixin {
    @Accessor("STRIPPABLES") static Map<Block, Block> getStripped() { throw new AssertionError(); }
}