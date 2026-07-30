package net.mcreator.ancientcraft.entity;

import java.util.List;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.common.NeoForgeMod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

import net.mcreator.ancientcraft.procedures.HerobrineOnEntityTickUpdateProcedure;

public class HerobrineEntity extends PathfinderMob {
	private static final int LIGHTNING_MIN_INTERVAL = 1200;
	private static final int LIGHTNING_INTERVAL_RANGE = 600;
	private static final int VANISH_MIN_DURATION = 40;
	private static final int VANISH_DURATION_RANGE = 40;
	private static final int STALKING_PHASE = 0;
	private static final int COMBAT_PHASE = 1;
	private static final int DEFENSIVE_PHASE = 2;
	private static final int WATCHING_PHASE = 3;
	private static final int ANIM_NONE = 0;
	private static final int ANIM_SWING = 1;
	private static final int ANIM_DASH = 2;
	private static final int ANIM_JUMP = 3;
	private static final int ANIM_SLAM = 4;
	private static final int ANIM_VANISH = 5;
	private static final int ANIM_ROAR = 6;
	private static final int ANIM_TELEPORT = 7;
	private static final int ANIM_WARP = 8;

	private int actionCooldown;
	private int worldControlCooldown;
	private int lightningCooldown;
	private int throwCooldown;
	private int jumpCooldown;
	private int vanishCooldown;
	private int vanishDuration;
	private int teleportCooldown;
	private int whisperCooldown;
	private int auraCooldown;
	private int echoCooldown;
	private int slamCooldown;
	private int panicCooldown;
	private int phaseTick;
	private int lastSeenTick;
	private int consecutiveMisses;
	private boolean vanished;
	private boolean phaseLocked;
	private boolean roaring;
	private int aggressionLevel;
	private Vec3 anchorPosition;
	private int anchorCooldown;
	private int animationTimer;
	private int animationDuration;
	private int activeAnimation;
	private int watchCooldown;
	private int totemCooldown;
	private int messageCooldown;
	private int glitchCooldown;
	private int fakeSightingCooldown;
	private int distortionCooldown;
	private boolean echoClone;
	private int echoCloneLifetime;
	private int blocksBrokenByPlayer;
	private int daysWatched;
	private Vec3 lastPlayerLocation;
	private BlockPos lastObservedPlayerBlock;

