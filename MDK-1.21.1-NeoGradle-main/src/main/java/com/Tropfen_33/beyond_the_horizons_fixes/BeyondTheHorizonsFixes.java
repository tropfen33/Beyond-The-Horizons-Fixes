package com.Tropfen_33.beyond_the_horizons_fixes;

import com.Tropfen_33.beyond_the_horizons_fixes.config.FixesConfig;
import com.Tropfen_33.beyond_the_horizons_fixes.worldgen.StructureScaler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BeyondTheHorizonsFixes.MOD_ID)
public class BeyondTheHorizonsFixes {
	public static final String MOD_ID = "beyond_the_horizons_fixes";
	public static final Logger LOGGER = LoggerFactory.getLogger("BeyondTheHorizons-Fixes");

	public BeyondTheHorizonsFixes(IEventBus modEventBus, ModContainer container) {
		// COMMON config lives in config/beyond_the_horizons_fixes-common.toml, which is easy to
		// ship inside a modpack and is read on both client and server.
		container.registerConfig(ModConfig.Type.COMMON, FixesConfig.SPEC, "beyond_the_horizons_fixes-common.toml");
		modEventBus.addListener(FixesConfig::onConfigEvent);

		// Structure distances are applied once per server start, before any chunk is generated.
		NeoForge.EVENT_BUS.addListener(StructureScaler::onServerAboutToStart);
	}
}
