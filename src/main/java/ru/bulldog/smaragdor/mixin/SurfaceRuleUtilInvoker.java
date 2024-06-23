package ru.bulldog.smaragdor.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(SurfaceRuleUtil.class)
public interface SurfaceRuleUtilInvoker {

	@Invoker("getRulesForBiomes")
	static List<MaterialRules.MaterialRule> invokeGetRulesForBiomes(List<Biome> biomes) {
		throw new AssertionError();
	}

	@Invoker("mergeSurfaceRules")
	static MaterialRules.MaterialRule invokeMergeSurfaceRules(RegistryKey<DimensionOptions> dimensionKey, MaterialRules.MaterialRule org, BiomeSource source, List<MaterialRules.MaterialRule> additionalRules) {
		throw new AssertionError();
	}
}
