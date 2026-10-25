package com.Tropfen_33.beyond_the_horizons_fixes.worldgen;

/**
 * Duck interfaces implemented by mixins so {@link StructureScaler} can read and change values
 * of vanilla / Yung's structure placements, which have no public setters.
 */
public final class StructureAccess {
	private StructureAccess() {
	}

	/** Implemented on StructurePlacement (the "frequency" chance of a structure chunk actually generating). */
	public interface Frequency {
		float bth$getFrequency();

		void bth$setFrequency(float frequency);
	}

	/** Implemented on RandomSpreadStructurePlacement (spacing / separation, in chunks). */
	public interface Spread {
		void bth$setSpread(int spacing, int separation);
	}

	/** Implemented on YUNG's Better Strongholds placement (the ring distances, in chunks). */
	public interface Rings {
		int bth$getChunkDistanceToFirstRing();

		int bth$getRingChunkThickness();

		void bth$setRings(int chunkDistanceToFirstRing, int ringChunkThickness);
	}
}
