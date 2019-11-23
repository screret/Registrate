package com.tterrag.registrate.providers.loot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootParameterSet;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTable.Builder;
import net.minecraft.world.storage.loot.ValidationResults;
import net.minecraftforge.fml.LogicalSide;

public class RegistrateLootTableProvider extends LootTableProvider implements RegistrateProvider {
    
    public interface LootType<T extends RegistrateLootTables> {
        
        static LootType<RegistrateBlockLootTables> BLOCK = register("block", LootParameterSets.BLOCK, RegistrateBlockLootTables::new);
        
        T getLootCreator(Registrate parent, Consumer<T> callback);
        
        LootParameterSet getLootSet();
        
        static <T extends RegistrateLootTables> LootType<T> register(String name, LootParameterSet set, BiFunction<Registrate, Consumer<T>, T> factory) {
            LootType<T> type = new LootType<T>() {
                @Override
                public T getLootCreator(Registrate parent, Consumer<T> callback) {
                    return factory.apply(parent, callback);
                }
                
                @Override
                public LootParameterSet getLootSet() {
                    return set;
                }
            };
            LOOT_TYPES.put(name, type);
            return type;
        }
    }
    
    private static final Map<String, LootType<?>> LOOT_TYPES = new HashMap<>();
    
    private final Registrate parent;
    
    private final Multimap<LootType<?>, Consumer<? super RegistrateLootTables>> lootActions = HashMultimap.create();
    private final Set<RegistrateLootTables> currentLootCreators = new HashSet<>();

    public RegistrateLootTableProvider(Registrate parent, DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
        this.parent = parent;
    }

    @Override
    public String getName() {
        return "Loot tables";
    }
    
    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }
    
    @Override
    public ProviderType<RegistrateLootTableProvider> getType() {
        return ProviderType.LOOT;
    }
    
    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationResults validationresults) {
        currentLootCreators.forEach(c -> c.validate(map, validationresults));
    }
    
    @SuppressWarnings("unchecked")
    public <T extends RegistrateLootTables> void addLootAction(LootType<T> type, Consumer<T> action) {
        this.lootActions.put(type, (Consumer<? super RegistrateLootTables>) action);
    }
    
    private Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>> getLootCreator(Registrate parent, LootType<?> type) {
        return () -> {
            RegistrateLootTables creator = type.getLootCreator(parent, cons -> lootActions.get(type).forEach(c -> c.accept(cons)));
            currentLootCreators.add(creator);
            return creator;
        };
    }
    
    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> getTables() {
        parent.genData(getType(), this);
        currentLootCreators.clear();
        ImmutableList.Builder<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> builder = ImmutableList.builder();
        for (LootType<?> type : LOOT_TYPES.values()) {
            builder.add(Pair.of(getLootCreator(parent, type), type.getLootSet()));
        }
        return builder.build();
    }
}