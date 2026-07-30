/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ancientcraft.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcreator.ancientcraft.item.*;
import net.mcreator.ancientcraft.LostTimesMod;

import java.util.function.Function;

public class LostTimesModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(LostTimesMod.MODID);
	public static final DeferredItem<Item> HEROBRINE_SPAWN_EGG;
	public static final DeferredItem<Item> HEROBRINE_SHRINE;
	public static final DeferredItem<Item> LOST_GRASS;
	public static final DeferredItem<Item> LOST_LANDS;
	public static final DeferredItem<Item> HEROBRINESWORD;
	public static final DeferredItem<Item> HEROBRINE_SOUL;
	public static final DeferredItem<Item> RUBY_ORE;
	public static final DeferredItem<Item> RUBY;
	public static final DeferredItem<Item> RUBY_SWORD;
	public static final DeferredItem<Item> RUBY_PICK;
	public static final DeferredItem<Item> RUBY_AXE;
	public static final DeferredItem<Item> RUBY_SHOVEL;
	public static final DeferredItem<Item> RUBY_HOE;
	public static final DeferredItem<Item> DEEPSLATE_RUBY_ORE;
	public static final DeferredItem<Item> BLOCK_OF_RUBY;
	static {
		HEROBRINE_SPAWN_EGG = register("herobrine_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(LostTimesModEntities.HEROBRINE.get())));
		HEROBRINE_SHRINE = register("herobrine_shrine", HerobrineShrineItem::new);
		LOST_GRASS = block(LostTimesModBlocks.LOST_GRASS);
		LOST_LANDS = register("lost_lands", LostLandsItem::new);
		HEROBRINESWORD = register("herobrinesword", HerobrineswordItem::new);
		HEROBRINE_SOUL = register("herobrine_soul", HerobrineSoulItem::new);
		RUBY_ORE = block(LostTimesModBlocks.RUBY_ORE, new Item.Properties().rarity(Rarity.RARE));
		RUBY = register("ruby", RubyItem::new);
		RUBY_SWORD = register("ruby_sword", RubySwordItem::new);
		RUBY_PICK = register("ruby_pick", RubyPickItem::new);
		RUBY_AXE = register("ruby_axe", RubyAxeItem::new);
		RUBY_SHOVEL = register("ruby_shovel", RubyShovelItem::new);
		RUBY_HOE = register("ruby_hoe", RubyHoeItem::new);
		DEEPSLATE_RUBY_ORE = block(LostTimesModBlocks.DEEPSLATE_RUBY_ORE, new Item.Properties().rarity(Rarity.UNCOMMON));
		BLOCK_OF_RUBY = block(LostTimesModBlocks.BLOCK_OF_RUBY, new Item.Properties().rarity(Rarity.UNCOMMON));
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, ? extends I> supplier) {
		return REGISTRY.registerItem(name, supplier, Item.Properties::new);
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.registerItem(block.getId().getPath(), prop -> new BlockItem(block.get(), prop), () -> properties);
	}
}