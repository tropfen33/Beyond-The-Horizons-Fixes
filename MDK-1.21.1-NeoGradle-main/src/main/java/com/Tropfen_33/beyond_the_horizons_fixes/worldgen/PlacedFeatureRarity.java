package com.Tropfen_33.beyond_the_horizons_fixes.worldgen;

import com.Tropfen_33.beyond_the_horizons_fixes.config.FixesConfig;
import com.google.common.collect.MapMaker;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.concurrent.ConcurrentMap;

/**
 * Looks up (and caches) the rarity ratio for a placed feature. Worldgen asks about every placed
 * feature of every biome for every chunk, so the lookup has to be cheap: the cache is keyed by
 * object identity (weak keys), not by the record's expensive equals/hashCode.
 */
public final class PlacedFeatureRarity {
	/** Set while a repeated placement attempt runs, so the mixin does not scale it again. */
	public static final ThreadLocal<Boolean> BUSY = ThreadLocal.withInitial(() -> false);

	private static final ConcurrentMap<PlacedFeature, Double> CACHE = new MapMaker().weakKeys().makeMap();

	private PlacedFeatureRarity() {
	}

	public static void clearCache() {
		CACHE.clear();
	}

	public static double ratio(PlacedFeature feature, WorldGenLevel level) {
		Double cached = CACHE.get(feature);
		if (cached != null) {
			return cached;
		}
		if (!FixesConfig.isLoaded()) {
			return 1.0;
		}
		ResourceLocation key = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getKey(feature);
		double ratio = key == null ? 1.0 : FixesConfig.getPlacedFeatureRatio(key);
		CACHE.put(feature, ratio);
		return ratio;
	}
}
