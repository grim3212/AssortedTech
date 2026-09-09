# AssortedTech 26.2 — shared notes

Read `../PORTING-26.2.md` and `../UPGRADE-GUIDE-1.20.1-to-26.2.md` first — the guide now has five
mods' worth of hard-won detail in it, including Steps 3b/3c/3d which were written from the most
recent port. This file only covers what is specific to this mod.

**The policy in `PORTING-26.2.md` binds: no deprecated API, no back-compat or conversion paths.**

This is the sixth and last mod. The other five are ported and are the best reference you have —
when you hit something, grep `AssortedCore`, `AssortedTools`, `AssortedStorage`, `AssortedDecor` and
`AssortedWorld` for how it was already solved rather than inventing a second answer.

## Build

```bash
cd D:/Development/MinecraftModding/AssortedTech
./gradlew :common:compileJava -PcommonOnly --console=plain > /tmp/<yourname>.txt 2>&1; echo "EXIT=$?"
```

`-PcommonOnly` narrows `settings.gradle` to the `common` module. **Never edit `settings.gradle`** —
other agents build at the same time. Never pipe gradle into `tail`; the pipe eats the exit code.
Do not combine `-PcommonOnly` with `:fabric:` or `:neoforge:` targets — the flag excludes them.

## Already done — do not redo

- `api/util/TechArmorMaterials` — a plain enum building an `ArmorMaterial` record.
- `common/item/GravityArmorItem` — a plain `Item` using `Properties#humanoidArmor`.
- The armour texture moved out of the `assets/minecraft` override into
  `assets/assortedtech/textures/entity/equipment/humanoid/gravity.png`.
- Build files, `neoforge.mods.toml`, `fabric.mod.json`, access wideners reset (originals backed up
  in `porting-tools/*.bak`), and all three mechanical scripts run.

## This mod has no mixins

There is no `.mixins.json` anywhere in it and there never was. If you think you need one, you are
probably about to reimplement something that has a data-driven answer now — say so in your report
instead.

## Registration needs ids before construction

Both blocks and items must know their id before they are built (`Properties.setId(ResourceKey)`,
mandatory since 1.21.2). Copy the pattern from
`AssortedCore/common/.../common/items/CoreItems.java` and its blocks equivalent. Call sites change
from `register("x", () -> new XBlock(props))` to `register("x", props -> new XBlock(props))`.

## Things this mod will hit, with where they were already solved

| What | Where it was solved before |
|---|---|
| `BlockBehaviour` methods now `protected` (`rotate`, `mirror`, `canSurvive`, `updateShape`) | every mod's blocks |
| `updateShape` signature change | AssortedDecor blocks |
| Block entity save/load on `ValueOutput`/`ValueInput` | AssortedCore `blockentity/` |
| `CompoundTag` getters returning `Optional` | AssortedTools `WandCoord3D` |
| `Level.isClientSide` private / `Level.random` protected | everywhere - use `isClientSide()`, `getRandom()` |
| `CreativeModeTab.Output` protected -> `Services.PLATFORM.modifyCreativeTab` | all five mods' `*CreativeItems` |
| `DamageSources#source` private | AssortedTools `api/util/ToolsDamageSources` |
| Custom payloads -> `CustomPacketPayload` via `LibPayload` | AssortedCore, AssortedStorage |
| `BakedModel`/`UnbakedModel` -> `BlockStateModel`/`ItemModel` | AssortedStorage, AssortedDecor |
| Screens: retained-mode GUI, `GuiGraphics` changes | AssortedCore screens |
| Recipe providers -> `RecipeProvider.Runner` + `RecipeOutput` + `ResourceKey<Recipe<?>>` | AssortedTools `data/ToolsRecipes` |
| Tag providers -> `TagAppender` + a local tagger record | AssortedCore `CoreBlockTagProvider` |
| Armour equipment assets | AssortedTools `ToolsEquipmentAssetProvider` |

## Verify, do not guess

Decompiled 26.2 sources are extracted at
`C:/Users/dakot/AppData/Local/Temp/claude/D--Development-MinecraftModding/f6e49d58-fab2-4067-a529-7eff5f84a481/scratchpad/mcsrc`
and the full jar is
`C:/Users/dakot/.gradle/caches/neoformruntime/intermediate_results/decompile_743224902f1768459894187086c68d3fb30d272a_output.jar`
— unzip what you need into that directory and read it. Class indexes:
`porting-tools/mc262_classes.txt` (Minecraft), `porting-tools/nf_classes.txt` (NeoForge).

**NeoForge's patch set decides how much loader-specific work is real.** Extract it from the
userdev jar (`unzip -l <jar> | grep 'patches/'`) before concluding that the two loaders need
different code. See Step 3c of the guide.

## Recording your work

Append your section under a `# AssortedTech` heading in `../REVIEW-BEHAVIOUR-CHANGES.md`, using
`##` for your slice — that is how the file is structured. Cover behaviour that now differs, on-disk
format changes, dead code you removed, and every `// TODO(26.2): ...` you left.

Report honestly. Compiling is not working. Do not claim a clean build you did not observe.
