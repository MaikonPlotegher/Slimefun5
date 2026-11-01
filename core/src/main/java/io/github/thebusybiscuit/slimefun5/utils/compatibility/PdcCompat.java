package io.github.thebusybiscuit.slimefun5.utils.compatibility;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey;

/**
 * Modern Persistent-Data (PDC) abstraction.
 * <p>
 * Keyed by Slimefun's {@link NamespacedKey} (converted to {@code org.bukkit.NamespacedKey} via {@link BukkitKeys}).
 * All data operations operate directly on standard Bukkit {@code PersistentDataContainer} (1.14+).
 */
public final class PdcCompat {

    private PdcCompat() {}

    private static final Object UNAVAILABLE = new Object();
    private static final Class<?> DATA_TYPE_CLASS = resolveClass("org.bukkit.persistence.PersistentDataType");
    private static final ConcurrentHashMap<String, Object> DATA_TYPES = new ConcurrentHashMap<>();

    /**
     * @return whether this server exposes the PersistentDataContainer API (always true on 1.21+).
     */
    public static boolean isSupported() {
        return true;
    }

    @Nullable
    private static Class<?> resolveClass(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Compatibility no-op for sandbox checks. */
    public static void keepInMemory(@Nonnull Predicate<Object> holders) {}

    /** Compatibility no-op for sandbox checks. */
    public static void forgetMemory() {}

    @Nullable
    private static PersistentDataContainer container(Object holder) {
        if (holder instanceof PersistentDataHolder) {
            return ((PersistentDataHolder) holder).getPersistentDataContainer();
        }
        Object result = ReflectionCompat.invoke(holder, "getPersistentDataContainer");
        return result instanceof PersistentDataContainer ? (PersistentDataContainer) result : null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T, Z> PersistentDataType<T, Z> dataType(String typeName) {
        if (typeName == null) {
            return null;
        }
        switch (typeName) {
            case "STRING": return (PersistentDataType<T, Z>) PersistentDataType.STRING;
            case "BYTE": return (PersistentDataType<T, Z>) PersistentDataType.BYTE;
            case "SHORT": return (PersistentDataType<T, Z>) PersistentDataType.SHORT;
            case "INTEGER": return (PersistentDataType<T, Z>) PersistentDataType.INTEGER;
            case "LONG": return (PersistentDataType<T, Z>) PersistentDataType.LONG;
            case "FLOAT": return (PersistentDataType<T, Z>) PersistentDataType.FLOAT;
            case "DOUBLE": return (PersistentDataType<T, Z>) PersistentDataType.DOUBLE;
            case "BYTE_ARRAY": return (PersistentDataType<T, Z>) PersistentDataType.BYTE_ARRAY;
            default:
                Object resolved = DATA_TYPES.computeIfAbsent(typeName, PdcCompat::resolveDataType);
                return resolved instanceof PersistentDataType ? (PersistentDataType<T, Z>) resolved : null;
        }
    }

    private static Object resolveDataType(String typeName) {
        if (DATA_TYPE_CLASS == null) {
            return UNAVAILABLE;
        }

        try {
            Object value = DATA_TYPE_CLASS.getField(typeName).get(null);
            return value == null ? UNAVAILABLE : value;
        } catch (Throwable ignored) {
            return UNAVAILABLE;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void set(Object holder, NamespacedKey key, String typeName, Object value) {
        PersistentDataContainer c = container(holder);
        PersistentDataType t = dataType(typeName);
        org.bukkit.NamespacedKey k = (org.bukkit.NamespacedKey) BukkitKeys.toBukkit(key);

        if (c != null && t != null && k != null) {
            c.set(k, t, value);
        }
    }

    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object get(Object holder, NamespacedKey key, String typeName) {
        PersistentDataContainer c = container(holder);
        PersistentDataType t = dataType(typeName);
        org.bukkit.NamespacedKey k = (org.bukkit.NamespacedKey) BukkitKeys.toBukkit(key);

        if (c != null && t != null && k != null) {
            return c.get(k, t);
        }

        return null;
    }

    public static Object getOrDefault(Object holder, NamespacedKey key, String typeName, Object defaultValue) {
        Object value = get(holder, key, typeName);
        return value != null ? value : defaultValue;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean has(Object holder, NamespacedKey key, String typeName) {
        PersistentDataContainer c = container(holder);
        PersistentDataType t = dataType(typeName);
        org.bukkit.NamespacedKey k = (org.bukkit.NamespacedKey) BukkitKeys.toBukkit(key);

        if (c != null && t != null && k != null) {
            return c.has(k, t);
        }

        return false;
    }

    /**
     * Version-safe equality of two holders' PersistentDataContainers.
     */
    public static boolean containersEqual(Object holderA, Object holderB) {
        PersistentDataContainer a = container(holderA);
        PersistentDataContainer b = container(holderB);

        if (a == null || b == null) {
            return a == b;
        }

        return a.equals(b);
    }

    public static void remove(Object holder, NamespacedKey key) {
        PersistentDataContainer c = container(holder);
        org.bukkit.NamespacedKey k = (org.bukkit.NamespacedKey) BukkitKeys.toBukkit(key);

        if (c != null && k != null) {
            c.remove(k);
        }
    }

    // --- Convenience accessors mirroring the dough PersistentDataAPI usage Slimefun relied on ---

    public static void setString(Object holder, NamespacedKey key, String value) {
        set(holder, key, "STRING", value);
    }

    @Nullable
    public static String getString(Object holder, NamespacedKey key) {
        Object value = get(holder, key, "STRING");
        return value instanceof String ? (String) value : null;
    }

    public static Optional<String> getOptionalString(Object holder, NamespacedKey key) {
        return Optional.ofNullable(getString(holder, key));
    }

    public static void setByte(Object holder, NamespacedKey key, byte value) {
        set(holder, key, "BYTE", value);
    }

    public static byte getByte(Object holder, NamespacedKey key) {
        Object value = get(holder, key, "BYTE");
        return value instanceof Byte ? (Byte) value : 0;
    }

    public static boolean hasByte(Object holder, NamespacedKey key) {
        return has(holder, key, "BYTE");
    }

    public static void setInt(Object holder, NamespacedKey key, int value) {
        set(holder, key, "INTEGER", value);
    }

    public static int getInt(Object holder, NamespacedKey key) {
        return getInt(holder, key, 0);
    }

    public static int getInt(Object holder, NamespacedKey key, int defaultValue) {
        Object value = get(holder, key, "INTEGER");
        return value instanceof Integer ? (Integer) value : defaultValue;
    }
}
