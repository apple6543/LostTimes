/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ancientcraft.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.mcreator.ancientcraft.LostTimesMod;

@EventBusSubscriber
public class LostTimesModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LostTimesMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LOST_TAB = REGISTRY.register("lost_tab",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.lost_times.lost_tab")).icon(() -> new ItemStack(LostTimesModBlocks.LOST_GRASS.get())).displayItems((parameters, tabData) -> {
				tabData.accept(LostTimesModItems.HEROBRINE_SHRINE.get());
				tabData.accept(LostTimesModBlocks.LOST_GRASS.get().asItem());
				tabData.accept(LostTimesModItems.LOST_LANDS.get());
				tabData.accept(LostTimesModItems.HEROBRINESWORD.get());
				tabData.accept(LostTimesModItems.HEROBRINE_SOUL.get());
				tabData.accept(LostTimesModBlocks.RUBY_ORE.get().asItem());
				tabData.accept(LostTimesModItems.RUBY.get());
				tabData.accept(LostTimesModItems.RUBY_SWORD.get());
				tabData.accept(LostTimesModItems.RUBY_PICK.get());
				tabData.accept(LostTimesModItems.RUBY_AXE.get());
				tabData.accept(LostTimesModItems.RUBY_SHOVEL.get());
				tabData.accept(LostTimesModItems.RUBY_HOE.get());
				tabData.accept(LostTimesModBlocks.DEEPSLATE_RUBY_ORE.get().asItem());
			}).withSearchBar().build());

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(LostTimesModItems.HEROBRINE_SPAWN_EGG.get());
		}
	}
}