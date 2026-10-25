package com.Tropfen_33.beyond_the_horizons_fixes.mixin.worldgen;

import com.Tropfen_33.beyond_the_horizons_fixes.worldgen.StructureAccess;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StructurePlacement.class)
public abstract class StructurePlacementMixin implements StructureAccess.Frequency {
	@Shadow
	@Mutable
	@Final
	private float frequency;

	@Override
	public float bth$getFrequency() {
		return this.frequency;
	}

	@Override
	public void bth$setFrequency(float frequency) {
		this.frequency = frequency;
	}
}
