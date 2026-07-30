/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ancientcraft.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;

import net.mcreator.ancientcraft.block.RubyOreBlock;
import net.mcreator.ancientcraft.block.LostLandsPortalBlock;
import net.mcreator.ancientcraft.block.LostGrassBlock;
import net.mcreator.ancientcraft.block.DeepslateRubyOreBlock;
import net.mcreator.ancientcraft.block.BlockOfRubyBlock;
import net.mcreator.ancientcraft.LostTimesMod;

import java.util.function.Function;

public class LostTimesModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(LostTimesMod.MODID);
	public static final DeferredBlock<Block> LOST_GRASS;
	public static final DeferredBlock<Block> LOST_LANDS_PORTAL;
	public static final DeferredBlock<Block> RUBY_ORE;
	public static final DeferredBlock<Block> DEEPSLATE_RUBY_ORE;
	public static final DeferredBlock<Block> BLOCK_OF_RUBY;
	static {
		LOST_GRASS = register("lost_grass", LostGrassBlock::new);
		LOST_LANDS_PORTAL = register("lost_lands_portal", LostLandsPortalBlock::new);
		RUBY_ORE = register("ruby_ore", RubyOreBlock::new);
		DEEPSLATE_RUBY_ORE = register("deepslate_ruby_ore", DeepslateRubyOreBlock::new);
		BLOCK_OF_RUBY = register("block_of_ruby", BlockOfRubyBlock::new);
	}

	// Start of user code block custom blocks
	// End of user code block custom blocks
	private static <B extends Block> DeferredBlock<B> register(String name, Function<BlockBehaviour.Properties, ? extends B> supplier) {
		return REGISTRY.registerBlock(name, supplier);
	}
}