package net.mcreator.ancientcraft.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementHolder;

import net.mcreator.ancientcraft.init.LostTimesModItems;
import net.mcreator.ancientcraft.entity.HerobrineEntity;

public class OneshotProcedure {
	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (entity instanceof HerobrineEntity) {
			{
				Entity _ent = entity;
				if (_ent.level() instanceof ServerLevel _serverLevel) {
					_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.PLAYER_ATTACK)), 9999999);
				}
			}
			if (!entity.isAlive()) {
				if (sourceentity instanceof ServerPlayer _player && _player.level() instanceof ServerLevel _level) {
					AdvancementHolder _adv = _level.getServer().getAdvancements().get(Identifier.parse("lost_times:kill_herobrine"));
					if (_adv != null) {
						AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
						if (!_ap.isDone()) {
							for (String criteria : _ap.getRemainingCriteria())
								_player.getAdvancements().award(_adv, criteria);
						}
					}
				}
				if (sourceentity instanceof Player _player) {
					ItemStack _setstack = new ItemStack(LostTimesModItems.HEROBRINE_SOUL.get()).copy();
					_setstack.setCount(1);
					_player.getInventory().placeItemBackInInventory(_setstack);
				}
			}
		}
	}
}