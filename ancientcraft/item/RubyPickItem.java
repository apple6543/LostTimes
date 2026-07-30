package net.mcreator.ancientcraft.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class RubyPickItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 999, 27f, 0, 5, TagKey.create(Registries.ITEM, Identifier.parse("lost_times:ruby_pick_repair_items")));

	public RubyPickItem(Item.Properties properties) {
		super(properties.pickaxe(TOOL_MATERIAL, 3f, -3f).rarity(Rarity.UNCOMMON));
	}
}