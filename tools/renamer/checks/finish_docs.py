import os, re

ROOT = '<REPO>/src/main/java/com/trolmastercard/sexmod'

# file (rel to ROOT) -> class javadoc body (without /** */)
DOCS = {
    'potion/HornyPotion.java': """/**
 * The horny potion — the item/effect that transforms the player into a girl
 * (spawns the XxxPlayerEntity form, hides the vanilla player). Girls check
 * {@code getActivePotionEffect(HORNY_POTION)} to decide whether scenes skip
 * the PAYMENT gate (Kobold U(), Jenny's menu) and to track the transformed
 * state (Jenny's {@code yFlag}, Luna's menu behavior).
 */""",
    'networking/UnknownPacket.java': """/**
 * Placeholder/unknown packet registered for both sides. Kept for wire
 * compatibility with the original jar's channel; no behavior.
 */""",
    'networking/UpdateEquipmentPacket.java': """/**
 * SERVER-bound packet: updates a girl's equipment/held items (used by the
 * girl inventory screens). See GirlInventoryContainer* for the UI side.
 */""",
    'networking/UpdateVelocityPacket.java': """/**
 * SERVER-bound packet: applies a velocity to a girl entity (impulses/knockback
 * from items and abilities).
 */""",
    'worldgen/ConfigWorldGenHandler.java': """/**
 * Forge world-gen event handler that routes structure generation to the
 * configured generators (girl houses etc.) — see GirlHouseGenerator.
 */""",
    'worldgen/GirlHouseGenerator.java': """/**
 * Generates the girls' houses in the overworld — the structures where NPC
 * girls spawn and call home ({@code homePos}).
 */""",
    'worldgen/IWorldGen.java': """/**
 * Interface for world-gen hooks used by ConfigWorldGenHandler.
 */""",
    'api/IGalathFinish.java': """/**
 * Callback fired when a galath scene/fight finishes.
 */""",
    'api/IGalathStart.java': """/**
 * Callback fired when a galath scene/fight starts.
 */""",
    'api/IPositionProvider.java': """/**
 * Provides a Vec3d position (used by scene positioning helpers).
 */""",
    'api/ITargetProvider.java': """/**
 * Provides a target entity (used by scene targeting helpers).
 */""",
    'api/KoboldNames.java': """/**
 * Kobold name generator / registry (tribe member names).
 */""",
    'api/LightingType.java': """/**
 * Lighting modes for whitelisted custom models (DEFAULT vs SEXMOD
 * look-vector lighting) — see ServerWhitelistManager.ModelData.
 */""",
    'util/ForgeEventHandler.java': """/**
 * Central Forge event registration hub (entity spawn, ticks, interactions).
 * RegisterB(true/false) sets up the event bus subscriptions; the mod's
 * handlers (HandlePlayerMovement, GirlCameraHelper, HornyMeterHud,
 * GirlSavedData, PlayerIds, ...) are registered here.
 */""",
    'util/LivingDeathHandler.java': """/**
 * Forge death handler: cleans dead girls out of the global girl list
 * ({@code BaseGirlEntity.GLOBAL_GIRL_CACHE} / getGirlEntityList removals)
 * and handles girl-specific death bookkeeping.
 */""",
    'util/GirlBedInteraction.java': """/**
 * Right-click-on-bed interaction hook used by bed scenes (girls walking a
 * player to bed / waking up).
 */""",
    'util/NameTagInteractHandler.java': """/**
 * Handles name-tag naming of girls on right-click (sets CUSTOM_NAME).
 */""",
    'util/GuiOpenHandler.java': """/**
 * CLIENT: opens GUIs for girls/structures when the server requests an
 * {@code openGui} (complements GuiHandler).
 */""",
    'util/ItemRegistrationHandler.java': """/**
 * Registers the mod's items (girl wand, dragon staff, tribe egg, ...).
 */""",
    'util/RenderHandler.java': """/**
 * Registers all entity renderers with the RenderManager (CLIENT).
 */""",
    'util/AllieWorldData.java': """/**
 * WorldSavedData for Allie companions (tamed Allie state per world).
 */""",
    'util/BeeWorldData.java': """/**
 * WorldSavedData for bee companions (tamed bee state per world).
 */""",
    'util/DeprecatedCheckForUpdates.java': """/**
 * Legacy update checker — no longer used (kept for reference).
 */""",
    'util/EscapeDirectionKey.java': """/**
 * CLIENT: maps key input to escape-direction actions in scenes.
 */""",
    'util/PathUtils.java': """/**
 * Pathfinding helpers for girl movement goals.
 */""",
    'util/GalathDamageSource.java': """/**
 * DamageSource for galath attacks.
 */""",
    'util/SuccubusDamageSource.java': """/**
 * DamageSource for succubus-style drain attacks.
 */""",
    'util/IBoneRotationSupplier.java': """/**
 * Supplies a bone rotation value (render-time hook for bone animation).
 */""",
    'util/IGalathExecute.java': """/**
 * Executable action hook for galath AI states.
 */""",
    'util/IGalathUpdate.java': """/**
 * Per-tick update hook for galath AI states.
 */""",
    'util/DebugWindow.java': """/**
 * Developer debug overlay window (obfuscated-era leftover, largely dormant).
 */""",
    'util/DebugWindow2.java': """/**
 * Second developer debug overlay window (leftover, largely dormant).
 */""",
    'util/AbstractPlayerKoblinGoboldRenderer.java': """/**
 * Base geckolib renderer for player-form kobolds/goblins.
 */""",
    'util/TrigMath.java': """/**
 * Trigonometry helpers (wrapDegrees etc.).
 */""",
    'util/Vector2d.java': """/**
 * 2D double vector value type.
 */""",
    'util/Vector2f.java': """/**
 * 2D float vector value type.
 */""",
    'util/Vector4d.java': """/**
 * 4D double vector value type (used e.g. for bed target + yaw).
 */""",
    'util/Rectangle.java': """/**
 * Integer rectangle value type.
 */""",
    'util/PlayerJennyRenderer.java': """/**
 * Renderer for the player-form Jenny (horny potion).
 */""",
    'util/PlayerBiaRenderer.java': """/**
 * Renderer for the player-form Bia (horny potion).
 */""",
    'util/PlayerLunaRenderer.java': """/**
 * Renderer for the player-form Luna (horny potion).
 */""",
    'util/PlayerEllieRenderer.java': """/**
 * Renderer for the player-form Ellie (horny potion).
 */""",
    'util/PlayerSlimeRenderer.java': """/**
 * Renderer for the player-form Slime (horny potion).
 */""",
    'util/PlayerBeeRenderer.java': """/**
 * Renderer for the player-form Bee (horny potion).
 */""",
    'util/PlayerGoblinRenderer.java': """/**
 * Renderer for the player-form Goblin (horny potion).
 */""",
    'util/PlayerAllieRenderer.java': """/**
 * Renderer for the player-form Allie (horny potion).
 */""",
    'util/SlimeRenderer.java': """/**
 * Renderer for the Slime NPC.
 */""",
    'util/BiaRenderer.java': """/**
 * Renderer for the Bia NPC.
 */""",
    'util/LunaRenderer.java': """/**
 * Renderer for the Luna NPC.
 */""",
    'util/AllieRenderer.java': """/**
 * Renderer for the Allie NPC.
 */""",
}

def insert_class_doc(path, doc):
    with open(path) as f:
        s = f.read()
    if '/**' in s:
        return False  # already has javadoc somewhere — skip (agents may have got it)
    m = re.search(r'^(?P<indent> *)(public |protected |private |abstract |final )*(class|interface|enum|@interface)\b', s, re.M)
    if not m:
        return False
    # find the class declaration line start
    decl_start = m.start()
    # back up over annotations
    line_start = s.rfind('\n', 0, decl_start) + 1
    # insert doc before any annotations attached to the class
    doc_block = "/**\n" + doc.strip() + "\n */\n"
    s = s[:line_start] + doc_block + s[line_start:]
    with open(path, 'w') as f:
        f.write(s)
    return True

done, skipped = 0, 0
for rel, doc in DOCS.items():
    p = os.path.join(ROOT, rel)
    if not os.path.exists(p):
        print("MISSING:", rel)
        continue
    if insert_class_doc(p, doc):
        done += 1
    else:
        skipped += 1
        print("SKIP (has javadoc or no class decl):", rel)
print(f"done={done} skipped={skipped}")
