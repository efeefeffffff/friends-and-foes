package com.faboslav.friendsandfoes.entity.ai.brain;

import com.faboslav.friendsandfoes.entity.BarnacleEntity;
import com.faboslav.friendsandfoes.entity.WildfireEntity;
import com.faboslav.friendsandfoes.init.FriendsAndFoesMemorySensorType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class BarnacleBrain
{
	public static final List<MemoryModuleType<?>> MEMORY_MODULES;
	public static final List<SensorType<? extends Sensor<? super BarnacleEntity>>> SENSORS;
	private static final TargetPredicate VALID_TARGET_PLAYER_PREDICATE;

	public BarnacleBrain() {
	}

	public static Brain<?> create(Dynamic<?> dynamic) {
		Brain.Profile<BarnacleEntity> profile = Brain.createProfile(MEMORY_MODULES, SENSORS);
		Brain<BarnacleEntity> brain = profile.deserialize(dynamic);

		addCoreActivities(brain);
		addAvoidActivities(brain);
		addIdleActivities(brain);

		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.resetPossibleActivities();

		return brain;
	}

	private static void addCoreActivities(Brain<BarnacleEntity> brain) {
		brain.setTaskList(Activity.CORE,
			0,
			ImmutableList.of(
				new LookAroundTask(45, 90),
				new WanderAroundTask()
			)
		);
	}

	private static void addIdleActivities(Brain<BarnacleEntity> brain) {
		brain.setTaskList(
			Activity.IDLE,
			ImmutableList.of(
				Pair.of(0, new UpdateAttackTargetTask(barnacle -> getTarget((BarnacleEntity) barnacle))),
				Pair.of(0, makeRandomWanderTask())
			),
			ImmutableSet.of(
				Pair.of(MemoryModuleType.AVOID_TARGET, MemoryModuleState.VALUE_ABSENT)
			)
		);
	}

	private static void addAvoidActivities(Brain<BarnacleEntity> brain) {
		brain.setTaskList(
			Activity.AVOID,
			ImmutableList.of(
				Pair.of(0, GoToRememberedPositionTask.toEntity(MemoryModuleType.AVOID_TARGET, 1.25F, 16, false))
			),
			ImmutableSet.of(
				Pair.of(MemoryModuleType.AVOID_TARGET, MemoryModuleState.VALUE_PRESENT)
			)
		);
	}

	public static void updateActivities(BarnacleEntity barnacle) {
		barnacle.getBrain().resetPossibleActivities(
			ImmutableList.of(
				Activity.AVOID,
				Activity.IDLE
			)
		);
	}

	private static RandomTask<BarnacleEntity> makeRandomWanderTask() {
		return new RandomTask(
			ImmutableList.of(
				Pair.of(new StrollTask(0.6F, true), 2),
				Pair.of(new GoTowardsLookTarget(1.0F, 3), 2),
				Pair.of(new WaitTask(30, 60), 1)
			)
		);
	}

	private static Optional<? extends LivingEntity> getTarget(BarnacleEntity barnacle) {
		PlayerEntity nearestVisibleTargetablePlayer = barnacle.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER).orElse(
			barnacle.getWorld().getClosestPlayer(VALID_TARGET_PLAYER_PREDICATE, barnacle, barnacle.getX(), barnacle.getEyeY(), barnacle.getZ())
		);

		if (nearestVisibleTargetablePlayer == null) {
			return Optional.empty();
		}

		return Optional.of(nearestVisibleTargetablePlayer);
	}

	public static void setAttackTarget(WildfireEntity wildfire, LivingEntity target) {
		wildfire.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		wildfire.getBrain().remember(MemoryModuleType.ATTACK_TARGET, target, 200L);
	}

	static {
		SENSORS = List.of(
			SensorType.NEAREST_LIVING_ENTITIES,
			SensorType.NEAREST_PLAYERS,
			FriendsAndFoesMemorySensorType.BARNACLE_SPECIFIC_SENSOR.get()
		);
		MEMORY_MODULES = List.of(
			MemoryModuleType.VISIBLE_MOBS,
			MemoryModuleType.PATH,
			MemoryModuleType.LOOK_TARGET,
			MemoryModuleType.WALK_TARGET,
			MemoryModuleType.AVOID_TARGET,
			MemoryModuleType.INTERACTION_TARGET,
			MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
			MemoryModuleType.NEAREST_PLAYERS,
			MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE
		);
		VALID_TARGET_PLAYER_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(WildfireEntity.GENERIC_FOLLOW_RANGE);
	}
}