package net.mcreator.ancientcraft.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class RubyShovelItem extends ShovelItem {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 999, 25f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("lost_times:ruby_shovel_repair_items")));

	public RubyShovelItem(Item.Properties properties) {
		super(TOOL_MATERIAL, 3f, -3f, properties.rarity(Rarity.UNCOMMON));
	}
}