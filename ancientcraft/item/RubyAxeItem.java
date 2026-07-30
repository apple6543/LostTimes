package net.mcreator.ancientcraft.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.AxeItem;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class RubyAxeItem extends AxeItem {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 999, 17f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("lost_times:ruby_axe_repair_items")));

	public RubyAxeItem(Item.Properties properties) {
		super(TOOL_MATERIAL, 3f, -3f, properties.rarity(Rarity.UNCOMMON));
	}
}