package net.feltmc.feltapi.api.tool;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;

public class FeltAxeItem extends AxeItem {
    boolean torchingEnabled;
    public FeltAxeItem(Tier material, int attackDamage, float attackSpeed, Properties settings) {
        this(material, attackDamage, attackSpeed, settings, false);
    }

    public FeltAxeItem(Tier material, int attackDamage, float attackSpeed, Properties settings, boolean torching) {
        super(material, attackDamage, attackSpeed, settings);
        torchingEnabled = torching;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player playerEntity = context.getPlayer();
        if(!playerEntity.isShiftKeyDown() && FeltToolHelper.canStrip(context)){
            return FeltToolHelper.strip(context);
        } else if (torchingEnabled && FeltToolHelper.canTorch(context)){
            return FeltToolHelper.torch(context);
        }
        return InteractionResult.PASS;
    }
}