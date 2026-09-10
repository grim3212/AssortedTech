# AssortedTech — in-world testing checklist

Manual checks only. Anything automatable lives in `common/src/gametest/java/.../gametest/`
and runs with `./gradlew :neoforge:runGameTestServer` / `:fabric:runGameTest`.

Run each list on **both** NeoForge and Fabric.

## Gravity blocks
- [ ] The action bar shows the new range when you cycle it
- [ ] Right-clicking with a redstone torch toggles the range overlay

## Bridges
- [ ] A projected bridge is drawn with the texture set on its control

## Sensors, spikes, fans, alarms
- [ ] Each sensor's tooltip names what it detects
- [ ] Spike tooltip shows its damage
- [ ] Fan screen opens and its mode and range stick
- [ ] Alarm screen opens, its help text is legible, and the chosen sound plays on a signal
- [ ] Air particles appear in front of a running fan

## Torches
- [ ] Both torches draw their particles
