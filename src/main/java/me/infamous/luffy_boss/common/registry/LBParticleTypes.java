package me.infamous.luffy_boss.common.registry;

import me.infamous.luffy_boss.LuffyBoss;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class LBParticleTypes {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, LuffyBoss.MODID);

    public static final RegistryObject<BasicParticleType> BIG_SOUL_FIRE_FLAME = PARTICLE_TYPES.register("big_soul_fire_flame",
            () -> new BasicParticleType(false));

    public static void register(IEventBus modEventBus){
        PARTICLE_TYPES.register(modEventBus);
    }
}
