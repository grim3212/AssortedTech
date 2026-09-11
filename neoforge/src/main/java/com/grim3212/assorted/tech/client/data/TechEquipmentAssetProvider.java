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
 * Writes an {@code equipment_asset} for every armour material; without one the armour renders
 * untextured and nothing warns. Vanilla's {@code EquipmentAssetProvider} hardcodes its own
 * materials, so it cannot be extended. The only armour is boots, so only the main humanoid layer is
 * declared.
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
