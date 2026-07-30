package net.mcreator.ancientcraft.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class LostGrassBlock extends Block {
	public LostGrassBlock(BlockBehaviour.Properties properties) {
		super(properties.sound(SoundType.GRAVEL).strength(0.6f, 0.5f));
	}
}