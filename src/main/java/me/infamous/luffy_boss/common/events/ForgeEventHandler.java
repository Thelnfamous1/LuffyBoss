package me.infamous.luffy_boss.common.events;

import me.infamous.luffy_boss.LuffyBoss;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = LuffyBoss.MODID)
public class ForgeEventHandler {
    private static boolean DEBUG = false;

    @SubscribeEvent
    static void attackPartEntity(AttackEntityEvent event){
        if(DEBUG && event.getTarget() instanceof PartEntity<?>){
            LuffyBoss.LOGGER.info("Distance to target: {}", event.getPlayer().distanceTo(event.getTarget()));
        }
    }
}
