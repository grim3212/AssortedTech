package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.lib.data.CrossLoaderData;
import com.grim3212.assorted.tech.Constants;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * One generated tree serves both loaders, so every file must carry Fabric's condition and
 * ingredient keys exactly as AssortedLib's {@link CrossLoaderData} derives them from NeoForge's.
 */
final class CrossLoaderDataTests {

    private CrossLoaderDataTests() {
    }

    static void register(BiConsumer<String, Consumer<GameTestHelper>> out) {
        out.accept("cross_loader_data_matches_translator", CrossLoaderDataTests::dataMatchesTranslator);
    }

    private static void dataMatchesTranslator(GameTestHelper helper) {
        List<String> problems = CrossLoaderData.mismatches(helper.getLevel().getServer().getResourceManager(), Constants.MOD_ID);
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }
}
