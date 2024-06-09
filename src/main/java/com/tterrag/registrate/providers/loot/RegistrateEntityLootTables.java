package com.tterrag.registrate.providers.loot;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Generated;

import com.tterrag.registrate.AbstractRegistrate;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.packs.VanillaEntityLoot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RegistrateEntityLootTables extends VanillaEntityLoot implements RegistrateLootTables {

    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateEntityLootTables> callback;

    public RegistrateEntityLootTables(HolderLookup.Provider p_346214_, AbstractRegistrate<?> parent, Consumer<RegistrateEntityLootTables> callback) {
        super(p_346214_);
        this.parent = parent;
        this.callback = callback;
    }

    @Override
    public void generate() {
        callback.accept(this);
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return parent.getAll(Registries.ENTITY_TYPE).stream().map(Supplier::get);
    }

    // @formatter:off
    // GENERATED START - DO NOT EDIT BELOW THIS LINE

    /** Generated override to expose protected method: {@link EntityLootSubProvider#createSheepTable} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Fri, 9 Jun 2023 03:55:24 GMT")
    public static LootTable.Builder createSheepTable(ItemLike p_249422_) { return EntityLootSubProvider.createSheepTable(p_249422_); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#canHaveLootTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Fri, 9 Jun 2023 03:55:24 GMT")
    public boolean canHaveLootTable(EntityType<?> p_249029_) { return super.canHaveLootTable(p_249029_); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#killedByFrogVariant} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Fri, 9 Jun 2023 03:55:24 GMT")
    public LootItemCondition.Builder killedByFrogVariant(ResourceKey<FrogVariant> p_249403_) { return super.killedByFrogVariant(p_249403_); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Fri, 9 Jun 2023 03:55:24 GMT")
    public void add(EntityType<?> p_248740_, LootTable.Builder p_249440_) { super.add(p_248740_, p_249440_); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Fri, 9 Jun 2023 03:55:24 GMT")
    public void add(EntityType<?> p_252130_, ResourceKey<LootTable> p_251706_, LootTable.Builder p_249357_) { super.add(p_252130_, p_251706_, p_249357_); }

    // GENERATED END

    public HolderLookup.Provider getRegistries() {
        return this.registries;
    }
}
