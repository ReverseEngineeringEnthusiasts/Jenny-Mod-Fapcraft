# Running Fapcraft on Cleanroom

Cleanroom is a MinecraftForge continuation for 1.12.2 that runs on Java 25 with LWJGL 3 + SDL 3.
It is what the CurseForge-era 1.12 packs cannot do (Java 8, LWJGL 2), and Fapcraft 1.1.1 runs on
it — but three things about the combination are not obvious and cost a day to work out.

Verified against **Cleanroom 0.7.13-alpha** and **0.7.14-alpha**, Java 25.0.1
(`java-runtime-epsilon`), Prism Launcher 11.1.1 (Flatpak), Arch + NVIDIA.

## Installation (Prism)

The upstream project ships a MultiMC-format instance zip, which Prism imports directly:

1. Download `cleanroom-<version>-alpha.zip` from the GitHub release.
2. Unzip it into a Prism instance folder: it must contain `instance.cfg`, `mmc-pack.json` and
   `patches/net.minecraftforge.json` + `patches/org.lwjgl.json` at the instance root.
3. `mmc-pack.json` declares `net.minecraft` 1.12.2, `org.lwjgl` 3.4.3 and
   `net.minecraftforge` = the Cleanroom version. The local patch file supplies the loader,
   `mainClass = top.outlands.foundation.boot.Foundation`, `compatibleJavaMajors = [25]` and
   `compatibleJavaName = java-runtime-epsilon`.
4. Prism resolves the libraries from `maven.cleanroommc.com` + Maven Central, using the SHA-1s in
   the patch. The universal jar on Maven Central and the GitHub release asset are byte-identical.

Prism only rescans the instances directory at startup, so restart the launcher after dropping a
new folder in — a second `--launch` invocation forwards to the already-running process, which
does not know the new instance.

## The splash screen must stay off

With Forge's animated splash enabled, Cleanroom 0.7.13/0.7.14 can die during startup:

```
java.lang.NoClassDefFoundError: org/lwjgl/sdl/SDL_DropEvent
  at ... SplashProgress$2.run
Caused by: java.util.ConcurrentModificationException
  at top.outlands.foundation.TransformerDelegate.fillTransformerHolder
  at top.outlands.foundation.boot.ActualClassLoader.runTransformers
```

The splash thread loads classes while the main thread is still registering transformers, so a
class load can hit a list being modified. `SplashProgress.finish()` then throws
`IllegalStateException: Splash thread` and the client dies.

Fix: `minecraft/config/splash.properties` → `enabled=false`. The game then draws vanilla's own
loading screen on the main thread and the race cannot happen. The animated splash is the only
casualty.

## The dark GUI (fixed in 1.1.1)

Symptom: every GUI screen — main menu, options, chests — renders at roughly 4 % brightness, while
the world and the in-game HUD look normal.

Cause: a mod toggled `GL_LIGHTING` with raw `GL11.glEnable(2896)`/`glDisable(2896)` instead of
`GlStateManager`. Minecraft caches the lighting state, so its own `disableLighting()` became a
no-op; the GUI then rendered lit, and lit GUIs are almost black.

Diagnosis that actually pinned it (worth reusing):

- A `-javaagent` or FML coremod that logs `glIsEnabled(GL_LIGHTING)` at the top of
  `GuiScreen.drawScreen` showed `lighting=true` on the **first** GUI frame with the mod loaded
  and `false` without it.
- Instrumenting `Gui.drawRect` / `drawGradientRect` first proved **nothing** was painted over the
  screen: the GUI was drawn normally, just lit.

Fix: every lighting toggle in the mod goes through `GlStateManager.enableLighting()` /
`disableLighting()`, and no renderer may call raw `glEnable`/`glDisable` for lighting, blend,
alpha, depth or cull (see `rendering.md`).

Prism-specific gotcha while arming a JVM agent: Prism 11 **ignores** `JvmArgs`, `Env` and
`WrapperCommand` in `instance.cfg` and strips them on save; it only honours what is set through
its GUI (Settings → Java → JVM arguments). A hand-built `java` command line can bypass the
launcher, but it must carry Prism's stdin parameter protocol or pass
`-Dfml.ignoreInvalidMinecraftCertificates=true -Dfml.ignorePatchDiscrepancies=true` to survive
Forge's jar signature checks.

## LWJGL 2 → 3 shim notes

Cleanroom keeps 1.12 mods working through `lwjgly`, which adapts LWJGL 2 calls at class load.
Two practical consequences:

- `org.lwjgl.opengl.GL11` resolves to LWJGL 3's class, and the adapter maps legacy names
  (`glGetFloat` → `glGetFloatv`). Do not rely on LWJGL 2-only `org.lwjgl.BufferUtils` semantics
  in new code.
- Anything still using `Display.*` goes through `CleanroomWindowBridge`, which pumps SDL events;
  that is the thread that races during splash (see above).

## What to check first when it crashes

| symptom | likely cause |
|---|---|
| `Splash thread` / `SDL_DropEvent` / transformer CME | splash screen enabled → set `enabled=false` |
| GUI uniformly dark, world fine | raw GL lighting toggle (see above) |
| `NoClassDefFoundError` for a mod class after a Java bump | Cleanroom's `compatibleJavaMajors` is `[25]`; use the bundled 25 runtime |
| `SecurityException: signer information does not match` | launching Minecraft by hand without the FML ignore flags |
| mod loads but a renderer throws once a scene starts | model/bone assumptions — see `rendering.md` |

## Library versions that matter

| component | version | note |
|---|---|---|
| GeckoLib | **3.0.30** (bundled/shaded) | last 1.12.2 line is 3.0.31; its only real change is a mirrored box-UV fix, and the mod has no mirrored models. Stay on 3.0.30. |
| unrar5j | **2.0.4** | 2.0.3 could loop forever extracting certain RAR5 reskin packs |
| Forge | 14.23.5.2860 | final 1.12.2 build |
| ASM | 7.1 | build-time only (SRG reobf); runtime ASM comes from Forge |

Cleanroom itself: the bundled instance pins `0.7.13-alpha`; `0.7.14-alpha` works the same way and
is a one-line `patches/net.minecraftforge.json` swap if you want to follow upstream.
