package net.mcreator.ancientcraft.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.Item;

public class HerobrineSoulItem extends Item {
	public HerobrineSoulItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
	}

	@Override
	public ItemStackTemplate getCraftingRemainder(ItemInstance itemInstance) {
		return new ItemStackTemplate(this);
	}

	@Override
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}
}