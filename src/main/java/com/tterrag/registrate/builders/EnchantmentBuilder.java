package com.tterrag.registrate.builders;

import java.util.Arrays;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * A builder for enchantments, allows for customization of the {@link EquipmentSlot equipment slots}, and configuration of data associated with
 * enchantments (lang).
 * 
 * @param <T>
 *            The type of enchantment being built
 * @param <P>
 *            Parent object type
 */
public class EnchantmentBuilder<T extends Enchantment, P> extends AbstractBuilder<Enchantment, T, P, EnchantmentBuilder<T, P>> {

    @FunctionalInterface
    public interface EnchantmentFactory<T extends Enchantment> {
        
        T create(Enchantment.EnchantmentDefinition definition);
    }

    /**
     * Create a new {@link EnchantmentBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The enchantment will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * </ul>
     * 
     * @param <T>
     *            The type of the builder
     * @param <P>
     *            Parent object type
     * @param owner
     *            The owning {@link AbstractRegistrate} object
     * @param parent
     *            The parent object
     * @param name
     *            Name of the entry being built
     * @param callback
     *            A callback used to actually register the built entry
     * @return A new {@link EnchantmentBuilder} with reasonable default data generators.
     */
    public static <T extends Enchantment, P> EnchantmentBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, Enchantment.EnchantmentDefinition definition, EnchantmentFactory<T> factory) {
        return new EnchantmentBuilder<>(owner, parent, name, callback, definition, factory)
                .defaultLang();
    }

    private final Enchantment.EnchantmentDefinition definition;
    private final EnchantmentFactory<T> factory;

    protected EnchantmentBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, Enchantment.EnchantmentDefinition definition, EnchantmentFactory<T> factory) {
        super(owner, parent, name, callback, Registries.ENCHANTMENT);
        this.factory = factory;
        this.definition = definition;
    }

    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#getAutomaticName(NonNullSupplier, net.minecraft.resources.ResourceKey)}. This is the default, so it is generally not necessary to call, unless for
     * undoing previous changes.
     * 
     * @return this {@link EnchantmentBuilder}
     */
    public EnchantmentBuilder<T, P> defaultLang() {
        return lang(Enchantment::getDescriptionId);
    }

    /**
     * Set the translation for this enchantment.
     * 
     * @param name
     *            A localized English name
     * @return this {@link EnchantmentBuilder}
     */
    public EnchantmentBuilder<T, P> lang(String name) {
        return lang(Enchantment::getDescriptionId, name);
    }

    @Override
    protected @NonnullType T createEntry() {
        return factory.create(definition);
    }
}
