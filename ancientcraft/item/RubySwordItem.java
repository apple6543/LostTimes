package net.mcreator.ancientcraft.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class RubySwordItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 999, 1f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("lost_times:ruby_sword_repair_items")));

	public RubySwordItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 12f, -1f).rarity(Rarity.UNCOMMON));
	}
}