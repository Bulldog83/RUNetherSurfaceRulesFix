package ru.bulldog.smaragdor.mixin;

import com.mojang.serialization.Codec;
import io.github.uhq_games.regions_unexplored.RegionsUnexplored;
import io.github.uhq_games.regions_unexplored.mixin.access.BiomeSourceAccess;
import io.github.uhq_games.regions_unexplored.mixin.access.NoiseBasedChunkGeneratorAccess;
import io.github.uhq_games.regions_unexplored.mixin.access.NoiseGeneratorSettingsAccess;
import io.github.uhq_games.regions_unexplored.util.AddSurfaceRuleUtil;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AddSurfaceRuleUtil.class)
public abstract class AddSurfaceRuleUtilMixin {

	@Unique
	private static final Logger log = LogManager.getLogger("RUSurfaceRulesFix");

	@Inject(
			method = {"appendSurfaceRule"},
			at = {@At("HEAD")},
			cancellable = true)
	private static void ruSrf$hybridAppendNetherSurfaceRules(DynamicRegistryManager registryAccess, RegistryKey<DimensionOptions> levelStemKey, MaterialRules.MaterialRule ruleSource, CallbackInfo ci) {
		log.debug("--- levelStemKey = {}", levelStemKey);
		if (levelStemKey != DimensionOptions.NETHER) {
			return;
		}

		DimensionOptions levelStem = registryAccess.get(RegistryKeys.DIMENSION).get(levelStemKey);
		log.debug("--- levelStem = {}", levelStem);

		if (levelStem == null) {
			return;
		}

		ChunkGenerator chunkGenerator = levelStem.chunkGenerator();
		boolean hasRegionsUnexploredBiome = chunkGenerator.getBiomeSource().getBiomes().stream().anyMatch(biomeHolder -> (biomeHolder.getKey().orElseThrow()).getValue().getNamespace().equals(RegionsUnexplored.MODID));
		if (hasRegionsUnexploredBiome && chunkGenerator instanceof NoiseChunkGenerator noiseChunkGenerator) {
			ChunkGeneratorSettings noiseGeneratorSettings = ((NoiseBasedChunkGeneratorAccess) chunkGenerator).regions_unexplored$getNoiseGeneratorSettings().value();
			if (noiseGeneratorSettings != null && (Object) noiseGeneratorSettings instanceof NoiseGeneratorSettingsAccess generatorSettingsAccess) {
				List<MaterialRules.MaterialRule> rulesForBiomes = SurfaceRuleUtilInvoker.invokeGetRulesForBiomes(noiseChunkGenerator.getBiomeSource().getBiomes().stream().map(RegistryEntry::value).toList());
				MaterialRules.MaterialRule mergedMaterialRules = SurfaceRuleUtilInvoker.invokeMergeSurfaceRules(levelStemKey, noiseGeneratorSettings.surfaceRule(), noiseChunkGenerator.getBiomeSource(), rulesForBiomes);
				if (mergedMaterialRules == null && !rulesForBiomes.isEmpty()) {
					generatorSettingsAccess.regions_unexplored$setSurfaceRule(MaterialRules.sequence(RegionsUnexplored.getSurfaceRules(noiseGeneratorSettings.surfaceRule()), ruleSource));
					logSuccess(chunkGenerator, levelStemKey);

					ci.cancel();
				} else if (mergedMaterialRules != null) {
					generatorSettingsAccess.regions_unexplored$setSurfaceRule(MaterialRules.sequence(RegionsUnexplored.getSurfaceRules(mergedMaterialRules), ruleSource));
					logSuccess(chunkGenerator, levelStemKey);

					ci.cancel();
				}
			}
		}
	}

	@Unique
	private static void logSuccess(ChunkGenerator chunkGenerator, RegistryKey<DimensionOptions> levelStemKey) {
		Codec<? extends BiomeSource> biomeSourceCodec = ((BiomeSourceAccess)chunkGenerator.getBiomeSource()).regions_unexplored$invokeCodec();
		RegionsUnexplored.LOGGER.info("Loading dimension \"{}\" with biome source: \"{}\".", levelStemKey.getValue(), Registries.BIOME_SOURCE.getId(biomeSourceCodec));
	}
}
