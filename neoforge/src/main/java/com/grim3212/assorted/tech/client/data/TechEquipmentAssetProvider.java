package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.util.TechArmorMaterials;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Writes an {@code equipment_asset} definition for every armour material.
 * <p>
 * Since 1.21.4 the worn-armour texture is not derived from the material's name. An
 * {@code ArmorMaterial} carries a {@link ResourceKey} into the {@code equipment_asset} registry, and
 * the client resolves the layer textures through the JSON that key names. Without these files
 * armour renders untextured on the player - and nothing warns, because a missing equipment asset is
 * not a missing model.
 * <p>
 * This does not extend vanilla's {@code EquipmentAssetProvider}: its bootstrap is private and
 * hardcodes vanilla's own materials, so there is nothing to hook into. The write itself is two
 * lines, which is all that class does either.
 * <p>
 * {@code addMainHumanoidLayer} rather than {@code addHumanoidLayers}: this mod's only armour is a
 * pair of boots, so there was never a {@code _layer_2.png} and declaring a leggings layer would
 * point at a texture that does not exist. The one texture now lives at
 * {@code textures/entity/equipment/humanoid/gravity.png} in the mod's own namespace, rather than
 * squatting in {@code assets/minecraft}.
 */
public class TechEquipmentAssetProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public TechEquipmentAssetProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "equipment");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Map<ResourceKey<EquipmentAsset>, EquipmentClientInfo> assets = new HashMap<>();

        for (TechArmorMaterials material : TechArmorMaterials.values()) {
            assets.put(material.assetId(), EquipmentClientInfo.builder()
                    .addMainHumanoidLayer(Identifier.fromNamespaceAndPath(Constants.MOD_ID, material.getName()), false)
                    .build());
        }

        return DataProvider.saveAll(cache, EquipmentClientInfo.CODEC, this.pathProvider::json, assets);
    }

    @Override
    public String getName() {
        return "Equipment Asset Definitions: " + Constants.MOD_ID;
    }
}
