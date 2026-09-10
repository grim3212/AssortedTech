# AssortedTech — in-world testing checklist

Manual checks only. Anything automatable lives in `common/src/gametest/java/.../gametest/`
and runs with `./gradlew :neoforge:runGameTestServer` / `:fabric:runGameTest`.

Run each list on **both** NeoForge and Fabric.

## Gravity blocks
- [ ] Attractor pulls entities in, repulsor pushes them away, gravitor lifts them
- [ ] The directional variants only act along the face they point at
- [ ] Right-clicking with an empty main hand cycles the range, and the action bar shows it
- [ ] Right-clicking with a redstone torch toggles the range overlay
- [ ] Redstone signal turns them on and off
- [ ] Gravity boots make the wearer immune
- [ ] Range and direction survive a world reload

## Bridges
- [ ] Each bridge control (laser, accel, trick, death, gravity) projects a bridge when powered
- [ ] The bridge takes the texture of the block above/behind the control
- [ ] Slabs and fences are refused; only full cubes project
- [ ] Accel bridge gives Speed, death bridge damages, gravity bridge changes fall behaviour
- [ ] Bridge disappears when the signal is removed

## Sensors, spikes, fans, alarms
- [ ] Each sensor type emits redstone when its material is nearby, and the tooltip names it
- [ ] Spikes damage entities that walk on them, damage scaling with the material
- [ ] Spike tooltip shows its damage
- [ ] Fan screen opens; fan pushes and pulls entities and the range setting sticks
- [ ] Alarm screen opens, its help text is legible, and the chosen sound plays on a signal
- [ ] Air particles appear in front of a running fan

## Torches
- [ ] Glowstone torch and its wall form light up, both with correct names
- [ ] Flip flop torch and its wall form toggle on a redstone pulse, both with correct names
- [ ] Both draw their particles

## Creative
- [ ] The Assorted Tech tab exists and every block/item in it has a model and a name
