package com.Tropfen_33.beyond_the_horizons_fixes.mixin.worldgen;

import com.Tropfen_33.beyond_the_horizons_fixes.worldgen.StructureAccess;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/** Lets StructureScaler change the spacing / separation of a random-spread placement. */
@Mixin(RandomSpreadStructurePlacement.class)
public abstract class RandomSpreadStructurePlacementMixin implements StructureAccess.Spread {
	@Shadow
	@Mutable
	@Final
	private int spacing;

	@Shadow
	@Mutable
	@Final
	private int separation;

	@Override
	public void bth$setSpread(int spacing, int separation) {
		this.spacing = spacing;
		this.separation = separation;
	}
}
