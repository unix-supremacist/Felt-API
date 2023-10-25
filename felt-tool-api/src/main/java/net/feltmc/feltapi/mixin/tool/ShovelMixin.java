package net.feltmc.feltapi.mixin.tool;

import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ShovelItem.class)
public interface ShovelMixin {
    @Accessor("FLATTENABLES") static Map<Block, BlockState> getPathed() { throw new AssertionError(); }
}
