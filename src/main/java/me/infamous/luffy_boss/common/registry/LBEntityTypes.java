package me.infamous.luffy_boss.common.registry;

import me.infamous.luffy_boss.LuffyBoss;
import me.infamous.luffy_boss.common.entity.GearFiveLuffy;
import me.infamous.luffy_boss.common.entity.GiantFistEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class LBEntityTypes {

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, LuffyBoss.MODID);

    public static final RegistryObject<EntityType<GearFiveLuffy>> GEAR_FIVE_LUFFY = register("gear_five_luffy",
            EntityType.Builder.of(GearFiveLuffy::new, EntityClassification.MONSTER)
                    .sized(6.0F, 17.0F)
                    .clientTrackingRange(10));

    public static final RegistryObject<EntityType<GiantFistEntity>> GIANT_FIST = register("giant_fist",
            EntityType.Builder.<GiantFistEntity>of(GiantFistEntity::new, EntityClassification.MISC)
                    .sized(0.3125F, 0.3125F)
                    .clientTrackingRange(4)
                    .updateInterval(10));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String pKey, EntityType.Builder<T> pBuilder) {
        return ENTITY_TYPES.register(pKey, () -> pBuilder.build(new ResourceLocation(LuffyBoss.MODID, pKey).toString()));
    }

    public static void register(IEventBus modEventBus){
        ENTITY_TYPES.register(modEventBus);
    }
}
