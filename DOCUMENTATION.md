# Fapcraft 1.12.2 Remap — Engineering Log & Findings

**Updated:** 2026-08-12, late session
**Purpose:** full record of every crash, root cause, fix, and open issue discovered while
getting the deobfuscated/restructured Fapcraft 1.12.2 working. Resume point for later work.

---

## 0. Project layout

| project | path | what it is |
|---|---|---|
| base (decompile) | `../Fapcraft-1.12.2/` | byte-faithful deobfuscation of `Fapcraft.1.12.2.v1.1.jar`, flat package `com.trolmastercard.sexmod`, Maven build |
| remap | `../Fapcraft-1.12.2-remap/` | same code restructured into role packages (`entity/`, `client/model/`, `client/renderer/`, `networking/`, `item/`, `util/`, `proxy/`, `command/`, `worldgen/`, …) + class renames |
| reference | `../Fapcraft-1.12.2/pipeline/reference/` | 2021 non-obfuscated source + 1.21.1 port (naming/structure blueprint) |

Deployed jar (remap) → Prism instance `More FPS [FORGE]` mods folder as
`fapcraft-1.12.2-1.1.0.jar`. Base jar staged as `fapcraft-BASE-1.1.0.jar.disabled` for A/B
testing (swap by renaming; only ONE fap jar active at a time — same modid `sexmod`).

Instance: Forge 14.23.5.2859, Java 8u202. **Forge 2859 lacks `SharedMonsterAttributes.SWIM_SPEED`
(added in 2860)** — every SWIM_SPEED reference was removed.

---

## 1. All crashes fixed (in order) — root causes

1. **Entity registration NPE** — `EntityRegistry.doModEntityRegistration` NPE.
   Decompiler dropped `Main.instance`: `registerModEntity(..., null, ...)` → `findContainerFor(null)`
   → null container. Official bytecode: `getstatic Main.instance`. Fixed in `SexModEntities`/`bi`.
   Also `Main` was missing `@Mod.Instance instance` and `@SidedProxy proxy` fields entirely
   (reconstruction had substituted `CommonProxy.PROXY`, which would never run `ClientProxy`
   on the client → no renderers/keybinds/entity preloading). Restored both + `proxy.xxx` calls.

2. **Item/block registry ClassCastException** — `AlliesLampItem cannot be cast to Block`.
   `@SubscribeEvent` handlers used raw `Register` instead of `Register<Item>`/`Register<Block>`
   (decompiler lost the generic). Raw `Register` subscribes to EVERY registry event (Blocks too)
   → registering an Item into the Block registry. Fixed 7 items + 1 block in both projects.

3. **`Main.setConfigs` IOException: Stream closed** — Vineflower "could not verify finally
   blocks" semaphore loop: `continue` inside try triggered `finally` which closed the
   BufferedReader mid-read. Original is plain try-with-resources. Fixed in `Main.setConfigs`.
   **Same latent bug** found + fixed in `ServerWhitelistManager.f_clash130()` and `a(File)`.
   All `// $VF:` comments stripped from the tree.

4. **`registerGuiHandler(null, …)` NPE** — same dropped-`Main.instance` artifact in
   `ClientProxy`/`CommonProxy`. Official bytecode: `getstatic Main.instance`. Fixed both.

5. **Kobold render NPE (`this.j` null)** — the reconstruction dropped the `doRender` bridges.
   Original `GirlRenderer` has THREE methods: `a(T,double,double,double,float,float)` (custom
   logic that sets `this.j`) + `func_76986_a(Entity,…)` + `doRender(EntityLivingBase,…)` that
   dispatch to it. Without the bridges, GeckoLib's base `doRender` runs, `this.j` stays null.
   Added bridges. **Generics clash note:** concrete `Entity`/`EntityLivingBase` params clash with
   the lib's geckolib; the working form is `func_76986_a(T,…)` + `doRender(T,…)` (javac emits
   the `Entity`/`EntityLivingBase` bridges). Same bug fixed in `SexSceneRenderer` via
   `doRender(SexSceneEntity,…)`.

6. **Item render NPE (`this.j.x`)** — item renderers (`DragonStaff`, `KoboldEgg`, `Summon`)
   dropped their `render(Item,ItemStack)` bridge that dispatches to the setup method `a(item,stack)`
   (which sets `this.j` then calls `super.render`). Fixed with `render(ConcreteItem,ItemStack)`
   (javac emits the `render(Item,…)` bridge). `AlliesLamp`/`GalathCoin` don't need it
   (override `render(GeoModel,…)` directly — verified against original).

