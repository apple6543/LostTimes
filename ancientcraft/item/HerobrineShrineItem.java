package net.mcreator.ancientcraft.item;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;

import net.mcreator.ancientcraft.procedures.HerobrineShrineRightclickedOnBlockProcedure;

public class HerobrineShrineItem extends Item {
	public HerobrineShrineItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
	}

	@Override
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		super.useOn(context);
		HerobrineShrineRightclickedOnBlockProcedure.execute(context.getLevel(), context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ(), context.getPlayer());
		return InteractionResult.SUCCESS;
	}
}