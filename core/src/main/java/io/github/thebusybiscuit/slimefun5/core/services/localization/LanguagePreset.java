package io.github.thebusybiscuit.slimefun5.core.services.localization;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.inventory.ItemStack;

/**
 * This enum holds info about a {@link Language} that is embedded in our resources folder.
 * Every enum constant holds the key of that {@link Language} as well as a texture hash
 * for the {@link ItemStack} to display.
 * 
 * @author TheBusyBiscuit
 * 
 * @see Language
 *
 */
public enum LanguagePreset {

    ENGLISH("en", "a1701f21835a898b20759fb30a583a38b994abf60d3912ab4ce9f2311e74f72"),
    PORTUGUESE_BRAZIL("pt-BR", "9a46475d5dcc815f6c5f2859edbb10611f3e861c0eb14f088161b3c0ccb2b0d9"),
    PORTUGUESE_PORTUGAL("pt", false, "ebd51f4693af174e6fe1979233d23a40bb987398e3891665fafd2ba567b5a53a"),
    SPANISH("es", "32bd4521983309e0ad76c1ee29874287957ec3d96f8d889324da8c887e485ea8");

    private final String id;
    private final boolean releaseReady;
    private final String textureHash;
    private final TextDirection textDirection;

    @ParametersAreNonnullByDefault
    LanguagePreset(String id, boolean releaseReady, TextDirection direction, String textureHash) {
        this.id = id;
        this.releaseReady = releaseReady;
        this.textureHash = textureHash;
        this.textDirection = direction;
    }

    @ParametersAreNonnullByDefault
    LanguagePreset(String id, boolean releaseReady, String textureHash) {
        this(id, releaseReady, TextDirection.LEFT_TO_RIGHT, textureHash);
    }

    @ParametersAreNonnullByDefault
    LanguagePreset(String id, TextDirection direction, String textureHash) {
        this(id, true, direction, textureHash);
    }

    @ParametersAreNonnullByDefault
    LanguagePreset(String id, String textureHash) {
        this(id, true, textureHash);
    }

    /**
     * This returns the id of this {@link Language}.
     * 
     * @return The language code
     */
    public @Nonnull String getLanguageCode() {
        return id;
    }

    /**
     * This returns whether this {@link LanguagePreset} is "release-ready".
     * A release-ready {@link Language} will be available in RC builds of Slimefun.
     * 
     * @return Whether this {@link Language} is "release-ready"
     */
    boolean isReadyForRelease() {
        return releaseReady;
    }

    /**
     * This returns the texture hash for this language.
     * This will be the flag of the corresponding country.
     * (Not accurate I know, but better than having all languages
     * look the same by using the same items)
     * 
     * @return The texture hash of this language
     */
    public @Nonnull String getTexture() {
        return textureHash;
    }

    /**
     * This returns the direction of text for
     * this language.
     * 
     * @return The direction of text for this language
     */
    public @Nonnull TextDirection getTextDirection() {
        return textDirection;
    }
}

