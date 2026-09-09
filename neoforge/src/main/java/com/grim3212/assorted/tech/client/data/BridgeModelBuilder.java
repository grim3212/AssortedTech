package com.grim3212.assorted.tech.client.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.grim3212.assorted.tech.client.model.BridgeUnbakedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Writes the {@code assortedtech:bridge} loader block into a bridge block model json.
 * <p>
 * Forge's {@code ModelBuilder} / {@code BlockStateProvider} pair is gone, so this is no longer a
 * builder hanging off a {@code BlockModelBuilder} owned by a second {@code ModelProvider}.
 * {@link CustomLoaderBuilder} is still the hook, but it now plugs into
 * {@link ExtendedModelTemplateBuilder#customLoader} and contributes to the json a
 * {@link net.minecraft.client.data.models.model.ModelTemplate} emits, so it is constructed with the
 * loader id plus whether the loader tolerates inline vanilla elements (it does not - it replaces the
 * geometry outright) and it has to be able to deep copy itself, because a {@code ModelTemplate} is
 * immutable.
 * <p>
 * The {@code loader} key and the shape of the {@code bridge} object are unchanged;
 * {@code UnbakedModelParser} still reads {@code loader}, the {@code Identifier} it names is still what
 * {@code ModelEvent.RegisterLoaders} is keyed by, and {@link BridgeUnbakedModel.Loader} still reads a
 * {@code parent} plus a {@code textures} map out of {@code bridge}.
 * <p>
 * The {@code model} / top level {@code textures} branch of the 1.20.1 builder is dropped. It existed
 * to point the loader at an OBJ model - AssortedDecor's colorizer uses that path - and nothing in this
 * mod ever called {@code objModel}, so it was emitting nothing and had no reader on the other side.
 */
public class BridgeModelBuilder extends CustomLoaderBuilder {

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
