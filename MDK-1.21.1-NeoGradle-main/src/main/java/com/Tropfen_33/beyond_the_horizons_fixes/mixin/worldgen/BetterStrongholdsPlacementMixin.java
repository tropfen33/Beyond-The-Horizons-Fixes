package com.Tropfen_33.beyond_the_horizons_fixes.mixin.worldgen;

import com.Tropfen_33.beyond_the_horizons_fixes.worldgen.StructureAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * YUNG's Better Strongholds places strongholds in rings around the world origin. Scaling only the
 * spacing would leave the rings where they are, so their distances are scaled as well.
 * Referenced by name: nothing happens when that mod is not installed.
 */
@Mixin(targets = "com.yungnickyoung.minecraft.betterstrongholds.world.placement.BetterStrongholdsPlacement", remap = false)
public abstract class BetterStrongholdsPlacementMixin implements StructureAccess.Rings {
	@Shadow(remap = false)
	@Mutable
	@Final
	private int chunkDistanceToFirstRing;

	@Shadow(remap = false)
	@Mutable
	@Final
	private int ringChunkThickness;

	@Override
	public int bth$getChunkDistanceToFirstRing() {
		return this.chunkDistanceToFirstRing;
	}

	@Override
	public int bth$getRingChunkThickness() {
		return this.ringChunkThickness;
	}

	@Override
	public void bth$setRings(int chunkDistanceToFirstRing, int ringChunkThickness) {
		this.chunkDistanceToFirstRing = chunkDistanceToFirstRing;
		this.ringChunkThickness = ringChunkThickness;
	}
}
