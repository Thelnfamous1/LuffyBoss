package me.infamous.luffy_boss.common.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MultipartController<P extends Entity, T extends PartEntity<P>> {
    private final Map<String, T> partsByName;
    private final PartEntity<?>[] parts;
    private final Map<String, PartTicker<P, T>> tickersByName;

    public MultipartController(Map<String, T> partsByName, PartEntity<?>[] parts, Map<String, PartTicker<P, T>> tickersByName){
        this.partsByName = partsByName;
        this.parts = parts;
        this.tickersByName = tickersByName;
    }

    @Nullable
    public T getPart(String name) {
        return this.partsByName.get(name);
    }

    public PartEntity<?>[] getParts() {
        return this.parts;
    }

    public void tick() {
        this.partsByName.forEach((name, part) -> {
            double prevX = part.getX();
            double prevY = part.getY();
            double prevZ = part.getZ();

            PartTicker<P, T> ticker = this.tickersByName.get(name);
            if(ticker != null) ticker.tick(part);

            part.xo = prevX;
            part.yo = prevY;
            part.zo = prevZ;
            part.xOld = prevX;
            part.yOld = prevY;
            part.zOld = prevZ;
        });
    }

    public static class Builder<P extends Entity, T extends PartEntity<P>>{
        private final Map<String, T> partsByName = new HashMap<>();
        private final Map<String, PartTicker<P, T>> tickersByName = new HashMap<>();
        @Nullable
        private Function<T, String> nameProvider;

        public Builder(){
        }

        public Builder<P, T> useNameProvider(Function<T, String> nameGetter){
            this.nameProvider = nameGetter;
            return this;
        }

        public Builder<P, T> addPart(T part){
            if(this.nameProvider == null) throw new UnsupportedOperationException("Cannot add part without a name!");
            return this.addPart(this.nameProvider.apply(part), part);
        }

        public Builder<P, T> addPart(String name, T part){
            this.partsByName.put(name, part);
            return this;
        }

        public Builder<P, T> addPart(String name, T part, PartTicker<P, T> ticker){
            this.partsByName.put(name, part);
            this.tickersByName.put(name, ticker);
            return this;
        }

        public Builder<P, T> universalTicker(PartTicker<P, T> ticker){
            this.partsByName.keySet().forEach(name -> this.tickersByName.put(name, ticker));
            return this;
        }

        public MultipartController<P, T> build(){
            return new MultipartController<>(ImmutableMap.copyOf(this.partsByName), this.partsByName.values().toArray(new PartEntity<?>[0]), ImmutableMap.copyOf(this.tickersByName));
        }
    }

    @FunctionalInterface
    public interface PartTicker<P extends Entity, T extends PartEntity<P>>{
        void tick(T part);
    }
}
