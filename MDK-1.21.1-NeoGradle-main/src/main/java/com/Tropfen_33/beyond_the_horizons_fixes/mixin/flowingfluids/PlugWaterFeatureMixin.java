package com.Tropfen_33.beyond_the_horizons_fixes.mixin.flowingfluids;

import com.Tropfen_33.beyond_the_horizons_fixes.config.FixesConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Flowing Fluids seals every generated fluid source that touches air with a solid block (mud, sand,
 * stone ...) right after a chunk's biome decoration. This lets the config switch that off.
 * Referenced by name and marked @Pseudo: nothing happens when Flowing Fluids is not installed.
 */
@Pseudo
@Mixin(targets = "traben.flowing_fluids.PlugWaterFeature", remap = false)
public abstract class PlugWaterFeatureMixin {

	@Inject(method = "processChunk", at = @At("HEAD"), cancellable = true, remap = false)
	private static void bthFixes$skipWorldGenPlugging(LevelAccessor level, ChunkPos chunk, ChunkAccess chunkAccess, CallbackInfo ci) {
		// World generation passes a WorldGenRegion. The manual debug command passes the real ServerLevel
		// and should keep working even when world gen plugging is switched off.
		if (level instanceof ServerLevel) {
			return;
		}
		if (!FixesConfig.plugFluidsDuringWorldGen()) {
			ci.cancel();
		}
	}
}
