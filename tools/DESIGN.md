# A 1.12.2 Mod Design Language

This is the layout and access policy for this repository. It is derived from
how mature 1.12.2 mods actually organise code, not from what the decompiler
left behind. Treat it as a review checklist: a class is in the right place
only if it satisfies the rules below.

The short version:

> **Side first. Role second. Feature third, and only when the feature is big.
> Registration in one place. Packets in one place. No dumping grounds.**

---

## 1. Principles

The rules below come from mature 1.12.2 codebases and GeckoLib-based entity
mods. The recurring shape is:

1. **`client/` versus `common/` (or `server/`)** on every large mod. The split
   is about class loading, not tidiness: a dedicated server must never touch
   `Minecraft`, `GuiScreen`, `ModelBase` or a renderer.
2. **Roles inside each side** (`gui`, `model`, `render`, `entity`, `block`,
   `item`, `world`). Feature folders appear only when a feature is large
   enough to earn one.
3. **A registration package** for blocks, items, entities, sounds, potions and
   recipes, called from the proxy.
4. **One network package** with a single `PacketHandler`; packets are grouped
   by feature only when there are enough of them.
5. **A small `util`**. Math and truly generic helpers only. Domain logic lives
   with its domain.
6. **`api/` is for stable cross-package contracts.** Implementations, enums,
   constants and "stuff I did not know where to put" do not belong there.

### Access

Mature mods keep almost every top-level type `public` because Forge, GeckoLib
and cross-package code instantiate them. Their *members* are usually private.
The decompiled tree here is the opposite: many helpers are package-private and
many fields are package-private. That is why moving a class breaks access. The
design language fixes the cause, not the symptom.

### Entity shape

A large entity class is acceptable. What matters is that one entity's state,
AI, animations, combat, riding and interaction handlers are local to that
entity. Do not split one entity into `XxxActions`, `XxxCombat`, `XxxRiding`
static namespaces that all take the entity and reach into its fields; that
keeps the coupling and adds navigation cost. Extract a component only when it
is genuinely reusable by a second entity.

## 2. The rules

### R1. Side first

```
com.trolmastercard.sexmod
├── Main.java              @Mod entry point, constants, version loader
├── api/                   stable interfaces
├── proxy/                 CommonProxy / ClientProxy
├── network/               PacketHandler + packets
├── registry/              ModBlocks, ModItems, ModEntities, ModSounds, ModPotions
├── common/                code that may load on a dedicated server
└── client/                code that may only load on a client
```

- `common/**` must not import `net.minecraft.client.*`, `net.minecraftforge.client.*`,
  `com.trolmastercard.sexmod.client.**`, or GeckoLib render/model classes.
- `client/**` may import `common/**`. The reverse is a bug even when Forge
  happens to tolerate it.
- `@SideOnly(Side.CLIENT)` is a marker, not a licence to put client code in
  `common/`. The package is the boundary.
- **Legacy exception.** This tree came from a decompiled mod whose entity,
  item, command and model classes already carry `@SideOnly` client branches.
  `check_layout.py` reports those as warnings (errors with `--strict-side`).
  Do not add new ones: split the client part into `client/` or route it
  through the proxy.

### R2. Role second, feature third

Inside `common/` and `client/`, use role packages first:

```
common/{block,item,potion,entity,inventory,world,command,event,model,util,combat,update}
client/{gui,model,render,particle,network,reskin,event,debug}
```

Add a feature subpackage only when the role package would exceed roughly 25
top-level classes or the feature is a named domain:

```
common/entity/{base,girl,player,galath,goblin,kobold,scene,ai}
common/world/{gen,data}
client/gui/{screen,hud,container}
client/model/{base,entity,item,vanilla}
client/render/{base,entity,player,item,effects}
network/{girl,scene,galath,kobold}
```

Do **not** create a package for one incidental class. A one-class package is
allowed only for a real domain that will grow or that has a clear public name
(`common/block`, `common/potion`, `client/particle`).

### R3. Keep packages between about 5 and 25 classes

- More than ~30 top-level classes: split by feature or sub-role.
- Fewer than ~3: merge into the nearest parent unless it is a real domain.
- Never create `misc`, `stuff`, `helpers`, `common2`, `utils2` or a second
  `util` because the first one filled up.
- **Line count is not a split criterion.** A 2,758-line entity is easy to
  develop on when that entity's behaviour is local. Split for
  cohesion or a real second user, never just to make a file shorter.

### R4. Depth is bounded

At most four package levels below the mod root:

```
client/render/entity          depth 3   good
client/render/entity/player   depth 4   good
client/render/entity/player/parts  depth 5   too deep
```

