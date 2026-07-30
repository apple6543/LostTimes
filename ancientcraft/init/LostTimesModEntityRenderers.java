/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ancientcraft.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.ancientcraft.client.renderer.HerobrineRenderer;

@EventBusSubscriber(Dist.CLIENT)
public class LostTimesModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(LostTimesModEntities.HEROBRINE.get(), HerobrineRenderer::new);
	}
}