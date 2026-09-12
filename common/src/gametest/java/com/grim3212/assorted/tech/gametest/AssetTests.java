package com.grim3212.assorted.tech.gametest;

import net.minecraft.locale.Language;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.MinecraftServer;
import com.grim3212.assorted.lib.platform.Services;
import java.io.BufferedReader;
import java.io.IOException;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.common.handlers.TechCreativeItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.grim3212.assorted.lib.test.TestSupport.*;
import static com.grim3212.assorted.tech.gametest.TechTestSupport.*;

/**
 * What the mod ships: models and names, the bridge item model, loader keys both loaders read, and recipes that load.
 */
final class AssetTests {

    private AssetTests() {
    }

    static void register(BiConsumer<String, Consumer<GameTestHelper>> out) {
        out.accept("assets_have_models_and_names", AssetTests::assetsHaveModelsAndNames);
        out.accept("bridge_item_draws_its_stored_block", AssetTests::bridgeItemDrawsItsStoredBlock);
        out.accept("loader_models_are_read_on_both_loaders", AssetTests::loaderModelsAreReadOnBothLoaders);
        out.accept("every_recipe_loads_or_is_conditioned_off", AssetTests::everyRecipeLoadsOrIsConditionedOff);
        out.accept("every_item_tag_has_a_name", AssetTests::everyItemTagHasAName);
    }

    /**
     * Every block and item has a model and an English name, and the creative tab exists. Every gap
     * is reported at once.
     */
    private static void assetsHaveModelsAndNames(GameTestHelper helper) {
        JsonObject lang = readJson("/assets/" + Constants.MOD_ID + "/lang/en_us.json");
        helper.assertTrue(lang != null, "assets/" + Constants.MOD_ID + "/lang/en_us.json is not on the classpath");

        List<String> missing = new ArrayList<>();
        int blocks = 0;
        int items = 0;

        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            Identifier id = entry.getKey().identifier();
            if (!Constants.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            blocks++;
            if (!resourceExists("/assets/" + id.getNamespace() + "/blockstates/" + id.getPath() + ".json")) {
                missing.add("blockstates/" + id.getPath() + ".json");
            }
            String key = entry.getValue().getDescriptionId();
            if (!lang.has(key)) {
                missing.add("lang key " + key + " (block " + id + ")");
            }
        }

        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            Identifier id = entry.getKey().identifier();
            if (!Constants.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            items++;
            if (!resourceExists("/assets/" + id.getNamespace() + "/items/" + id.getPath() + ".json")) {
                missing.add("items/" + id.getPath() + ".json");
            }
            String key = entry.getValue().getDescriptionId();
            if (!lang.has(key)) {
                missing.add("lang key " + key + " (item " + id + ")");
            }
        }

        if (!BuiltInRegistries.CREATIVE_MODE_TAB.containsKey(TechCreativeItems.CREATIVE_TAB_KEY)) {
            missing.add("the " + TechCreativeItems.CREATIVE_TAB_KEY.identifier() + " creative tab");
        }
        if (!lang.has("itemGroup." + Constants.MOD_ID)) {
            missing.add("lang key itemGroup." + Constants.MOD_ID);
        }

        // Guards against the whole walk passing because the registries came back empty.
        helper.assertTrue(blocks > 40, "only " + blocks + " blocks are registered under " + Constants.MOD_ID);
        helper.assertTrue(items > 40, "only " + items + " items are registered under " + Constants.MOD_ID);
        helper.assertTrue(missing.isEmpty(), missing.size() + " missing asset(s) across " + blocks + " blocks and "
                + items + " items: " + String.join(", ", missing));

        helper.succeed();
    }

    /**
     * The bridge item draws the block it holds: {@code items/bridge.json} must name the {@code
     * assortedtech:bridge} item model type, as a plain {@code minecraft:model} bakes the bridge
     * once, empty, and nothing warns. {@code TechClientGameTests} checks what it draws.
     */
    private static void bridgeItemDrawsItsStoredBlock(GameTestHelper helper) {
        JsonObject json = readJson("/assets/" + Constants.MOD_ID + "/items/bridge.json");
        helper.assertTrue(json != null, "assets/" + Constants.MOD_ID + "/items/bridge.json is not on the classpath");

        JsonObject model = json.getAsJsonObject("model");
        helper.assertValueEqual(model.get("type").getAsString(), Constants.MOD_ID + ":bridge", "item model type of the bridge");
        helper.assertValueEqual(model.get("model").getAsString(), Constants.MOD_ID + ":block/bridge", "block model the bridge item draws");
        helper.assertTrue(model.has("tints") && model.getAsJsonArray("tints").size() == 1
                        && (Constants.MOD_ID + ":bridge").equals(model.getAsJsonArray("tints").get(0).getAsJsonObject().get("type").getAsString()),
                "the bridge item lost its " + Constants.MOD_ID + ":bridge tint: " + model.get("tints"));

        helper.succeed();
    }

