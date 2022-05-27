package io.github.feltmc.feltapi.mixin.tool.common;

import io.github.feltmc.feltapi.api.tool.extensions.EnchantmentExtension;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    public void felt_injectEnchantmentCheck(ItemStack stack, CallbackInfoReturnable<Boolean> callback){
        if (stack.getItem() instanceof EnchantmentExtension extension){
            callback.setReturnValue(extension.canApplyAtEnchantingTable(stack, (Enchantment) (Object)this));
        }
    }
}
