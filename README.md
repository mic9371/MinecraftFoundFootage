![Minecraft Found Footage](https://github.com/SpacePotatoee/SPBackrooms-Revamped/blob/master/MinecraftFoundFootage.png)

# SP-Backrooms Revamped: Minecraft 1.21.1 port

This branch ports [SpacePotatoee's Found Footage mod](https://github.com/SpacePotatoee/MinecraftFoundFootage) from Minecraft 1.20.1 to **1.21.1** (Fabric). It is an unofficial port and is not affiliated with the original author.

## Requirements

- Minecraft 1.21.1, Fabric Loader 0.16.10 or newer, Java 21
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [GeckoLib](https://modrinth.com/mod/geckolib) 4.7 or newer
- [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) 2.5.26 or newer
- Bundled in the jar: Veil, Cardinal Components API, MidnightLib

Sodium 0.6.x loads and runs, but with the mod's visuals (deferred lighting, PBR, VHS look) switched off, because Sodium's chunk renderer does not provide what they need yet (plan: [docs/sodium-port.md](docs/sodium-port.md)). Tested with Sodium 0.6.13; Sodium 0.7 and newer break Veil's own Sodium integration, so they are declared incompatible. Iris is incompatible. OptiFine, Phosphor, Create and Modern Industrialization were declared incompatible by the original mod and were not retested. Lithium works.

## Building

```bash
./gradlew build
```

The jar is written to `build/libs/`. `./gradlew runClient` and `./gradlew runServer` start a development client and server (the server runs in `run/server`).

## What changed for 1.21.1

- Networking uses `CustomPayload` records, and block, item and chunk-generator code follows the 1.21 APIs.
- Veil for 1.21.1 no longer has the deferred renderer the mod's visuals are built on. A copy of the 1.20.1 deferred renderer and light system now lives in the mod (`com.sp.render.deferred`), together with its shaders, post pipelines and framebuffer definitions.
- Veil 1.0.0.228 for 1.21.1 has several shader-pipeline bugs that the mod works around with small mixins (`com.sp.mixin.veilfix`): include ordering, integer framebuffer clears. Shaders were adjusted for the rest.
- The game state "blackout" in Level 0 leaves lights dark until the event ends; this is the original behaviour.

## Status

The Backrooms levels (Level 0, 1, 2, Poolrooms, Infinite Field) generate and render correctly in a development client. Gameplay has had little testing, so expect rough edges. Level 324 remains cut from generation, as in the original.

## Credits and license

Original mod by SpacePotato and Herr Chaos (see `fabric.mod.json`). Licensed under LGPL-3.0, see [LICENSE](LICENSE). Code vendored from [Veil](https://github.com/FoundryMC/Veil) is listed in [NOTICE.md](NOTICE.md).