    /**
     * Every custom blockstate model and loader model carries both loaders' keys: NeoForge reads
     * {@code "type"} and {@code "loader"}, Fabric only {@code "fabric:type"}. With only NeoForge's
     * key, Fabric loads a plain static model and nothing warns.
     */
    private static void loaderModelsAreReadOnBothLoaders(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        List<String> checkedModels = new ArrayList<>();
        int customVariants = 0;

        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            Identifier id = entry.getKey().identifier();
            if (!Constants.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            JsonObject blockstate = readJson("/assets/" + id.getNamespace() + "/blockstates/" + id.getPath() + ".json");
            if (blockstate == null) {
                continue;
            }

            for (JsonObject variant : blockstateVariants(blockstate)) {
                if (variant.has("type")) {
                    customVariants++;
                    if (!variant.get("type").equals(variant.get("fabric:type"))) {
                        wrong.add("blockstate " + id.getPath() + " has type " + variant.get("type") + " but fabric:type " + variant.get("fabric:type"));
                    }
                }

                String model = variant.has("model") ? variant.get("model").getAsString() : null;
                if (model == null || checkedModels.contains(model) || !model.startsWith(Constants.MOD_ID + ":")) {
                    continue;
                }
                checkedModels.add(model);

                Identifier modelId = Identifier.parse(model);
                JsonObject json = readJson("/assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json");
                if (json != null && json.has("loader") && !json.get("loader").equals(json.get("fabric:type"))) {
                    wrong.add("model " + model + " has loader " + json.get("loader") + " but fabric:type " + json.get("fabric:type"));
                }
            }
        }

        helper.assertTrue(customVariants > 0, "no custom blockstate variants were found to check");
        helper.assertTrue(wrong.isEmpty(), wrong.size() + " json(s) Fabric would read as static: " + String.join("; ", wrong));
        helper.succeed();
    }

    /**
     * Every recipe file either loaded or was skipped by this loader's own load conditions; anything
     * else failed to parse.
     */
    private static void everyRecipeLoadsOrIsConditionedOff(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        FileToIdConverter recipes = FileToIdConverter.json("recipe");
        String conditionsKey = Services.PLATFORM.getPlatformName().equals("Fabric") ? "fabric:load_conditions" : "neoforge:conditions";
        List<String> failed = new ArrayList<>();

        recipes.listMatchingResources(server.getResourceManager()).forEach((file, resource) -> {
            Identifier id = recipes.fileToId(file);
            if (!id.getNamespace().equals(Constants.MOD_ID) || server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, id)).isPresent()) {
                return;
            }

            try (BufferedReader reader = resource.openAsReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (!json.has(conditionsKey)) {
                    failed.add(id.toString());
                }
            } catch (IOException e) {
                failed.add(id + " (" + e.getMessage() + ")");
            }
        });

        helper.assertTrue(failed.isEmpty(), failed.size() + " recipes failed to load without being conditioned off: " + String.join(", ", failed.subList(0, Math.min(10, failed.size()))));
        helper.succeed();
    }

    /**
     * Every non-vanilla item tag has a {@code tag.item.<namespace>.<path>} name, the check Fabric
     * API warns about at dev startup. Both loaders name the standard c: tags, so anything missing
     * is ours.
     */
    private static void everyItemTagHasAName(GameTestHelper helper) {
        Language language = Language.getInstance();
        List<String> missing = helper.getLevel().registryAccess().lookupOrThrow(Registries.ITEM).getTags()
                .map(tag -> tag.key().location())
                .filter(id -> !"minecraft".equals(id.getNamespace()))
                .map(id -> "tag.item." + id.getNamespace() + "." + id.getPath().replace('/', '.'))
                .filter(key -> !language.has(key))
                .sorted()
                .toList();
        helper.assertTrue(missing.isEmpty(), "item tags with no name in any lang file: " + missing);
        helper.succeed();
    }
}
