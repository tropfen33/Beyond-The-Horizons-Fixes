package com.Tropfen_33.beyond_the_horizons_fixes.config;

import com.Tropfen_33.beyond_the_horizons_fixes.BeyondTheHorizonsFixes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FixesConfig {
	public static final ModConfigSpec SPEC;

	private static final ModConfigSpec.IntValue BACKPACK_MAX_SLOTS_PER_ITEM;
	private static final ModConfigSpec.ConfigValue<List<? extends String>> BACKPACK_ITEM_OVERRIDES;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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

		SPEC = builder.build();
	}

	/** Parsed copy of the overrides, rebuilt whenever the config loads or reloads. */
	private static volatile Map<ResourceLocation, Integer> overrides = Map.of();

	private FixesConfig() {
	}

	public static void onConfigEvent(ModConfigEvent event) {
		if (event.getConfig().getSpec() == SPEC) {
			overrides = parseOverrides(BACKPACK_ITEM_OVERRIDES.get());
		}
	}

	/** Slot limit for the given item id; 0 means unlimited. */
	public static int getBackpackSlotLimit(ResourceLocation itemId) {
		Integer override = overrides.get(itemId);
		if (override != null) {
			return override;
		}
		return SPEC.isLoaded() ? BACKPACK_MAX_SLOTS_PER_ITEM.get() : 0;
	}

	private static boolean isValidOverride(Object o) {
		return o instanceof String s && parseEntry(s) != null;
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
}
