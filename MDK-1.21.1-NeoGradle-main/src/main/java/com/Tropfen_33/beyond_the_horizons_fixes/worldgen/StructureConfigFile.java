package com.Tropfen_33.beyond_the_horizons_fixes.worldgen;

import com.Tropfen_33.beyond_the_horizons_fixes.BeyondTheHorizonsFixes;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * config/beyond_the_horizons_fixes-structures.toml: one distance multiplier per structure set.
 *
 * Which structure sets exist depends on the installed mods, so this file cannot be declared up front like
 * the normal config. It is (re)written every time a server starts: sets that are missing get a line
 * with the default 1.0, values you edited are kept, and sets of removed mods are left alone.
 */
public final class StructureConfigFile {
	private static final String FILE_NAME = "beyond_the_horizons_fixes-structures.toml";

	private StructureConfigFile() {
	}

	public static Map<ResourceLocation, Double> syncAndRead(Collection<ResourceLocation> allIds) {
		Map<ResourceLocation, Double> result = new HashMap<>();
		Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);

		try (CommentedFileConfig config = CommentedFileConfig.builder(path).sync().build()) {
			config.load();

			if (!config.contains(List.of("_info"))) {
				config.set(List.of("_info"), "see comment");
				config.setComment(List.of("_info"),
						" Distance multiplier per structure set. 1 = unchanged, 2 = twice as far apart (1/4 as many), 0.5 = half as far apart.\n"
								+ " No upper limit. Multiplied with the multipliers from beyond_the_horizons_fixes-common.toml [structures].\n"
								+ " Only affects chunks generated after the change. New structure sets are added automatically at world load.");
			}

			boolean changed = false;
			List<ResourceLocation> sorted = new ArrayList<>(allIds);
			sorted.sort(Comparator.comparing(ResourceLocation::toString));

			for (ResourceLocation id : sorted) {
				List<String> key = List.of(id.toString());
				if (!config.contains(key)) {
					config.set(key, 1.0);
					changed = true;
				}
				Object value = config.get(key);
				if (value instanceof Number number && number.doubleValue() > 0.0) {
					result.put(id, number.doubleValue());
				} else {
					BeyondTheHorizonsFixes.LOGGER.warn("Ignoring invalid value '{}' for structure set {} in {}", value, id, FILE_NAME);
				}
			}

			if (changed) {
				config.save();
			}
		} catch (RuntimeException e) {
			BeyondTheHorizonsFixes.LOGGER.error("Could not read or write {}; per-structure multipliers are ignored", FILE_NAME, e);
			return Map.of();
		}
		return result;
	}
}
