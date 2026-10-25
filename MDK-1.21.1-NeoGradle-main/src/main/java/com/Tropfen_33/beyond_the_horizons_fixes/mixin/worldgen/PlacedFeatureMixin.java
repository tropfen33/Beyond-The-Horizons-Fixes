package com.Tropfen_33.beyond_the_horizons_fixes.mixin.worldgen;

import com.Tropfen_33.beyond_the_horizons_fixes.worldgen.PlacedFeatureRarity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scales how often a placed feature generates without editing its JSON.
 * Ratio &lt; 1: some attempts are skipped. Ratio &gt; 1: extra attempts are made (each one still goes
 * through the feature's own rarity filter, so N extra attempts = N times the chance).
 */
@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureMixin {

	@Inject(method = "placeWithBiomeCheck", at = @At("HEAD"), cancellable = true, remap = false)
	private void bthFixes$scaleRarity(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (PlacedFeatureRarity.BUSY.get()) {
			return;
		}

		PlacedFeature self = (PlacedFeature) (Object) this;
		double ratio = PlacedFeatureRarity.ratio(self, level);
		if (ratio == 1.0) {
			return;
		}

		if (ratio < 1.0) {
			if (random.nextDouble() >= ratio) {
				cir.setReturnValue(false);
			}
			return;
		}

		int extraAttempts = (int) Math.floor(ratio) - 1;
		if (random.nextDouble() < ratio - Math.floor(ratio)) {
			extraAttempts++;
		}

		PlacedFeatureRarity.BUSY.set(true);
		try {
			for (int i = 0; i < extraAttempts; i++) {
				self.placeWithBiomeCheck(level, generator, random, pos);
			}
		} finally {
			PlacedFeatureRarity.BUSY.set(false);
		}
	}
}