7. **Allie/Galath/Manglelie/Slime texture = `.geo.json` → NPE on spawn** — `getTextureLocation`
   was left with the base model-selection logic (`c[n]` = geo json) instead of the subclass
   texture override. Official: `c2.a(em)` returns `textures/entity/allie/allie.png` etc.
   Fixed all 4 models to return their png. (The original base `cv.a(em)`/`getTextureLocation`
   DOES return `c[n]`, but every NpcModel overrides it with a texture; the reconstruction had
   put the base logic into the subclasses.)

8. **Goblin crash `NumberFormatException: "variant14"`** — `GoblinRenderer` lambda called
   `b(int,String)` with the bone NAME (`var5.getName()` = `"variant14"`). Original lambda
   (`lambda$manageEar$1`) calls `b(GeoBone,int)` — a DIFFERENT method (inherited from
   `GirlRendererBase`). Fixed the lambda to `b(var5, var1x)`.

9. **Bee + Galath `NoSuchFieldError: SWIM_SPEED`** — the mod references Forge 2860's
   `SWIM_SPEED`; instance has 2859. Removed from `BeeEntity` and `GalathEntity` (galath = the
   "can't spawn galath" cause).

10. **Jenny `NoClassDefFoundError: JennyEntity$1`** — NOT a code bug: the jar was replaced in
    the mods folder while the game was running; Forge lazily loaded the synthetic switch-map
    class from the replaced file. Class is valid + in the jar. Fix = restart the game; never
    swap jars mid-session.

---

## 2. Visual / behavioral issues — reported, investigated, status

### 2a. "in-sex-scene pose while walking" / "player character with dick following" (RESOLVED hypothesis: outfit model)
Investigated: `fp` animation-state enum (170 constants) is byte-identical to original (order +
values). Animation controller wiring (action/movement/eyes → same lambda) is faithful. Allie
animation selection is faithful (movement → tail, action → state-based). Follow-goal sets
`fp.NULL` (50 reset sites).

