package com.Tropfen_33.beyond_the_horizons_fixes.config;

import com.Tropfen_33.beyond_the_horizons_fixes.BeyondTheHorizonsFixes;
import com.Tropfen_33.beyond_the_horizons_fixes.worldgen.PlacedFeatureRarity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FixesConfig {
	public static final ModConfigSpec SPEC;

	private static final ModConfigSpec.IntValue BACKPACK_MAX_SLOTS_PER_ITEM;
	private static final ModConfigSpec.ConfigValue<List<? extends String>> BACKPACK_ITEM_OVERRIDES;

	private static final List<String> VENT_ORES = List.of("asurine", "crimsite", "ochrum", "scorchia", "scoria", "veridium");
	private static final Map<String, ModConfigSpec.DoubleValue> VENT_DISTANCES = new LinkedHashMap<>();
	private static final ModConfigSpec.DoubleValue VENT_GLOBAL_DISTANCE;

	/** namespace -> structure set names (as in data/<namespace>/worldgen/structure_set/<name>.json). */
	private static final Map<String, List<String>> KNOWN_STRUCTURE_SETS = new LinkedHashMap<>();
	private static final Map<ResourceLocation, ModConfigSpec.DoubleValue> STRUCTURE_SET_VALUES = new LinkedHashMap<>();

	static {
		KNOWN_STRUCTURE_SETS.put("minecraft", List.of(
				"villages", "desert_pyramids", "jungle_temples", "swamp_huts", "igloos", "pillager_outposts",
				"woodland_mansions", "ocean_monuments", "ocean_ruins", "shipwrecks", "buried_treasures",
				"mineshafts", "strongholds", "ancient_cities", "ruined_portals", "trail_ruins", "trial_chambers",
				"nether_complexes", "nether_fossils", "end_cities"));
		KNOWN_STRUCTURE_SETS.put("betterdungeons", List.of(
				"small_dungeons", "zombie_dungeons", "skeleton_dungeons", "spider_dungeons", "small_nether_dungeons"));
		KNOWN_STRUCTURE_SETS.put("betterdeserttemples", List.of("desert_temples"));
		KNOWN_STRUCTURE_SETS.put("betterjungletemples", List.of("jungle_temples"));
		KNOWN_STRUCTURE_SETS.put("bettermineshafts", List.of("mineshafts"));
		KNOWN_STRUCTURE_SETS.put("betteroceanmonuments", List.of("ocean_monument"));
		KNOWN_STRUCTURE_SETS.put("betterstrongholds", List.of("stronghold"));
		KNOWN_STRUCTURE_SETS.put("betterwitchhuts", List.of("witch_hut", "witch_circle"));
	}

	private static final ModConfigSpec.DoubleValue STRUCTURE_DISTANCE;
	private static final ModConfigSpec.ConfigValue<List<? extends String>> STRUCTURE_NAMESPACES;
	private static final ModConfigSpec.ConfigValue<List<? extends String>> STRUCTURE_OVERRIDES;
	private static final ModConfigSpec.BooleanValue STRUCTURE_LOG_ALL;
	private static final ModConfigSpec.BooleanValue STRUCTURE_LOG_CHANGES;

	private static final ModConfigSpec.BooleanValue FLOWING_FLUIDS_PLUG_WORLDGEN;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		// ---------------------------------------------------------------- backpacks
		builder.comment("Sophisticated Backpacks / Sophisticated Storage slot limits").push("sophisticatedBackpacks");

		BACKPACK_MAX_SLOTS_PER_ITEM = builder
				.comment(
						"Maximum number of slots in ONE backpack that may hold the same item (e.g. sticks).",
						"Stacking into a slot that already holds the item always works; only starting a NEW slot is blocked",
						"once the limit is reached. Combine with Stack Upgrades so one slot can hold a lot.",
						"0 = unlimited (vanilla behaviour of the mod).")
				.defineInRange("maxSlotsPerItem", 0, 0, 1024);

		BACKPACK_ITEM_OVERRIDES = builder
				.comment(
						"Per-item overrides of maxSlotsPerItem, format \"modid:item=slots\".",
						"Example: [\"minecraft:stick=1\", \"minecraft:cobblestone=2\"]",
						"An override of 0 means unlimited for that item, even if maxSlotsPerItem is set.")
				.defineListAllowEmpty("itemOverrides", List.of(), () -> "minecraft:stick=1", FixesConfig::isValidOverride);

		builder.pop();

		// ---------------------------------------------------------------- molten vents
		builder.comment(
				"Create: Molten Vents spawn rarity. Only affects chunks generated AFTER the change.",
				"All values are DISTANCE multipliers: 1 = unchanged, 2 = you have to search twice the distance to find one",
				"(that is 1/4 as many vents), 0.5 = half the distance (4x as many vents), 0 = vent disabled."
		).push("createMoltenVents");

		VENT_GLOBAL_DISTANCE = builder
				.comment("Distance multiplier for ALL molten_vents features, on top of the per-vent values below.")
				.defineInRange("globalDistanceMultiplier", 1.0, 0.0, Double.MAX_VALUE);

		for (String ore : VENT_ORES) {
			String land = ore + "_vent";
			String water = "aquatic_" + ore + "_vent";
			VENT_DISTANCES.put(land, builder
					.comment("Distance multiplier for molten_vents:" + land)
					.defineInRange(land, 1.0, 0.0, Double.MAX_VALUE));
			VENT_DISTANCES.put(water, builder
					.comment("Distance multiplier for molten_vents:" + water)
					.defineInRange(water, 1.0, 0.0, Double.MAX_VALUE));
		}

		builder.pop();

		// ---------------------------------------------------------------- structures
		builder.comment(
				"Spawn rarity of structures (Yung's Better ... mods and any other structure set).",
				"Values are DISTANCE multipliers: 2 = structures are twice as far apart (1/4 as many), 0.5 = half as far apart.",
				"Only affects chunks generated AFTER the change. Only structure sets with 'random_spread' placement are scaled",
				"(Yung's mods, villages, temples ...). Sets with a 1-chunk grid (e.g. mineshafts) are thinned out by chance instead.",
				"Yung's Better Strongholds additionally has its ring distances scaled."
		).push("structures");

		STRUCTURE_DISTANCE = builder
				.comment("Distance multiplier applied to every structure set whose namespace is listed in 'affectedNamespaces'.")
				.defineInRange("distanceMultiplier", 1.0, 0.05, Double.MAX_VALUE);

		STRUCTURE_NAMESPACES = builder
				.comment(
						"Namespaces (mod ids) whose structure sets use 'distanceMultiplier'.",
						"The defaults are a best guess for Yung's Better mods; check the real names with logAllStructureSets.")
				.defineListAllowEmpty("affectedNamespaces", List.of(
						"betterdungeons", "betterstrongholds", "bettermineshafts", "betterfortresses",
						"betterdesertvillages", "betterwitchhuts", "betteroceanmonuments", "betterjungletemples"
				), () -> "betterdungeons", o -> o instanceof String s && !s.isBlank());

		STRUCTURE_OVERRIDES = builder
				.comment(
						"Individual multipliers, format \"namespace=factor\" or \"namespace:structure_set=factor\".",
						"A single structure set entry wins over a namespace entry, which wins over distanceMultiplier.",
						"Example: [\"betterdungeons=2\", \"betterdungeons:small_dungeon=3\", \"minecraft:villages=1.5\"]")
				.defineListAllowEmpty("overrides", List.of(), () -> "betterdungeons=2", FixesConfig::isValidFactorEntry);

		STRUCTURE_LOG_ALL = builder
				.comment("If true, logs every structure set (id, spacing, separation) when a world loads, so you can find the right names.")
				.define("logAllStructureSets", false);

		STRUCTURE_LOG_CHANGES = builder
				.comment("If true, logs every structure set whose spacing was changed, with the old and new values.")
				.define("logStructureChanges", true);

		builder.comment(
				"One distance multiplier per structure (1 = unchanged, 2 = twice as far apart = 1/4 as many, 0.5 = half as far apart).",
				"No upper limit. Multiplied with distanceMultiplier / overrides above. Entries of mods that are not installed do nothing.",
				"Structures of other mods: use 'overrides' above, with the names printed by logAllStructureSets."
		).push("perStructure");
		for (Map.Entry<String, List<String>> ns : KNOWN_STRUCTURE_SETS.entrySet()) {
			builder.push(ns.getKey());
			for (String name : ns.getValue()) {
				STRUCTURE_SET_VALUES.put(ResourceLocation.fromNamespaceAndPath(ns.getKey(), name), builder
						.comment("Distance multiplier for " + ns.getKey() + ":" + name)
						.defineInRange(name, 1.0, 0.05, Double.MAX_VALUE));
			}
			builder.pop();
		}
		builder.pop();

		builder.pop();

		// ---------------------------------------------------------------- flowing fluids
		builder.comment("Flowing Fluids (flowing_fluids) integration. Does nothing when that mod is not installed.")
				.push("flowingFluids");

		FLOWING_FLUIDS_PLUG_WORLDGEN = builder
				.comment(
						"Flowing Fluids seals every freshly generated water/lava source that touches air (sides or below)",
						"with a solid block so it cannot flow away: mud on land, sand in deserts, beaches and oceans, stone/deepslate",
						"deep down, netherrack in the Nether. That is where the mud around rivers and lakes comes from.",
						"true  = keep that behaviour (Flowing Fluids default).",
						"false = skip it, no sealing blocks are placed. World generation may lag more and generated water can spread.",
						"Only affects chunks generated AFTER the change. The /flowing_fluids debug command that seals fluids in",
						"nearby chunks by hand keeps working either way.")
				.define("plugFluidsDuringWorldGen", true);

		builder.pop();

		SPEC = builder.build();
	}

	/** Parsed copies, rebuilt whenever the config loads or reloads. */
	private static volatile Map<ResourceLocation, Integer> overrides = Map.of();
	private static volatile Map<String, Double> structureNamespaceOverrides = Map.of();
	private static volatile Map<ResourceLocation, Double> structureSetOverrides = Map.of();

	private FixesConfig() {
	}

	public static void onConfigEvent(ModConfigEvent event) {
		if (event.getConfig().getSpec() == SPEC) {
			overrides = parseOverrides(BACKPACK_ITEM_OVERRIDES.get());
			parseStructureOverrides(STRUCTURE_OVERRIDES.get());
			PlacedFeatureRarity.clearCache();
		}
	}

	public static boolean isLoaded() {
		return SPEC.isLoaded();
	}

	// ---------------------------------------------------------------- backpacks

	/** Slot limit for the given item id; 0 means unlimited. */
	public static int getBackpackSlotLimit(ResourceLocation itemId) {
		Integer override = overrides.get(itemId);
		if (override != null) {
			return override;
		}
		return SPEC.isLoaded() ? BACKPACK_MAX_SLOTS_PER_ITEM.get() : 0;
	}

	// ---------------------------------------------------------------- molten vents

	/**
	 * Density ratio for a placed feature compared to its built-in rarity: 1.0 = unchanged,
	 * 0.25 = a quarter as many (distance x2), 4.0 = four times as many (distance x0.5).
	 * Only molten_vents:* features are ever scaled.
	 */
	public static double getPlacedFeatureRatio(ResourceLocation key) {
		if (!SPEC.isLoaded() || !"molten_vents".equals(key.getNamespace())) {
			return 1.0;
		}
		double distance = VENT_GLOBAL_DISTANCE.get();
		ModConfigSpec.DoubleValue own = VENT_DISTANCES.get(key.getPath());
		if (own != null) {
			distance *= own.get();
		}
		if (distance <= 0.0) {
			return 0.0;
		}
		return 1.0 / (distance * distance);
	}

	// ---------------------------------------------------------------- structures

	public static boolean shouldLogStructureSetIds() {
		return SPEC.isLoaded() && STRUCTURE_LOG_ALL.get();
	}

	public static boolean shouldLogStructureChanges() {
		return SPEC.isLoaded() && STRUCTURE_LOG_CHANGES.get();
	}

	/** Distance multiplier for a structure set id; 1.0 = unchanged. */
	public static double getStructureDistanceFactor(ResourceLocation setId) {
		double factor = getStructureDistance(setId);
		ModConfigSpec.DoubleValue own = SPEC.isLoaded() ? STRUCTURE_SET_VALUES.get(setId) : null;
		return own == null ? factor : factor * own.get();
	}

	/** Distance multiplier for a structure set id; 1.0 = unchanged. */
	public static double getStructureDistance(ResourceLocation setId) {
		if (!SPEC.isLoaded()) {
			return 1.0;
		}
		Double exact = structureSetOverrides.get(setId);
		if (exact != null) {
			return exact;
		}
		Double namespace = structureNamespaceOverrides.get(setId.getNamespace());
		if (namespace != null) {
			return namespace;
		}
		if (STRUCTURE_NAMESPACES.get().contains(setId.getNamespace())) {
			return STRUCTURE_DISTANCE.get();
		}
		return 1.0;
	}

	// ---------------------------------------------------------------- flowing fluids

	/** Whether Flowing Fluids may seal generated fluids with blocks; true (its own default) until the config has loaded. */
	public static boolean plugFluidsDuringWorldGen() {
		return !SPEC.isLoaded() || FLOWING_FLUIDS_PLUG_WORLDGEN.get();
	}

	// ---------------------------------------------------------------- parsing

	private static boolean isValidOverride(Object o) {
		return o instanceof String s && parseEntry(s) != null;
	}

	private static boolean isValidFactorEntry(Object o) {
		if (!(o instanceof String s)) {
			return false;
		}
		int eq = s.lastIndexOf('=');
		if (eq <= 0 || eq == s.length() - 1) {
			return false;
		}
		try {
			return Double.parseDouble(s.substring(eq + 1).trim()) > 0.0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static Map.Entry<ResourceLocation, Integer> parseEntry(String raw) {
		int eq = raw.lastIndexOf('=');
		if (eq <= 0 || eq == raw.length() - 1) {
			return null;
		}
		ResourceLocation id = ResourceLocation.tryParse(raw.substring(0, eq).trim());
		if (id == null) {
			return null;
		}
		try {
			int slots = Integer.parseInt(raw.substring(eq + 1).trim());
			return slots < 0 ? null : Map.entry(id, slots);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Map<ResourceLocation, Integer> parseOverrides(List<? extends String> raw) {
		Map<ResourceLocation, Integer> parsed = new HashMap<>();
		for (String entry : raw) {
			Map.Entry<ResourceLocation, Integer> e = parseEntry(entry);
			if (e == null) {
				BeyondTheHorizonsFixes.LOGGER.warn("Ignoring invalid itemOverrides entry '{}'", entry);
			} else {
				parsed.put(e.getKey(), e.getValue());
			}
		}
		return Map.copyOf(parsed);
	}

	private static void parseStructureOverrides(List<? extends String> raw) {
		Map<String, Double> namespaces = new HashMap<>();
		Map<ResourceLocation, Double> sets = new HashMap<>();
		for (String entry : raw) {
			if (!isValidFactorEntry(entry)) {
				BeyondTheHorizonsFixes.LOGGER.warn("Ignoring invalid structures.overrides entry '{}'", entry);
				continue;
			}
			int eq = entry.lastIndexOf('=');
			String key = entry.substring(0, eq).trim();
			double factor = Double.parseDouble(entry.substring(eq + 1).trim());
			if (key.contains(":")) {
				ResourceLocation id = ResourceLocation.tryParse(key);
				if (id == null) {
					BeyondTheHorizonsFixes.LOGGER.warn("Ignoring invalid structures.overrides entry '{}'", entry);
				} else {
					sets.put(id, factor);
				}
			} else {
				namespaces.put(key, factor);
			}
		}
		structureNamespaceOverrides = Map.copyOf(namespaces);
		structureSetOverrides = Map.copyOf(sets);
	}
}
