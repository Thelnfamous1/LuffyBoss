package me.infamous.luffy_boss;

import me.infamous.luffy_boss.common.registry.LBEntityTypes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LuffyBoss.MODID)
public class LuffyBoss {
    public static final String MODID = "luffy_boss";
    public static final Logger LOGGER = LogManager.getLogger();

    public LuffyBoss() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        LBEntityTypes.register(modEventBus);
    }
}