**Found the actual bug:** `GirlModel.getModelLocation(BaseGirlEntity)` returned `c[0]` always
(the NUDE model) while the outfit-based selection (`a_clash34`, = original `getModelLocation`)
was dead code. Official `cv.getModelLocation(Object)` → `a(em)` → `c[outfitNr]` (outfit from
DataParameter `D`, default `D=1`). Fixed: `getModelLocation → a_clash34(var0)`. **This likely
fixed both the nude-by-default AND the "scene pose while walking"** (nude model's default pose).

### 2b. "player model still appears alongside the characters" (OPEN)
Mechanism verified faithful against original:
- `GirlCameraHelper.a(RenderPlayerEvent.Pre)` cancels the player's render and renders the
  `AbstractPlayerGirlEntity` (girl form) at the player's position with the player's pose.
- `PositionData.a(Pre)` cancels the player render when their girl is in a `hasPlayer` scene state.
- `GirlPlayerRenderer` (via `db`) renders the girl form; `c_clash336` maps it to the player.
- `PlayerIds` (login), `ResetGirlPacket.Handler.a_clash10` (reset to `fp.NULL`) — faithful.

Symptom persists → likely one of:
- girl form's `al` map (`d_clash567`) empty at the wrong time → player renders normally AND
  girl form renders separately;
- girl stuck in a scene state (see 2c) keeps the player suppressed + the girl in scene pose;
- `AbstractPlayerGirlEntity` spawned/positioned wrong (its `func_70619_bc` teleports it to the
  player; if it's a real world entity it may render BOTH via entity pass and camera helper).

**Next:** trace how a normal (non-dev) player's girl form is created/spawned, and whether it is
added to the world (`world.spawnEntity`) — PlayerIds only spawns dev-account forms.

### 2c. "stuck in touch boobs scene with Luna; /kill doesn't exit; rejoin does" (OPEN)
Scene state advances via `BaseGirlEntity.l_clash514` (tick `followUp` transitions) and player
interaction packets. `ResetGirlPacket` resets to `fp.NULL`. `/kill` doesn't clear it — the death
handler (`util/eo`) only removes the girl from a list; the scene-state cleanup on player death is
missing or not firing. Rejoin works because `PlayerIds` login resets girl forms.

**Next:** find the scene-abort path (player death / disconnect) in the original bytecode vs
reconstruction; likely a missing packet or a state not reset on `PlayerDeathEvent`.

### 2d. "slime / manglelie / bee right-click does nothing" (OPEN — possibly original behavior)
Only `SexEntity`/`WildSlimeEntity` override `processInitialInteract`. The NPC interaction menu
opens via packets (`GirlDataPacket` → `GirlScreenBase`, `PlayerActionPacket`, `BeeOpenChestPacket`).
`PlayerInteractEvent` handlers: `fu` (player-girl), `f4` (name-tag naming), `am` (goblin render).
Need to verify which entities are MEANT to open a menu on right-click in the original and how the
trigger works (likely a `PlayerInteractEvent.EntityInteract` in a handler that checks NpcType).

### 2e. "Can't spawn Galath" — RESOLVED: SWIM_SPEED (see crash #9).

### 2f. "lamp hotbar icon shows hand" (OPEN)
`AlliesLampItem` sets `ModelLoader.setCustomModelResourceLocation(b, 0, "sexmod:allies_lamp")`
+ `setTileEntityItemStackRenderer(new AlliesLampRenderer())` — matches original bytecode, and
`assets/sexmod/models/item/allies_lamp.json` is **byte-identical to the original** (parent
`builtin/entity` → icon is rendered by the TileEntityItemStackRenderer = the 3D
`AlliesLampRenderer`). So the "hand" on the icon comes from **`AlliesLampRenderer` / its model**
rendering a hand (like DragonStaffRenderer renders one). Compare `AlliesLampRenderer` +
`AlliesLampModel` against the original renderer/model classes. (All item model jsons present
in the jar.)

### 2g. "Ellie face-fuck hands swap textures" (OPEN)
Player-model hand texture during Ellie scene — likely a render-layer/model issue; not yet traced.

### 2h. "buttons positions messed up vs original" — GUI layout (INVESTIGATED)
Exhaustively compared every `GuiButton` x/y/w/h in `GirlScreenBase` (b5), `GirlInventoryScreen`
(m), `ClothingScreen` (a), `BeeDialogueScreen` (ch), `TribeNameScreen` (g7) against the original
v1.1 jar bytecode (via `javap -c` **and** `krak2 dis`) — **all positions byte-identical**. Also
cross-checked against two independent third-party reconstructions
(`github.com/palkaline/jenny-mod-re`, `github.com/RukoBlood/jenny-mod-re-clean`) which agree.
The interaction menu's left buttons (`GirlInventoryScreen.a_clash826`) had **empty labels until
hovered** (`d[i] <= 14 ? "" : label` + 0–23px width) — this is the original's hover-expand design
but reads as "text not there". **FIXED:** left buttons now always show their labels with a fixed
100px width (both branches). The "scaling problem" is the original design's fixed left-anchored
column (x=36, y=70–190) + right-anchored scene buttons; not a deob corruption.

### 2i. "Jenny shows diamond armour by default" — the look IS the model's design
Jenny's dressed model (`jennydressed.geo.json`) has cyan/diamond-blue body + armor bones
(`midsection` #2fcdbb, `braLside` #2fcab9, `armorChest` #52e6d7, …) — byte-identical to the
original jar's texture/geo. Unequipped girls show the under-mesh (lingerie) look; that is the
original design. The armor-bone visibility logic (`GirlModel.a(T,Integer,AnimationEvent)` →
equipment-gated `X/T/U/W`) is jar-faithful and was left untouched.

### 2j. "steve renderer shows for every character" / "player model alongside characters" — **ROOT CAUSE FOUND**
`GirlRenderer.func_110775_a` (getEntityTexture) was deobfuscated as returning `getSkinTexture`
(the **player's** skin). Original jar: `func_110775_a(Entity)` → `invokespecial`
`GeoEntityRenderer.getEntityTexture(EntityLivingBase)` → `getTextureLocation` → the **girl model's
own texture** (jenny.png etc.). Consequence: EVERY girl entity rendered its model with the local
player's skin → garbled "steve" bodies on all characters, and the player-form looked like the
player model alongside the girls. **FIXED:** `func_110775_a(T)` now calls
`super.getEntityTexture(var1)` (girl's own texture). `getSkinTexture` remains for the steve
subtree render only (matches jar's `d(T)`). Applied to base + remap.

### 2k. (retracted) 2i "dressed armor bones" fix — REVERTED
The earlier fix (armor bones shown whenever `D != 0`) made EVERY unequipped girl render her
diamond-blue armor bones → "every character has diamond armour". Reverted to the jar-faithful
equipment-gated logic (`a(processor, X, T, U, W)`). The "diamond armour" the user saw on Jenny is
her model's own cyan/diamond-blue outfit (verified byte-identical textures/geo vs the original
jar) — not a code bug.

### 2l. "steve body + armour/clothing overlays always visible on girls" — **ROOT CAUSE FOUND + FIXED (2026-08-13)**
`GirlModel.setLivingAnimations` (the per-frame hook that hides the `steve` bone and gates the
`armor*` bones) had been renamed to `a(T,Integer,AnimationEvent)` AND the compiler-generated
bridge methods were dropped. The renderer calls `IAnimatableModel.setLivingAnimations(Object,…)`;
with no override it dispatched to the geckolib base no-op, so the bone logic in `a(...)` was
**dead code**. Consequences: every girl always rendered her `steve` subtree (player-skin body +
`Head2`/cock) even idle/walking, and always rendered her `armor*` overlay bones.

Verified against the original jar: `cv` (GirlModel) declares the real method `a(T,…)` plus the
bridges `setLivingAnimations(IAnimatable,…)` and `setLivingAnimations(Object,…)` (checkcast →
`a(em,…)`). Same for `o` (SexSceneModel).

**FIXED** (base + remap): renamed `GirlModel.a(T,Integer,AnimationEvent)` →
`setLivingAnimations(T,Integer,AnimationEvent)` and the 7 NpcModel overrides
(`a(BaseGirlEntity,…)` → `setLivingAnimations(BaseGirlEntity,…)`, incl. their `super.a` calls),
plus `SexSceneModel.a(SexSceneEntity,…)` → `setLivingAnimations(...)`. javac now generates the
bridges; compiled bytecode matches the jar. Both projects build (0 errors).

---

## 3. Key architecture findings (verified against original bytecode)

- **AnimationState `fp`**: 170 constants, order byte-identical. Fields: `length`, `followUp`,
  `hasPlayer`, `autoBlink`, `useBoyCam`, `flipGirlYaw`, `ticksPlaying`. `useBoyCam` states
  (SITDOWN/SITDOWNIDLE/PAIZURI) teleport the player camera to the girl's `boyCam` bone via
  `PositionData.a(RenderTickEvent)`.
- **Player render suppression**: `PositionData`/`am` `RenderPlayerEvent.Pre` cancel the player
  render when the player's girl is in a `hasPlayer` state. `GirlCameraHelper` renders the girl
  form in place of the player. `GirlPlayerRenderer` only renders when `h_clash508()` or the
  `v` flag is set (set by the camera helper).
- **Models**: original `getModelLocation` = outfit-based `c[outfitNr]`; `getTextureLocation` =
  per-NPC texture png; `getAnimationFileLocation` = per-NPC animation json. `c[]` arrays are
  model locations, `D` (DataParameter, default 1) = outfit number.
- **Scene lifecycle**: girl enters scene states via `b(fp)`; advances via tick `followUp`
  (`l_clash514`) and interaction packets; resets via `ResetGirlPacket`/`PlayerIds` login.
- **Girl forms (`AbstractPlayerGirlEntity`)**: `Z` list + `al` map (UUID→form), rebuilt by
  `C_clash585()`; `func_70619_bc` positions the form at the player.
- **Forge 2859 compat**: `SWIM_SPEED` does not exist (2860+); removed all uses.

---

## 4. Tooling / scripts used (in `/tmp/opencode/jenny-deob/`)

- `javap -c -p` on official `Fapcraft.1.12.2.v1.1.jar` classes = ground truth.
- Fix scripts (apply to both projects): `fix-model-location.py`, `fix-texture-methods.py`,
  `fix-goblin-lambda.py`, `fix-galath-swim.py`, `fix-base-skin.py`, `fix-item-bridge-types.py`,
  `fix-register-generic*.py`, `fix-guihandler.py`, `fix-swim-speed.py`, etc.
- `deploy-fix.sh` (copy remap jar → mods), `deploy-all.sh` (+ rebuild/stage base jar).
- Build: `mvn -q clean package` in each project → 0 errors/warnings.

---

## 5. Deployment state (end of session)

- Remap jar freshly built (post getModelLocation + galath-swim + jenny rebuild) and deployed.
- Base A/B jar rebuilt + `.disabled`.
- **NEXT ACTION FOR USER:** start the game fresh (jar already current). Report:
  - stripped/armor state of each girl (outfit model fix)
  - player model duplication
  - scene stuck / /kill exit
  - right-click menus (slime/manglelie/bee)
  - GUI button positions
  - lamp icon
  - Ellie hand texture
