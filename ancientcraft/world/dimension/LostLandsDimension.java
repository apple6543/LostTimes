package net.mcreator.ancientcraft.world.dimension;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.ancientcraft.procedures.LostLandsPlayerLeavesDimensionProcedure;

@EventBusSubscriber
public class LostLandsDimension {
	@SubscribeEvent
	public static void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
		Entity entity = event.getEntity();
		Level world = entity.level();
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		if (event.getFrom() == ResourceKey.create(Registries.DIMENSION, Identifier.parse("lost_times:lost_lands"))) {
			LostLandsPlayerLeavesDimensionProcedure.execute(entity);
		}
	}
}