# AssortedTech — in-world testing checklist

Manual checks only. Anything automatable lives in `common/src/gametest/java/.../gametest/`
and runs with `./gradlew :neoforge:runGameTestServer` / `:fabric:runGameTest`.

Run each list on **both** NeoForge and Fabric.

## Gravity blocks
- [ ] The action bar shows the new range when you cycle it
- [ ] Right-clicking with a redstone torch toggles the range overlay
- [ ] Range boxes draw red or green
- [ ] Gravity boots make the wearer immune to a gravitor

## Bridges
- [ ] A projected bridge is drawn with the texture set on its control
- [ ] A bridge item holding a block shows that block's texture (give one with a command;
      nothing writes one in normal play)
- [ ] The stored texture's alpha picks the layer you expect (a glass bridge is see-through)
- [ ] Breaking a controller removes its bridge, and a broken segment is repaired on the next
      controller tick
- [ ] On a dedicated server, a second player sees a bridge appear and disappear with its control,
      with no ghost segments left behind
- [ ] A bridge projected as stone darkens the room behind it and casts a shadow on the client too,
      and one projected as glowstone glows. The gametests only watch the server's light engine, and
      the client sees the `light_dampening` state through a block update

## Sensors, spikes, fans, alarms
- [ ] On a dedicated server, a sensor lights up for a second player when it detects something
- [ ] Each sensor's tooltip names what it detects
- [ ] Spike tooltip shows its damage
- [ ] Fan screen opens and its mode and range stick
- [ ] Alarm screen opens, its help text is legible, and the chosen sound plays on a signal
- [ ] Air particles appear in front of a running fan
- [ ] Spikes draw cutout

## Torches
- [ ] Both torches draw their particles
- [ ] Both torches draw cutout, and the flip-flop torch cycles

## Rendering
- [ ] Nothing a renderer draws is subtly off. Sprite UVs now take 0-1 and ARGB colours no longer
      force alpha, so a mistake shows as the wrong texture region or a missing tint, not a crash
