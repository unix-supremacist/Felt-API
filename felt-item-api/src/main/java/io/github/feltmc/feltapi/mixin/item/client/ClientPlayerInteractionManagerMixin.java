package io.github.feltmc.feltapi.mixin.item.client;

import io.github.feltmc.feltapi.api.item.extensions.DamageableItemExtension;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Debug(export = true)
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    //@ModifyVariable(method = "interactBlock", at = @At(value = "STORE"), ordinal = 1, print = true)
    private boolean modifyBL2(boolean bl2, ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult){
        return true;
        /*BlockPos blockPos = hitResult.getBlockPos();
        ItemStack mainStack = player.getMainHandStack();
        ItemStack offHandStack = player.getOffHandStack();
        if (mainStack.getItem() instanceof DamageableItemExtension extension){
            return bl2 && !extension.doesSneakBypassUse(mainStack, world, blockPos, player);
        } else if (offHandStack.getItem() instanceof DamageableItemExtension extension){
            return bl2 && !extension.doesSneakBypassUse(offHandStack, world, blockPos, player);
        }
        return bl2;*/
    }

    /*@Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectInteractBlock(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir, BlockPos blockPos, ItemStack itemStack, boolean bl){
       *//* ItemStack mainStack = player.getMainHandStack();
        ItemStack offHandStack = player.getOffHandStack();
        if (mainStack.getItem() instanceof DamageableItemExtension extension){
            bl2 = bl2 && !extension.doesSneakBypassUse(mainStack, world, blockPos, player);
        } else if (offHandStack.getItem() instanceof DamageableItemExtension extension){
            bl2 = bl2 && !extension.doesSneakBypassUse(offHandStack, world, blockPos, player);
        }*//*
    }*/
}