If a fifth level feels necessary, the fourth level probably contains two
different roles.

### R5. Names describe the role, not the history

- Packages: singular role nouns (`block`, `item`, `entity`, `render`, `model`,
  `network`). Feature packages keep the feature's name (`galath`, `kobold`).
- Classes: `*Entity`, `*Item`, `*Block`, `*Packet`, `*Model`, `*Renderer`,
  `*Screen`, `*Container`, `*Hud`, `*Handler`, `*Manager`, `*Data`, `*Events`,
  `Mod*` for registry holders.
- Ban numeric suffixes (`Foo2`), `Basic*`, `Unknown*`, and `Sex*` when a domain
  word exists. Rename on the way into the new layout; do not carry a bad name
  across the tree.
- A class named `*Screen` must be a screen. A class named `*Renderer` must be a
  renderer. A class named `*Container` must be a `Container`. If the name lies,
  rename it.

### R6. Access follows package boundaries

- **Top-level types are `public`** if anything outside their package can see
  them, or if Forge/GeckoLib instantiates them (entities, items, blocks,
  packets, models, renderers, screens, commands, event handlers, registry
  holders).
- **Package-private top-level types are allowed only when every reference is
  in the same package.** If a helper is used from another package, either move
  the helper next to its users or make it `public`. Never widen a member just
  to survive a move.
- **Fields are `private`** unless the class is a deliberate value/data holder.
  No `protected` fields for cross-package access; use a method.
- **Methods are `private` by default.** `protected` only for documented
  subclass hooks in the same package hierarchy. `public` only for cross-package
  use, an interface implementation, or an event/Forge entry point.
- Utility classes: `final`, private constructor, static methods.
- `@Nullable` on nullable parameters/returns; `@Override` on every override.

### R7. Registration lives in `registry/`

One holder per registry type:

```
registry/ModBlocks.java
registry/ModItems.java
registry/ModEntities.java
registry/ModSounds.java
registry/ModPotions.java
registry/ModRecipes.java
```

Each holder exposes a `register()` (or `register(IEventBus)` equivalent for
1.12.2) called from the proxy. Event handlers subscribe to behaviour; they do
not register content. Client-only registration (renderers, key bindings,
models) lives in `client/registry/` or `ClientProxy`, never in `common/`.

### R8. Networking lives in `network/`

- One `PacketHandler` owns channel creation, discriminators and registration
  order. **Discriminator order is wire protocol; never reorder it.**
- Packets are grouped by the feature they serve, not by "scene" as a catch-all.
- A packet class is `public`, implements `IMessage`, and keeps its fields
  private.
- Server handlers schedule world mutation on the server thread; client
  handlers schedule on the client thread. No world mutation in the Netty
  thread.
- Packet names say what they do: `StartScenePacket`, not `KoboldStatePacket`
  when every girl uses it.

### R9. Entities, models and renderers

- Shared entity framework: `common/entity/base`.
- Concrete girl family: `common/entity/girl`; player forms:
  `common/entity/player`; large named features: `common/entity/<feature>`.
- AI goals: `common/entity/ai`.
- GeckoLib models: `client/model/{base,entity,item,vanilla}`.
- Renderers: `client/render/{base,entity,player,item,effects}`.
- A model/renderer belongs to the entity it serves, never to `items` just
  because a file name was misleading.
- Client-only marker entities and overlays stay in `client/`, not `common/`.
- **One entity = one entity class.** Keep its state machine, combat, riding,
  interactions and NBT in that class. Do not create `XxxActions`, `XxxCombat`, `XxxRiding`
  static namespaces that take the entity and reach into its fields: that keeps
  the coupling and adds navigation cost.
- Extract a component only when a second entity can use it without being
  passed as a bag of state. `GalathFlightData` (a real enum state machine) is
  a component; `GalathActions` (a static namespace for one entity) is not.
- Registration is one entry in `registry/ModEntities` and one entry in the
  client renderer registry. Do not duplicate the class/id map in a second enum.

### R10. World data, worldgen and commands

- `WorldSavedData` and per-world persistence: `common/world/data`.
- Generators and structure placement: `common/world/gen`.
- Commands: `common/command`.
- Tribe/domain managers live with their domain (`common/entity/kobold`), not
  in `util`.

### R11. `util` is not a domain

`common/util` is for code with no better home:

- `common/util/math` — vectors, rotation, trig, matrices.
- `common/util` — side checks, generic value types, string/thread helpers.
- Client-only helpers: `client/render/base` (skins, caches),
  `client/render/effects` (camera), `client/event` (input), `client/debug`.

