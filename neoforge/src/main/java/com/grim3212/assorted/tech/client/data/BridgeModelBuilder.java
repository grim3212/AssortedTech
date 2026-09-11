package com.grim3212.assorted.tech.client.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.grim3212.assorted.tech.client.model.BridgeUnbakedModel;
import net.minecraft.resources.Identifier;
import com.grim3212.assorted.lib.client.data.LibCustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Writes the {@code assortedtech:bridge} loader block into a bridge block model json: a
 * {@code parent} and the {@code textures} map {@link BridgeUnbakedModel.Loader} reads. The loader
 * replaces the geometry, so it takes no inline elements.
 */
public class BridgeModelBuilder extends LibCustomLoaderBuilder {

    public static BridgeModelBuilder begin() {
        return new BridgeModelBuilder();
    }

    private @Nullable Identifier bridge;
    private final Map<String, Identifier> textures = Maps.newLinkedHashMap();

    protected BridgeModelBuilder() {
        super(BridgeUnbakedModel.LOADER_NAME, false);
    }

    /**
     * The model whose shape and remaining texture slots the bridge inherits.
     */
    public BridgeModelBuilder bridge(Identifier bridge) {
        Preconditions.checkNotNull(bridge, "bridge must not be null");
        this.bridge = bridge;
        return this;
    }

    /**
     * A texture slot filled in on the inherited model. The slot name carries no leading {@code #}:
     * that marks a reference to another slot, and {@code TextureSlots#getMaterial} strips it before
     * looking a slot up, so {@code "#stored"} would declare a slot nothing could ever find.
     */
    public BridgeModelBuilder addTexture(String name, Identifier texture) {
        Preconditions.checkNotNull(texture, "texture must not be null");
        this.textures.put(name, texture);
        return this;
    }

    @Override
    protected CustomLoaderBuilder copyInternal() {
        BridgeModelBuilder copy = new BridgeModelBuilder();
        copy.bridge = this.bridge;
        copy.textures.putAll(this.textures);
        return copy;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        Preconditions.checkNotNull(bridge, "bridge must not be null");

        JsonObject bridgeObj = new JsonObject();
        bridgeObj.addProperty("parent", bridge.toString());

        if (!this.textures.isEmpty()) {
            JsonObject textureObj = new JsonObject();
            this.textures.forEach((k, v) -> textureObj.addProperty(k, v.toString()));
            bridgeObj.add("textures", textureObj);
        }

        json.add("bridge", bridgeObj);
        return json;
    }
}
