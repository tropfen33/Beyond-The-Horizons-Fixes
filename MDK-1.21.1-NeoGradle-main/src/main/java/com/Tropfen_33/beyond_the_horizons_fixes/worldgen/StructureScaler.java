package com.Tropfen_33.beyond_the_horizons_fixes.worldgen;

import com.Tropfen_33.beyond_the_horizons_fixes.BeyondTheHorizonsFixes;
import com.Tropfen_33.beyond_the_horizons_fixes.config.FixesConfig;
import com.google.common.collect.MapMaker;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Applies the structure distance multipliers from the config to every structure set when a
 * server starts, before any chunk is generated.
 *
 * A multiplier of 2 means structures are twice as far apart (you travel twice the distance, so the
 * area you have to search is four times as large). The values are always computed from the
 * placement's ORIGINAL numbers, so applying it again (new world, changed config) never compounds.
 */
public final class StructureScaler {
	private record Original(int spacing, int separation, float frequency, int firstRing, int ringThickness) {
	}

	private static final ConcurrentMap<StructurePlacement, Original> ORIGINALS = new MapMaker().weakKeys().makeMap();

	private StructureScaler() {
	}

	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		Registry<StructureSet> registry = event.getServer().registryAccess().registryOrThrow(Registries.STRUCTURE_SET);

		for (Map.Entry<ResourceKey<StructureSet>, StructureSet> entry : registry.entrySet()) {
			ResourceLocation id = entry.getKey().location();
			if (FixesConfig.shouldLogStructureSetIds()) {
				BeyondTheHorizonsFixes.LOGGER.info("Structure set: {} ({})", id, entry.getValue().placement().getClass().getSimpleName());
			}

			double factor = FixesConfig.getStructureDistanceFactor(id);
			try {
				apply(id, entry.getValue().placement(), factor);
			} catch (RuntimeException e) {
				BeyondTheHorizonsFixes.LOGGER.error("Could not scale structure set {}", id, e);
			}
		}
	}

	private static void apply(ResourceLocation id, StructurePlacement placement, double factor) {
		if (!(placement instanceof StructureAccess.Frequency frequencyAccess)) {
			return;
		}

		Original original = ORIGINALS.computeIfAbsent(placement, StructureScaler::capture);

		if (placement instanceof RandomSpreadStructurePlacement && placement instanceof StructureAccess.Spread spreadAccess) {
			int spacing = original.spacing();
			int separation = original.separation();
			float frequency = original.frequency();

			if (original.spacing() <= 1) {
				// Frequency-driven sets (e.g. mineshafts: 1 chunk grid, small chance per chunk).
				// Twice the distance means a quarter of the density.
				frequency = (float) Math.min(1.0, original.frequency() / (factor * factor));
			} else {
				spacing = Math.max(1, clampToInt(original.spacing() * factor));
				separation = Math.max(0, Math.min(spacing - 1, clampToInt(original.separation() * factor)));
			}

			spreadAccess.bth$setSpread(spacing, separation);
			frequencyAccess.bth$setFrequency(frequency);
		}

		if (placement instanceof StructureAccess.Rings ringsAccess) {
			ringsAccess.bth$setRings(
					Math.max(0, clampToInt(original.firstRing() * factor)),
					Math.max(1, clampToInt(original.ringThickness() * factor)));
		}

		if (factor != 1.0 && FixesConfig.shouldLogStructureChanges()) {
			BeyondTheHorizonsFixes.LOGGER.info("Structure set {} distance x{}: spacing {} -> {}, separation {} -> {}, frequency {} -> {}",
					id, factor,
					original.spacing(), placement instanceof RandomSpreadStructurePlacement rs ? rs.spacing() : "-",
					original.separation(), placement instanceof RandomSpreadStructurePlacement rs2 ? rs2.separation() : "-",
					original.frequency(), frequencyAccess.bth$getFrequency());
		}
	}

	/** There is no upper limit in the config; internally the chunk maths just must not overflow an int. */
	private static int clampToInt(double value) {
		return (int) Math.min(Integer.MAX_VALUE / 4, Math.round(value));
	}

	private static Original capture(StructurePlacement placement) {
		int spacing = 0;
		int separation = 0;
		if (placement instanceof RandomSpreadStructurePlacement rs) {
			spacing = rs.spacing();
			separation = rs.separation();
		}
		float frequency = placement instanceof StructureAccess.Frequency f ? f.bth$getFrequency() : 1.0F;
		int firstRing = 0;
		int ringThickness = 1;
		if (placement instanceof StructureAccess.Rings rings) {
			firstRing = rings.bth$getChunkDistanceToFirstRing();
			ringThickness = rings.bth$getRingChunkThickness();
		}
		return new Original(spacing, separation, frequency, firstRing, ringThickness);
	}
}