If a util class mentions a domain noun (`Girl`, `Kobold`, `Galath`, `Tribe`,
`Model`, `Packet`), it belongs to that domain.

### R12. `api` is contracts only

`api/` contains interfaces that cross package boundaries and are expected to
stay stable. No enums, no constants, no helpers, no implementations. If an
"API" type is only used by one package, it belongs in that package's base.

### R13. Resources mirror the mod, not the Java packages

```
src/main/resources/assets/sexmod/
├── geo/            GeckoLib models
├── animations/     GeckoLib animations
├── textures/       entity, item, gui, particle
├── models/         vanilla item/block models
├── lang/
└── sounds/
```

Do not create a resource folder per Java package. Resource paths are part of
the installed mod and are kept stable independently of Java refactors.

---

## 3. Canonical tree

```
com.trolmastercard.sexmod
├── Main.java                     entry point, version loader, shared constants
├── api/                          I* contracts only
├── proxy/                        CommonProxy, ClientProxy
├── network/
│   ├── PacketHandler.java
│   ├── UnknownPacket.java
│   ├── girl/                     girl interaction, inventory, models, chat
│   ├── scene/                    scene lifecycle, camera, movement
│   ├── galath/                   Galath flight/rape/particle packets
│   └── kobold/                   tribe and task packets
├── registry/                     ModEntities, ModItems, SoundHandler
├── common/
│   ├── block/
│   ├── item/
│   ├── potion/
│   ├── inventory/                Container + Slot (shared with the server)
│   ├── command/
│   ├── event/
│   ├── model/                    server-side custom model archive/whitelist
│   ├── combat/
│   ├── update/
│   ├── world/
│   │   ├── data/                 WorldSavedData
│   │   └── gen/                  worldgen and structures
│   ├── entity/
│   │   ├── base/                 BaseGirlEntity, abstract bases, girl services
│   │   ├── girl/                 NPC girls + Manglelie + wild slime/pyrocynical
│   │   ├── player/               horny-potion player forms
│   │   ├── galath/               GalathEntity, flight enum, parts, projectile, player form
│   │   ├── goblin/
│   │   ├── kobold/               kobold + tribe system
│   │   └── ai/
│   └── util/
│       └── math/
└── client/
    ├── gui/
    │   ├── screen/               GuiScreen, GuiContainer screens, widgets
    │   ├── hud/
    │   └── container/            GuiContainer only
    ├── model/
    │   ├── base/                 GirlModel, controllers, interfaces, resources
    │   ├── entity/               GeckoLib entity models
    │   ├── item/                 GeckoLib item models
    │   └── vanilla/              vanilla ModelBase fallbacks
    ├── render/
    │   ├── base/                 shared girl renderers, layers, skins, caches
    │   ├── entity/               entity renderers
    │   ├── player/               player-form renderers
    │   ├── item/                 item renderers
    │   └── effects/              trails, ribbons, overlays, markers, camera
    ├── particle/
    ├── network/                  preview-world network stubs
    ├── reskin/                   pack discovery/download/core + screens
    ├── event/                    keys, input, movement, first-person handlers
    └── debug/                    age gate and dev overlays
```

## 4. Applying the language

The refactor is reproducible and staged:

```bash
# 1. Rename misleading classes first (still in their old packages).
python3 tools/refactor/rename_class.py src/main/java \
  com/trolmastercard/sexmod/client/gui/screen GirlScreenBase GirlSelectionScreen

# 2. Move classes. The mapping is {old.fqn: new.package}.
python3 tools/refactor/move_packages.py tools/refactor/mappings/layout.json

# 3. Make only the types that are now referenced across packages public.
python3 tools/refactor/widen_cross_package.py

# 4. Add missing imports, then compile.
python3 tools/refactor/add_imports.py
mvn -q -DskipTests compile
```

`tools/refactor/check_layout.py` enforces the structural rules (root classes,
`api` contents, client/common imports, package sizes, depth, numeric names).
Run it before every layout commit.

---

## 5. Review checklist

For every new or moved class:

- [ ] Is it on the correct side (`client/` vs `common/`)?
- [ ] Is the package a role or a real feature, not a leftover?
- [ ] Does the package stay between roughly 5 and 25 classes?
- [ ] Is the depth at most four levels below the mod root?
- [ ] Does the class name describe what it actually is?
- [ ] Is the top-level type `public` only because something else needs it?
- [ ] Are fields private, methods private unless used across packages?
- [ ] Is registration in `registry/`, not in an event handler?
- [ ] Is the packet in `network/<feature>` with a truthful name?
- [ ] Does `common/` avoid every `client` import?
- [ ] Is the resource path unchanged by the Java move?