	public HerobrineEntity(EntityType<HerobrineEntity> type, Level world) {
		super(type, world);
		xpReward = 99;
		setNoAi(false);
		setPersistenceRequired();
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.MACE));
		this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
		this.setPathfindingMalus(PathType.WATER, 0);
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.42D);
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1_000_000.0D);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7.0D);
		this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(64.0D);
		this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(2.0D);
		this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6D);
		this.setHealth(this.getMaxHealth());
		this.setNoGravity(true);
		this.moveControl = new MoveControl(this) {
			@Override
			public void tick() {
				// STOP ALL MOVEMENT & CONTROL IF DEAD
				if (!HerobrineEntity.this.isAlive()) {
					HerobrineEntity.this.setSpeed(0);
					return;
				}
				if (HerobrineEntity.this.isInWater()) {
					HerobrineEntity.this.setDeltaMovement(HerobrineEntity.this.getDeltaMovement().add(0, 0.005, 0));
				}
				if (this.operation == MoveControl.Operation.MOVE_TO && !HerobrineEntity.this.getNavigation().isDone()) {
					double dx = this.wantedX - HerobrineEntity.this.getX();
					double dy = this.wantedY - HerobrineEntity.this.getY();
					double dz = this.wantedZ - HerobrineEntity.this.getZ();
					float f = (float) (Mth.atan2(dz, dx) * (double) (180 / Math.PI)) - 90;
					float f1 = (float) (this.speedModifier * HerobrineEntity.this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
					HerobrineEntity.this.setYRot(this.rotlerp(HerobrineEntity.this.getYRot(), f, 10));
					HerobrineEntity.this.yBodyRot = HerobrineEntity.this.getYRot();
					HerobrineEntity.this.yHeadRot = HerobrineEntity.this.getYRot();
					if (HerobrineEntity.this.isInWater()) {
						HerobrineEntity.this.setSpeed((float) HerobrineEntity.this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
						float f2 = -(float) (Mth.atan2(dy, (float) Math.sqrt(dx * dx + dz * dz)) * (180 / Math.PI));
						f2 = Mth.clamp(Mth.wrapDegrees(f2), -85, 85);
						HerobrineEntity.this.setXRot(this.rotlerp(HerobrineEntity.this.getXRot(), f2, 5));
						float f3 = Mth.cos(HerobrineEntity.this.getXRot() * (float) (Math.PI / 180.0));
						HerobrineEntity.this.setZza(f3 * f1);
						HerobrineEntity.this.setYya((float) (f1 * dy));
					} else {
						HerobrineEntity.this.setSpeed(f1 * 0.55F);
					}
				} else {
					HerobrineEntity.this.setSpeed(0);
					HerobrineEntity.this.setYya(0);
					HerobrineEntity.this.setZza(0);
				}
			}
		};
	}

	@Override
	protected PathNavigation createNavigation(Level world) {
		return new WaterBoundPathNavigation(this, world);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
		this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.95D));
		this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource source, boolean recentlyHitIn) {
    // No custom drops
}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.generic.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.generic.death"));
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource damagesource, float amount) {
		if (damagesource.is(DamageTypes.IN_FIRE))
			return false;
		if (damagesource.getDirectEntity() instanceof AbstractThrownPotion || damagesource.getDirectEntity() instanceof AreaEffectCloud || damagesource.typeHolder().is(NeoForgeMod.POISON_DAMAGE))
			return false;
		if (damagesource.is(DamageTypes.FALL))
			return false;
		if (damagesource.is(DamageTypes.CACTUS))
			return false;
		if (damagesource.is(DamageTypes.DROWN))
			return false;
		if (damagesource.is(DamageTypes.LIGHTNING_BOLT))
			return false;
		if (damagesource.is(DamageTypes.EXPLOSION) || damagesource.is(DamageTypes.PLAYER_EXPLOSION))
			return false;
		if (damagesource.is(DamageTypes.FALLING_ANVIL))
			return false;
		if (damagesource.is(DamageTypes.DRAGON_BREATH))
			return false;
		if (damagesource.is(DamageTypes.WITHER) || damagesource.is(DamageTypes.WITHER_SKULL))
			return false;
		return super.hurtServer(level, damagesource, amount);
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}

	@Override
	public void baseTick() {
		super.baseTick();

		// IF DEAD, STOP EXECUTING AI / TRACKING
		if (!this.isAlive()) {
			this.getNavigation().stop();
			this.setTarget(null);
			return;
		}

		if (this.echoClone) {
			this.echoCloneLifetime++;
			if (this.echoCloneLifetime >= 40) {
				this.discard();
				return;
			}
			this.setDeltaMovement(0.0D, 0.0D, 0.0D);
			this.getNavigation().stop();
			this.setNoAi(true);
			this.setInvulnerable(true);
			return;
		}
		HerobrineOnEntityTickUpdateProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
		if (this.level().isClientSide()) {
			return;
		}

		this.decrementCooldowns();
		this.updateVisualAnimation();
		Player target = this.level().getNearestPlayer(this, 64.0D);
		if (target == null) {
			this.setTarget(null);
			this.getNavigation().stop();
			this.resetAmbientState();
			return;
		}

		this.setTarget(target);
		this.lookAt(target, 30.0F, 30.0F);
		double distanceSq = this.distanceToSqr(target);
		this.updateTracking(target, distanceSq);
		this.updateMemory(target, distanceSq);

		if (this.vanished) {
			this.setDeltaMovement(this.getDeltaMovement().x * 0.92D, 0.04D, this.getDeltaMovement().z * 0.92D);
			this.getNavigation().stop();
			this.spawnWhisperTrail();
			return;
		}

		int phase = this.determinePhase(target, distanceSq);
		this.phaseTick++;

		if (phase == STALKING_PHASE) {
			this.runStalkingBehavior(target, distanceSq);
		} else if (phase == COMBAT_PHASE) {
			this.runCombatBehavior(target, distanceSq);
		} else if (phase == WATCHING_PHASE) {
			this.runObservationBehavior(target, distanceSq);
		} else {
			this.runDefensiveBehavior(target, distanceSq);
		}
	}

	private void decrementCooldowns() {
		if (this.actionCooldown > 0) {
			this.actionCooldown--;
		}
		if (this.worldControlCooldown > 0) {
			this.worldControlCooldown--;
		}
		if (this.lightningCooldown > 0) {
			this.lightningCooldown--;
		}
		if (this.throwCooldown > 0) {
			this.throwCooldown--;
		}
		if (this.jumpCooldown > 0) {
			this.jumpCooldown--;
		}
		if (this.vanishDuration > 0) {
			this.vanishDuration--;
			if (this.vanishDuration <= 0) {
				this.vanished = false;
				this.setInvisible(false);
			}
		}
		if (this.vanishCooldown > 0) {
			this.vanishCooldown--;
		}
		if (this.teleportCooldown > 0) {
			this.teleportCooldown--;
		}
		if (this.whisperCooldown > 0) {
			this.whisperCooldown--;
		}
		if (this.auraCooldown > 0) {
			this.auraCooldown--;
		}
		if (this.echoCooldown > 0) {
			this.echoCooldown--;
		}
		if (this.slamCooldown > 0) {
			this.slamCooldown--;
		}
		if (this.panicCooldown > 0) {
			this.panicCooldown--;
		}
		if (this.anchorCooldown > 0) {
			this.anchorCooldown--;
		}
		if (this.watchCooldown > 0) {
			this.watchCooldown--;
		}
		if (this.totemCooldown > 0) {
			this.totemCooldown--;
		}
		if (this.messageCooldown > 0) {
			this.messageCooldown--;
		}
		if (this.glitchCooldown > 0) {
			this.glitchCooldown--;
		}
		if (this.fakeSightingCooldown > 0) {
			this.fakeSightingCooldown--;
		}
		if (this.distortionCooldown > 0) {
			this.distortionCooldown--;
		}
		if (this.animationTimer > 0) {
			this.animationTimer--;
			if (this.animationTimer <= 0) {
				this.activeAnimation = ANIM_NONE;
			}
		}
	}

	private void resetAmbientState() {
		this.phaseTick = 0;
		this.consecutiveMisses = 0;
		this.phaseLocked = false;
		this.roaring = false;
		this.aggressionLevel = 0;
		this.anchorPosition = null;
		this.watchCooldown = 0;
		this.totemCooldown = 0;
		this.messageCooldown = 0;
		this.glitchCooldown = 0;
		this.fakeSightingCooldown = 0;
		this.distortionCooldown = 0;
		this.blocksBrokenByPlayer = 0;
		this.daysWatched = 0;
		this.lastPlayerLocation = null;
		this.lastObservedPlayerBlock = null;
	}

	private void updateTracking(Player target, double distanceSq) {
		if (this.isActivelyEngaged(target, distanceSq)) {
			this.lastSeenTick = 0;
			this.consecutiveMisses = 0;
			this.aggressionLevel = Math.min(4, this.aggressionLevel + 1);
		} else {
			this.lastSeenTick++;
			this.consecutiveMisses = Math.min(10, this.consecutiveMisses + 1);
			if (this.consecutiveMisses >= 3) {
				this.aggressionLevel = Math.max(0, this.aggressionLevel - 1);
			}
		}
	}

	private void updateMemory(Player target, double distanceSq) {
		if (this.lastPlayerLocation == null) {
			this.lastPlayerLocation = target.position();
		} else {
			if (target.position().distanceToSqr(this.lastPlayerLocation) > 12.0D * 12.0D) {
				this.daysWatched = Math.min(30, this.daysWatched + 1);
				this.lastPlayerLocation = target.position();
			}
			if (this.lastObservedPlayerBlock != null) {
				BlockPos currentBlock = target.blockPosition();
				if (!this.level().getBlockState(currentBlock).isAir() && this.level().getBlockState(this.lastObservedPlayerBlock).isAir()) {
					this.blocksBrokenByPlayer = Math.min(80, this.blocksBrokenByPlayer + 1);
				}
			}
		}
		this.lastObservedPlayerBlock = target.blockPosition();
		if (this.daysWatched >= 2 && this.lastPlayerLocation != null && target.position().distanceToSqr(this.lastPlayerLocation) < 10.0D * 10.0D && this.getRandom().nextFloat() < 0.02F) {
			this.performObservationWatch(target);
		}
	}

	private int determinePhase(Player target, double distanceSq) {
		if (this.phaseLocked) {
			return this.aggressionLevel >= 3 ? COMBAT_PHASE : DEFENSIVE_PHASE;
		}
		if (distanceSq <= 9.0D * 9.0D || this.actionCooldown > 0 || this.getLastHurtByMob() == target) {
			this.phaseLocked = true;
			return COMBAT_PHASE;
		}
		if (!this.isActivelyEngaged(target, distanceSq) && this.consecutiveMisses > 6 && distanceSq > 16.0D * 16.0D) {
			return WATCHING_PHASE;
		}
		if (this.consecutiveMisses > 4 || this.vanishCooldown <= 0 && this.shouldVanish(target, distanceSq)) {
			return STALKING_PHASE;
		}
		return this.phaseTick % 3 == 0 ? DEFENSIVE_PHASE : STALKING_PHASE;
	}

	private void runObservationBehavior(Player target, double distanceSq) {
		if (distanceSq <= 12.0D * 12.0D && this.getRandom().nextFloat() < 0.75F) {
			this.beginVanishing();
			return;
		}
		if (this.watchCooldown <= 0) {
			this.performObservationWatch(target);
			this.watchCooldown = 70 + this.getRandom().nextInt(40);
		}
		if (this.fakeSightingCooldown <= 0 && this.blocksBrokenByPlayer >= 4 && this.getRandom().nextFloat() < 0.35F) {
			this.performFakeSighting(target);
			this.fakeSightingCooldown = 140 + this.getRandom().nextInt(80);
		}
		if (this.distortionCooldown <= 0 && distanceSq <= 30.0D * 30.0D && this.getRandom().nextFloat() < 0.5F) {
			this.performDarknessDistortion(target);
			this.distortionCooldown = 100 + this.getRandom().nextInt(80);
		}
		if (this.getRandom().nextFloat() < 0.16F) {
			this.performWorldControl(target);
		}
		if (this.getRandom().nextFloat() < 0.1F) {
			this.performWhisperEffect();
		}
		this.getNavigation().stop();
		this.lookAt(target, 30.0F, 30.0F);
		this.setDeltaMovement(this.getDeltaMovement().x * 0.92D, 0.0D, this.getDeltaMovement().z * 0.92D);
	}

	private void runStalkingBehavior(Player target, double distanceSq) {
		if (this.vanishCooldown <= 0 && this.shouldVanish(target, distanceSq) && !this.isActivelyEngaged(target, distanceSq)) {
			this.beginVanishing();
			return;
		}
		if (this.teleportCooldown <= 0 && this.getRandom().nextFloat() < 0.16F) {
			this.performTeleport(target);
			this.teleportCooldown = 90 + this.getRandom().nextInt(70);
		}
		if (this.whisperCooldown <= 0 && this.getRandom().nextFloat() < 0.14F) {
			this.performWhisperEffect();
			this.whisperCooldown = 100 + this.getRandom().nextInt(70);
		}
		if (this.auraCooldown <= 0 && this.getRandom().nextFloat() < 0.1F) {
			this.performAmbientAura(target);
			this.auraCooldown = 140 + this.getRandom().nextInt(80);
		}
		if (this.messageCooldown <= 0 && this.getRandom().nextFloat() < 0.08F) {
			this.performMessageSign(target);
			this.messageCooldown = 140 + this.getRandom().nextInt(80);
		}
		if (this.glitchCooldown <= 0 && this.getRandom().nextFloat() < 0.06F) {
			this.performRealityGlitch(target);
			this.glitchCooldown = 180 + this.getRandom().nextInt(100);
		}
		if (distanceSq <= 18.0D * 18.0D && this.getRandom().nextFloat() < 0.22F) {
			this.performShadowDash(target);
		}
		if (this.shouldUseCover(distanceSq) && this.tryHideBehindCover(target)) {
			return;
		}
		if (distanceSq > 36.0D * 36.0D) {
			this.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.05D);
		} else {
			this.getNavigation().moveTo(target, 1.0D);
		}
		this.setDeltaMovement(this.getDeltaMovement().x * 0.96D, 0.025D, this.getDeltaMovement().z * 0.96D);
	}

	private void runCombatBehavior(Player target, double distanceSq) {
		this.roaring = true;
		if (this.getHealth() <= this.getMaxHealth() * 0.3F && this.totemCooldown <= 0 && this.getRandom().nextFloat() < 0.08F) {
			this.performTotemRevival();
		}
		if (distanceSq <= 8.0D * 8.0D && this.actionCooldown <= 0) {
			this.triggerSwingAnimation(8);
			if (this.level() instanceof ServerLevel serverLevel) {
				this.doHurtTarget(serverLevel, target);
			}
			this.actionCooldown = 20 + this.getRandom().nextInt(12);
		}
		if (distanceSq <= 18.0D * 18.0D && this.jumpCooldown <= 0 && (distanceSq > 9.0D * 9.0D || this.getRandom().nextFloat() < 0.35F)) {
			this.performJumpAttack(target);
			this.jumpCooldown = 30 + this.getRandom().nextInt(20);
		}
		if (distanceSq <= 25.0D * 25.0D && this.lightningCooldown <= 0 && this.getRandom().nextFloat() < 0.3F) {
			this.performLightningStrike(target);
			this.lightningCooldown = LIGHTNING_MIN_INTERVAL + this.getRandom().nextInt(LIGHTNING_INTERVAL_RANGE);
		}
		if (distanceSq <= 10.0D * 10.0D && this.throwCooldown <= 0 && this.getRandom().nextFloat() < 0.22F) {
			this.throwNearbyMobs(target);
			this.throwCooldown = 32 + this.getRandom().nextInt(18);
		}
		if (this.echoCooldown <= 0 && this.getRandom().nextFloat() < 0.16F) {
			this.performEchoSpawn(target);
			this.echoCooldown = 50 + this.getRandom().nextInt(30);
		}
		if (this.slamCooldown <= 0 && this.getRandom().nextFloat() < 0.18F) {
			this.performGroundSlam(target);
			this.slamCooldown = 44 + this.getRandom().nextInt(24);
		}
		if (this.panicCooldown <= 0 && this.getRandom().nextFloat() < 0.1F) {
			this.performFearBurst(target);
			this.panicCooldown = 80 + this.getRandom().nextInt(40);
		}
		if (distanceSq <= 18.0D * 18.0D && this.getRandom().nextFloat() < 0.25F) {
			this.performShadowDash(target);
		}
		if (this.shouldUseCover(distanceSq) && this.tryHideBehindCover(target)) {
			return;
		}
		if (distanceSq > 28.0D * 28.0D) {
			this.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.2D);
		} else {
			this.getNavigation().moveTo(target, 1.1D);
		}
		this.setDeltaMovement(this.getDeltaMovement().x * 0.97D, 0.02D, this.getDeltaMovement().z * 0.97D);
	}

	private void runDefensiveBehavior(Player target, double distanceSq) {
		if (this.getHealth() <= this.getMaxHealth() * 0.3F && this.totemCooldown <= 0 && this.getRandom().nextFloat() < 0.08F) {
			this.performTotemRevival();
		}
		if (this.anchorCooldown <= 0) {
			this.anchorPosition = target.position();
			this.anchorCooldown = 50 + this.getRandom().nextInt(35);
		}
		if (this.worldControlCooldown <= 0) {
			this.performWorldControl(target);
			this.worldControlCooldown = 24 + this.getRandom().nextInt(18);
		}
		if (this.echoCooldown <= 0 && this.getRandom().nextFloat() < 0.16F) {
			this.performEchoSpawn(target);
			this.echoCooldown = 48 + this.getRandom().nextInt(28);
		}
		if (this.slamCooldown <= 0 && this.getRandom().nextFloat() < 0.12F) {
			this.performGroundSlam(target);
			this.slamCooldown = 54 + this.getRandom().nextInt(28);
		}
		if (this.teleportCooldown <= 0 && this.getRandom().nextFloat() < 0.14F) {
			this.performTeleport(target);
			this.teleportCooldown = 60 + this.getRandom().nextInt(50);
		}
		if (this.messageCooldown <= 0 && this.getRandom().nextFloat() < 0.08F) {
			this.performMessageSign(target);
			this.messageCooldown = 140 + this.getRandom().nextInt(90);
		}
		if (this.glitchCooldown <= 0 && this.getRandom().nextFloat() < 0.06F) {
			this.performRealityGlitch(target);
			this.glitchCooldown = 190 + this.getRandom().nextInt(110);
		}
		if (distanceSq <= 20.0D * 20.0D && this.getRandom().nextFloat() < 0.2F) {
			this.performShadowDash(target);
		}
		if (this.shouldUseCover(distanceSq) && this.tryHideBehindCover(target)) {
			return;
		}
		if (this.anchorPosition != null) {
			this.getNavigation().moveTo(this.anchorPosition.x(), this.anchorPosition.y(), this.anchorPosition.z(), 0.9D);
		} else {
			this.getNavigation().moveTo(target, 0.9D);
		}
		this.setDeltaMovement(this.getDeltaMovement().x * 0.94D, 0.01D, this.getDeltaMovement().z * 0.94D);
	}

	private void updateVisualAnimation() {
		if (this.animationTimer <= 0) {
			return;
		}
		float progress = 1.0F - ((float) this.animationTimer / (float) Math.max(this.animationDuration, 1));
		if (this.level() instanceof ServerLevel serverLevel) {
			switch (this.activeAnimation) {
				case ANIM_SWING -> {
					float swing = (float) Math.sin((1.0F - progress) * Math.PI) * 24.0F;
					this.setYRot(this.getYRot() + swing);
					this.yBodyRot = this.getYRot();
					this.yHeadRot = this.getYRot();
					this.setXRot(this.getXRot() + (progress > 0.5F ? -10.0F : 10.0F));
					serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.0D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
				}
				case ANIM_DASH -> {
					this.setYRot(this.getYRot() + (float) Math.sin(progress * Math.PI * 2.0F) * 18.0F);
					this.setXRot(this.getXRot() - 6.0F + (float) Math.sin(progress * Math.PI) * 8.0F);
					serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.9D, this.getZ(), 2, 0.0D, 0.0D, 0.0D, 0.0D);
				}
				case ANIM_JUMP -> {
					this.setYRot(this.getYRot() + (float) Math.sin(progress * Math.PI) * 14.0F);
					this.setXRot(this.getXRot() - 12.0F + progress * 16.0F);
					serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.7D, this.getZ(), 2, 0.0D, 0.0D, 0.0D, 0.0D);
				}
				case ANIM_SLAM -> {
					this.setXRot(this.getXRot() + (float) Math.sin(progress * Math.PI) * 24.0F);
					serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.2D, this.getZ(), 4, 0.15D, 0.05D, 0.15D, 0.01D);
				}
				case ANIM_VANISH -> {
					this.setYRot(this.getYRot() + (float) Math.sin(progress * Math.PI * 4.0F) * 8.0F);
					this.setXRot(this.getXRot() + (float) Math.sin(progress * Math.PI * 2.0F) * 6.0F);
					serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.8D, this.getZ(), 3, 0.02D, 0.02D, 0.02D, 0.0D);
				}
				case ANIM_ROAR -> {
					this.setYRot(this.getYRot() + (float) Math.sin(progress * Math.PI * 2.0F) * 20.0F);
					this.setXRot(this.getXRot() + (float) Math.cos(progress * Math.PI) * 12.0F);
					serverLevel.sendParticles(ParticleTypes.ASH, this.getX(), this.getY() + 1.1D, this.getZ(), 3, 0.08D, 0.08D, 0.08D, 0.01D);
				}
				case ANIM_TELEPORT -> {
					this.setYRot(this.getYRot() + (float) Math.sin(progress * Math.PI * 4.0F) * 10.0F);
					serverLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.8D, this.getZ(), 2, 0.0D, 0.0D, 0.0D, 0.0D);
				}
				case ANIM_WARP -> {
					this.setYRot(this.getYRot() + (float) Math.sin(progress * Math.PI * 3.0F) * 12.0F);
					serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.9D, this.getZ(), 2, 0.0D, 0.0D, 0.0D, 0.01D);
				}
				default -> {
					this.yBodyRot = this.getYRot();
					this.yHeadRot = this.getYRot();
				}
			}
		}
		this.yBodyRot = this.getYRot();
		this.yHeadRot = this.getYRot();
	}

	private void triggerSwingAnimation(int ticks) {
		this.triggerAnimation(ANIM_SWING, ticks);
	}

	private void triggerAnimation(int animationType, int ticks) {
		this.activeAnimation = animationType;
		this.animationDuration = Math.max(ticks, 1);
		this.animationTimer = this.animationDuration;
	}

	private boolean shouldUseCover(double distanceSq) {
		return distanceSq > 18.0D * 18.0D && this.getRandom().nextFloat() < 0.4F;
	}

	private void performObservationWatch(Player target) {
		this.triggerAnimation(ANIM_WARP, 9);
		this.lookAt(target, 30.0F, 30.0F);
		this.spawnWhisperTrail();
		if (this.getRandom().nextFloat() < 0.65F) {
			this.performObservationTeleport(target);
		}
		if (this.level() instanceof ServerLevel serverLevel) {
			for (int i = 0; i < 4; i++) {
				double x = target.getX() + (this.getRandom().nextDouble() - 0.5D) * 2.4D;
				double y = target.getY() + 1.0D + this.getRandom().nextDouble() * 1.2D;
				double z = target.getZ() + (this.getRandom().nextDouble() - 0.5D) * 2.4D;
				serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	private void performFakeSighting(Player target) {
		this.triggerAnimation(ANIM_WARP, 7);
		Vec3 lookDirection = target.getLookAngle().normalize();
		Vec3 sightingPos = target.position().add(lookDirection.scale(-40.0D)).add(0.0D, 2.0D, 0.0D);
		if (this.level() instanceof ServerLevel serverLevel) {
			for (int i = 0; i < 12; i++) {
				double x = sightingPos.x() + (this.getRandom().nextDouble() - 0.5D) * 1.2D;
				double y = sightingPos.y() + (this.getRandom().nextDouble() - 0.5D) * 2.2D;
				double z = sightingPos.z() + (this.getRandom().nextDouble() - 0.5D) * 1.2D;
				serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
			serverLevel.playSound(
				null,
				target.getX(),
				target.getY(),
				target.getZ(),
				BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("ambient.cave")),
				SoundSource.AMBIENT,
				0.22F,
				0.8F
			);
		}
	}

	private void performDarknessDistortion(Player target) {
		this.triggerAnimation(ANIM_WARP, 8);
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.playSound(
				null,
				target.getX(),
				target.getY(),
				target.getZ(),
				BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("ambient.cave")),
				SoundSource.AMBIENT,
				0.3F,
				0.9F
			);
			for (int i = 0; i < 10; i++) {
				double x = target.getX() + (this.getRandom().nextDouble() - 0.5D) * 5.0D;
				double y = target.getY() + 1.4D + this.getRandom().nextDouble() * 2.0D;
				double z = target.getZ() + (this.getRandom().nextDouble() - 0.5D) * 5.0D;
				serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 2, 0.0D, 0.0D, 0.0D, 0.01D);
			}
		}
	}

	private void performObservationTeleport(Player target) {
		Vec3 facing = target.getLookAngle().normalize();
		Vec3 side = new Vec3(facing.z(), 0.0D, -facing.x()).normalize();
		for (int i = 0; i < 8; i++) {
			double tx = target.getX() + side.x() * (10.0D + i * 2.0D) + (this.getRandom().nextDouble() - 0.5D) * 4.0D;
			double tz = target.getZ() + side.z() * (10.0D + i * 2.0D) + (this.getRandom().nextDouble() - 0.5D) * 4.0D;
			double ty = target.getY() + 1.0D + this.getRandom().nextInt(5);
			BlockPos candidate = BlockPos.containing(tx, ty, tz);
			if (this.level().getBlockState(candidate).isAir() && this.level().getBlockState(candidate.below()).isSolidRender()) {
				this.setPos(tx, ty, tz);
				this.setDeltaMovement(0.0D, 0.0D, 0.0D);
				this.getNavigation().stop();
				return;
			}
		}
	}

	private void performShadowDash(Player target) {
		this.triggerAnimation(ANIM_DASH, 8);
		Vec3 direction = target.position().subtract(this.position()).normalize();
		this.setDeltaMovement(direction.x() * 0.8D, 0.12D, direction.z() * 0.8D);
		this.hurtMarked = true;
		this.getNavigation().stop();
	}

	private void performAmbientAura(Player target) {
		this.triggerAnimation(ANIM_ROAR, 10);
		this.spawnWhisperTrail();
		if (this.level() instanceof ServerLevel serverLevel) {
			for (int i = 0; i < 6; i++) {
				double x = this.getX() + (this.getRandom().nextDouble() - 0.5D) * 4.2D;
				double y = this.getY() + 0.8D + this.getRandom().nextDouble() * 1.2D;
				double z = this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 4.2D;
				serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 2, 0.0D, 0.0D, 0.0D, 0.01D);
			}
		}
	}

	private void performEchoSpawn(Player target) {
		this.triggerAnimation(ANIM_WARP, 8);
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		BlockPos spawnPos = target.blockPosition().offset(this.getRandom().nextInt(5) - 2, 1, this.getRandom().nextInt(5) - 2);
		if (this.level().getBlockState(spawnPos).isAir() && this.level().getBlockState(spawnPos.below()).isSolidRender()) {
			@SuppressWarnings("unchecked")
			HerobrineEntity echoClone = new HerobrineEntity((EntityType<HerobrineEntity>) (EntityType<?>) this.getType(), this.level());
			echoClone.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
			echoClone.setNoAi(true);
			echoClone.setInvulnerable(true);
			echoClone.setHealth(1.0F);
			echoClone.setDeltaMovement(0.0D, 0.0D, 0.0D);
			echoClone.echoClone = true;
			echoClone.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
			echoClone.setInvisible(false);
			serverLevel.addFreshEntity(echoClone);
			for (int i = 0; i < 8; i++) {
				double x = spawnPos.getX() + 0.5D + (this.getRandom().nextDouble() - 0.5D) * 0.8D;
				double y = spawnPos.getY() + 0.6D;
				double z = spawnPos.getZ() + 0.5D + (this.getRandom().nextDouble() - 0.5D) * 0.8D;
				serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 4, 0.05D, 0.05D, 0.05D, 0.01D);
			}
		}
	}

	private void performGroundSlam(Player target) {
		this.triggerAnimation(ANIM_SLAM, 12);
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		for (int i = 0; i < 14; i++) {
			double x = target.getX() + (this.getRandom().nextDouble() - 0.5D) * 4.2D;
			double z = target.getZ() + (this.getRandom().nextDouble() - 0.5D) * 4.2D;
			double y = target.getY() + 0.2D;
			serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 2, 0.0D, 0.0D, 0.0D, 0.0D);
		}
		this.applyPushToNearbyEntities(target, 0.42F);
	}

	private void performFearBurst(Player target) {
		this.triggerAnimation(ANIM_ROAR, 12);
		this.roaring = true;
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		for (int i = 0; i < 16; i++) {
			double x = target.getX() + (this.getRandom().nextDouble() - 0.5D) * 5.0D;
			double y = target.getY() + 1.0D;
			double z = target.getZ() + (this.getRandom().nextDouble() - 0.5D) * 5.0D;
			serverLevel.sendParticles(ParticleTypes.ASH, x, y, z, 5, 0.08D, 0.08D, 0.08D, 0.01D);
		}
		this.applyPushToNearbyEntities(target, 0.35F);
	}

	private void applyPushToNearbyEntities(Player target, float strength) {
		List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(7.0D), entity -> entity != this && entity != target && entity.isAlive());
		for (LivingEntity entity : nearby) {
			Vec3 direction = entity.position().subtract(this.position()).normalize();
			entity.setDeltaMovement(direction.x() * strength, 0.3D, direction.z() * strength);
			entity.hurtMarked = true;
		}
	}

	private void spawnWhisperTrail() {
		for (int i = 0; i < 3; i++) {
			double x = this.getX() + (this.getRandom().nextDouble() - 0.5D) * 1.2D;
			double y = this.getY() + 0.9D + this.getRandom().nextDouble() * 0.6D;
			double z = this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 1.2D;
			this.level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.02D, 0.0D);
		}
	}

	private boolean isActivelyEngaged(Player target, double distanceSq) {
		return distanceSq <= 9.0D * 9.0D || this.actionCooldown > 0 || this.getLastHurtByMob() == target;
	}

	private boolean shouldVanish(Player target, double distanceSq) {
		if (distanceSq > 20.0D * 20.0D) {
			return false;
		}
		Vec3 viewVector = target.getLookAngle().normalize();
		Vec3 toEntity = this.position().subtract(target.position()).normalize();
		return viewVector.dot(toEntity) > 0.78D;
	}

	private void performTeleport(Player target) {
		this.triggerAnimation(ANIM_TELEPORT, 8);
		Vec3 facing = target.getLookAngle();
		Vec3 away = new Vec3(-facing.x(), 0.0D, -facing.z()).normalize();
		for (int i = 0; i < 8; i++) {
			double tx = target.getX() + away.x() * (3.0D + i) + (this.getRandom().nextDouble() - 0.5D) * 2.0D;
			double tz = target.getZ() + away.z() * (3.0D + i) + (this.getRandom().nextDouble() - 0.5D) * 2.0D;
			double ty = target.getY() + 0.5D + this.getRandom().nextInt(2);
			BlockPos candidate = BlockPos.containing(tx, ty, tz);
			if (this.level().getBlockState(candidate).isAir() && this.level().getBlockState(candidate.below()).isSolidRender()) {
				this.setPos(tx, ty, tz);
				this.setDeltaMovement(0.0D, 0.0D, 0.0D);
				this.getNavigation().stop();
				return;
			}
		}
	}

	private void performWhisperEffect() {
		this.triggerAnimation(ANIM_WARP, 7);
		for (int i = 0; i < 10; i++) {
			double x = this.getX() + (this.getRandom().nextDouble() - 0.5D) * 2.2D;
			double y = this.getY() + 0.8D + this.getRandom().nextDouble() * 1.0D;
			double z = this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 2.2D;
			this.level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.02D, 0.0D);
		}
	}

	private void beginVanishing() {
		this.triggerAnimation(ANIM_VANISH, 10);
		this.vanished = true;
		this.vanishDuration = VANISH_MIN_DURATION + this.getRandom().nextInt(VANISH_DURATION_RANGE);
		this.vanishCooldown = 80 + this.getRandom().nextInt(80);
		this.setInvisible(true);
		this.getNavigation().stop();
		this.setDeltaMovement(0.0D, 0.08D, 0.0D);
	}

	private boolean tryHideBehindCover(Player target) {
		BlockPos start = this.blockPosition();
		for (int i = 0; i < 12; i++) {
			BlockPos checkPos = start.offset(this.getRandom().nextInt(5) - 2, this.getRandom().nextInt(3) - 1, this.getRandom().nextInt(5) - 2);
			if (this.level().getBlockState(checkPos).isSolidRender()) {
				this.getNavigation().moveTo(checkPos.getX() + 0.5D, checkPos.getY() + 0.5D, checkPos.getZ() + 0.5D, 0.9D);
				return true;
			}
		}
		return false;
	}

	private void performJumpAttack(Player target) {
		this.triggerAnimation(ANIM_JUMP, 10);
		Vec3 direction = target.position().subtract(this.position()).normalize();
		this.setDeltaMovement(direction.x() * 0.9D, 1.05D, direction.z() * 0.9D);
		this.hurtMarked = true;
	}

	private void performLightningStrike(Player target) {
		this.triggerAnimation(ANIM_ROAR, 8);
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		BlockPos strikePos = target.blockPosition().above(2);
		LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
		bolt.setPos(
			strikePos.getX() + 0.5D,
			strikePos.getY(),
			strikePos.getZ() + 0.5D
		);
		serverLevel.addFreshEntity(bolt);
		target.hurt(this.damageSources().lightningBolt(), 6.0F);
	}

	private void throwNearbyMobs(Player target) {
		List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(7.0D), entity -> entity != this && entity.isAlive());
		if (nearby.isEmpty()) {
			return;
		}
		Vec3 pushDirection = target.position().subtract(this.position()).normalize();
		for (LivingEntity mob : nearby) {
			Vec3 throwVector = new Vec3(
				pushDirection.x() + (this.getRandom().nextDouble() - 0.5D) * 0.45D,
				0.7D + this.getRandom().nextDouble() * 0.2D,
				pushDirection.z() + (this.getRandom().nextDouble() - 0.5D) * 0.45D
			);
			mob.setDeltaMovement(throwVector.scale(1.25D));
			mob.hurtMarked = true;
		}
	}

	private void performMessageSign(Player target) {
		this.triggerAnimation(ANIM_WARP, 7);
		String[] messages = new String[] { "Herobrine", "You are not alone", "Leave", "I see you" };
		String message = messages[this.getRandom().nextInt(messages.length)];
		BlockPos signPos = target.blockPosition().offset(this.getRandom().nextInt(9) - 4, 0, this.getRandom().nextInt(9) - 4);
		BlockPos placedPos = null;
		for (int i = 0; i < 6; i++) {
			BlockPos candidate = signPos.above(i);
			if (this.level().isEmptyBlock(candidate) && this.level().getBlockState(candidate.below()).isSolidRender()) {
				this.level().setBlockAndUpdate(candidate, Blocks.OAK_SIGN.defaultBlockState());
				placedPos = candidate;
				break;
			}
		}
		if (placedPos != null && this.level().getBlockEntity(placedPos) instanceof SignBlockEntity signBlockEntity) {
			SignText signText = new SignText()
					.setMessage(0, Component.literal(message));

			signBlockEntity.setText(signText, true);
			signBlockEntity.setChanged();
		}
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.01D);
		}
		target.sendSystemMessage(Component.literal(message));
	}

	private void performRealityGlitch(Player target) {
		this.triggerAnimation(ANIM_WARP, 8);
		BlockPos origin = target.blockPosition().offset(this.getRandom().nextInt(7) - 3, this.getRandom().nextInt(3) - 1, this.getRandom().nextInt(7) - 3);
		for (int i = 0; i < 4; i++) {
			BlockPos glitchPos = origin.offset(this.getRandom().nextInt(3) - 1, this.getRandom().nextInt(3) - 1, this.getRandom().nextInt(3) - 1);
			if (this.level().getBlockState(glitchPos).isAir()) {
				this.level().setBlockAndUpdate(glitchPos, Blocks.COBBLESTONE.defaultBlockState());
			} else if (this.level().getBlockState(glitchPos).is(Blocks.COBBLESTONE)) {
				this.level().setBlockAndUpdate(glitchPos, Blocks.AIR.defaultBlockState());
			}
			this.level().addParticle(ParticleTypes.PORTAL, glitchPos.getX() + 0.5D, glitchPos.getY() + 0.5D, glitchPos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
		}
		this.setPos(target.getX() + (this.getRandom().nextDouble() - 0.5D) * 2.0D, this.getY(), target.getZ() + (this.getRandom().nextDouble() - 0.5D) * 2.0D);
		this.setDeltaMovement(0.0D, 0.0D, 0.0D);
		this.getNavigation().stop();
	}

	private void performWorldControl(Player target) {
		this.triggerAnimation(ANIM_WARP, 8);
		BlockPos center = target.blockPosition().offset(this.getRandom().nextInt(7) - 3, this.getRandom().nextInt(3) - 1, this.getRandom().nextInt(7) - 3);
		boolean built = false;
		if (this.getRandom().nextBoolean()) {
			for (int x = 0; x < 2; x++) {
				for (int z = 0; z < 2; z++) {
					BlockPos mutatePos = center.offset(x, 0, z);
					if (this.level().isEmptyBlock(mutatePos) && this.level().getBlockState(mutatePos.below()).isSolidRender()) {
						this.level().setBlockAndUpdate(mutatePos, Blocks.COBBLESTONE.defaultBlockState());
						this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, mutatePos.getX() + 0.5D, mutatePos.getY() + 0.5D, mutatePos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
						built = true;
					}
				}
			}
		} else {
			for (int i = 0; i < 3; i++) {
				BlockPos mutatePos = center.above(i);
				if (this.level().isEmptyBlock(mutatePos) && this.level().getBlockState(mutatePos.below()).isSolidRender()) {
					this.level().setBlockAndUpdate(mutatePos, Blocks.OAK_LOG.defaultBlockState());
					if (i == 2) {
						this.level().setBlockAndUpdate(mutatePos.above(), Blocks.REDSTONE_TORCH.defaultBlockState());
					}
					this.level().addParticle(ParticleTypes.SMOKE, mutatePos.getX() + 0.5D, mutatePos.getY() + 0.5D, mutatePos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
					built = true;
					break;
				}
			}
		}
		if (!built) {
			for (int i = 0; i < 4; i++) {
				BlockPos mutatePos = center.offset(this.getRandom().nextInt(3) - 1, 0, this.getRandom().nextInt(3) - 1);
				if (this.level().getBlockState(mutatePos).is(Blocks.OAK_LEAVES)) {
					this.level().setBlockAndUpdate(mutatePos, Blocks.AIR.defaultBlockState());
					this.level().addParticle(ParticleTypes.SMOKE, mutatePos.getX() + 0.5D, mutatePos.getY() + 0.5D, mutatePos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
					break;
				}
			}
		}
	}

	private void performTotemRevival() {
		this.triggerAnimation(ANIM_ROAR, 12);
		this.totemCooldown = 260 + this.getRandom().nextInt(120);
		this.setHealth(Math.min(this.getMaxHealth(), this.getHealth() + this.getMaxHealth() * 0.35F));
		if (this.level() instanceof ServerLevel serverLevel) {
			for (int i = 0; i < 10; i++) {
				double x = this.getX() + (this.getRandom().nextDouble() - 0.5D) * 2.4D;
				double y = this.getY() + 1.0D + this.getRandom().nextDouble() * 1.2D;
				double z = this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 2.4D;
				serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 2, 0.0D, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader world) {
		return world.isUnobstructed(this);
	}

	@Override
	public boolean canDrownInFluidType(FluidType type) {
		return false;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
		builder = builder.add(Attributes.MAX_HEALTH, 1000);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 5);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.STEP_HEIGHT, 0.6);
		builder = builder.add(Attributes.ATTACK_KNOCKBACK, 2);
		builder = builder.add(NeoForgeMod.SWIM_SPEED, 0.3);
		return builder;
	}
}