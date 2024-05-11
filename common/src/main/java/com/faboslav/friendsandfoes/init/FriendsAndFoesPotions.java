package com.faboslav.friendsandfoes.init;

import com.faboslav.friendsandfoes.FriendsAndFoes;
import com.faboslav.friendsandfoes.init.registry.RegistryEntry;
import com.faboslav.friendsandfoes.init.registry.ResourcefulRegistries;
import com.faboslav.friendsandfoes.init.registry.ResourcefulRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.Registry;

/**
 * @see net.minecraft.potion.Potions
 */
public final class FriendsAndFoesPotions
{
	public static final ResourcefulRegistry<Potion> POTIONS = ResourcefulRegistries.create(Registry.POTION, FriendsAndFoes.MOD_ID);

	public static final RegistryEntry<Potion> REACHING = POTIONS.register("reaching", () -> new Potion(new StatusEffectInstance(FriendsAndFoesStatusEffects.LONG_REACH.get(), 72000)));
	public static final RegistryEntry<Potion> LONG_REACHING = POTIONS.register("long_reaching", () -> new Potion("reaching", new StatusEffectInstance(FriendsAndFoesStatusEffects.LONG_REACH.get(), 144000)));
}