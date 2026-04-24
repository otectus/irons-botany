package com.ironsbotany.common.spell;

import com.ironsbotany.IronsBotany;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import com.ironsbotany.common.registry.IBSchools;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import com.ironsbotany.common.spell.catalyst.SpellContext;

import java.util.List;

public class ManaBloomSpell extends AbstractBotanicalSpell {

    private static final String[] FLOWER_COLORS = {
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
        "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
    };

    public ManaBloomSpell() {
        super(10000, 10000); // 10k base, +10k per level Botania mana
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 20;
        this.baseManaCost = 20;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.COMMON)
                .setSchoolResource(IBSchools.BOTANY_RESOURCE)
                .setMaxLevel(5)
                .setCooldownSeconds(30)
                .build();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("spell.ironsbotany.mana_bloom.description"));
    }

    @Override
    protected void executeBotanicalEffect(Level level, int spellLevel, LivingEntity entity,
                                                 CastSource castSource, MagicData playerMagicData,
                                                 SpellContext context) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos center = entity.blockPosition();

        // Apply range modification from context
        int baseRadius = 2 + spellLevel;
        int radius = (int) context.getModifiedRange(baseRadius);

        int flowerCount = 5 + (spellLevel * 2);

        for (int i = 0; i < flowerCount; i++) {
            BlockPos randomPos = center.offset(
                    level.random.nextInt(radius * 2) - radius,
                    0,
                    level.random.nextInt(radius * 2) - radius
            );

            // Find valid ground
            while (randomPos.getY() > level.getMinBuildHeight() &&
                    !level.getBlockState(randomPos).isSolidRender(level, randomPos)) {
                randomPos = randomPos.below();
            }

            randomPos = randomPos.above();

            if (level.getBlockState(randomPos).isAir() &&
                    level.getBlockState(randomPos.below()).isSolidRender(level, randomPos.below())) {

                // Spawn Botania mystical flower (fallback to vanilla poppy)
                Block flower = getRandomBotaniaFlower(level.random);
                level.setBlock(randomPos, flower.defaultBlockState(), 3);

                // Particle effect — vanilla swirl + mod petals for identity
                serverLevel.sendParticles(
                        ParticleTypes.EFFECT,
                        randomPos.getX() + 0.5, randomPos.getY() + 0.5, randomPos.getZ() + 0.5,
                        10, 0.3, 0.3, 0.3, 0.1
                );
                if (com.ironsbotany.common.config.CommonConfig.ENABLE_SPELL_PARTICLES.get()) {
                    serverLevel.sendParticles(
                            com.ironsbotany.common.registry.IBParticles.PETAL_MAGIC.get(),
                            randomPos.getX() + 0.5, randomPos.getY() + 0.6, randomPos.getZ() + 0.5,
                            6, 0.25, 0.35, 0.25, 0.02
                    );
                }
            }
        }
    }

    private static Block getRandomBotaniaFlower(net.minecraft.util.RandomSource random) {
        if (ModList.get().isLoaded("botania")) {
            String color = FLOWER_COLORS[random.nextInt(FLOWER_COLORS.length)];
            ResourceLocation rl = ResourceLocation.tryParse("botania:" + color + "_mystical_flower");
            if (rl != null) {
                Block flower = ForgeRegistries.BLOCKS.getValue(rl);
                if (flower != null && flower != Blocks.AIR) {
                    return flower;
                }
            }
        }
        return Blocks.POPPY;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return new ResourceLocation(IronsBotany.MODID, "mana_bloom");
    }
}
