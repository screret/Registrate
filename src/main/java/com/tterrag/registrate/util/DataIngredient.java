package com.tterrag.registrate.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.ObjectArrays;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.val;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.Nullable;

/**
 * A helper for data generation when using ingredients as input(s) to recipes.<br>
 * It remembers the name of the primary ingredient for use in creating recipe names/criteria.
 * <p>
 * Create an instance of this class with the various factory methods such as {@link #items(ItemLike, ItemLike...)} and {@link #tag(TagKey)}.
 * <p>
 * <strong>This class should not be used for any purpose other than data generation</strong>, it will throw an exception if it is serialized to a packet buffer.
 */
public final class DataIngredient implements ICustomIngredient {
    public static final MapCodec<DataIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("parent").forGetter(val -> val.parent),
            ResourceLocation.CODEC.fieldOf("id").forGetter(val -> val.id),
            ItemPredicate.CODEC.listOf().fieldOf("criteria").forGetter(val -> List.of(val.predicates))
    ).apply(instance, (parent, id, predicates) -> new DataIngredient(parent, id, predicates.toArray(ItemPredicate[]::new))));
    public static final IngredientType<DataIngredient> TYPE = new IngredientType<>(CODEC);

    private interface Excludes {

        void toNetwork(FriendlyByteBuf buffer);
        
        boolean checkInvalidation();
        
        void markValid();
        
        boolean isVanilla();

        ItemStack[] getItems();

        Ingredient.Value[] getValues();
    }

    @Delegate(excludes = Excludes.class)
    private final Ingredient parent;
    @Getter
    private final ResourceLocation id;
    private final Function<RegistrateRecipeProvider, Criterion<InventoryChangeTrigger.TriggerInstance>> criteriaFactory;
    private final ItemPredicate[] predicates;

    private DataIngredient(Ingredient parent, ItemLike item) {
        this.parent = parent;
        this.id = BuiltInRegistries.ITEM.getKey(item.asItem());
        this.criteriaFactory = prov -> RegistrateRecipeProvider.has(item);
        this.predicates = new ItemPredicate[] {ItemPredicate.Builder.item().of(item).build()};
    }
    
    private DataIngredient(Ingredient parent, TagKey<Item> tag) {
        this.parent = parent;
        this.id = tag.location();
        this.criteriaFactory = prov -> RegistrateRecipeProvider.has(tag);
        this.predicates = new ItemPredicate[] {ItemPredicate.Builder.item().of(tag).build()};
    }
    
    private DataIngredient(Ingredient parent, ResourceLocation id, ItemPredicate... predicates) {
        this.parent = parent;
        this.id = id;
        this.criteriaFactory = prov -> RegistrateRecipeProvider.inventoryTrigger(predicates);
        this.predicates = predicates;
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> getCriterion(RegistrateRecipeProvider prov) {
        return criteriaFactory.apply(prov);
    }
    
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T extends ItemLike> DataIngredient items(NonNullSupplier<? extends T> first, NonNullSupplier<? extends T>... others) {
        return items(first.get(), (T[]) Arrays.stream(others).map(Supplier::get).toArray(ItemLike[]::new));
    }

    @SafeVarargs
    public static <T extends ItemLike> DataIngredient items(T first, T... others) {
        return ingredient(Ingredient.of(ObjectArrays.concat(first, others)), first);
    }

    public static DataIngredient stacks(ItemStack first, ItemStack... others) {
        return ingredient(Ingredient.of(ObjectArrays.concat(first, others)), first.getItem());
    }

    public static DataIngredient tag(TagKey<Item> tag) {
        return ingredient(Ingredient.of(tag), tag);
    }
    
    public static DataIngredient ingredient(Ingredient parent, ItemLike required) {
        return new DataIngredient(parent, required);
    }
    
    public static DataIngredient ingredient(Ingredient parent, TagKey<Item> required) {
        return new DataIngredient(parent, required);
    }
    
    public static DataIngredient ingredient(Ingredient parent, ResourceLocation id, ItemPredicate... criteria) {
        return new DataIngredient(parent, id, criteria);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(parent.getItems());
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }
}
