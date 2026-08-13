#!/usr/bin/env python3
"""Rename confirmed obfuscated BaseGirlEntity fields to meaningful names.

Scoped:
  * BaseGirlEntity.java       - declarations, `this.X`, bare static refs
  * descendant entity files   - `this.X` accesses of inherited fields
  * everywhere                - `BaseGirlEntity.X` static refs

Method calls `this.X(...)` are excluded via negative lookahead.
Use: python3 tools/rename_fields.py
"""
import os
import re

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "java")

INSTANCE = {
    "t": "TICK_RATE", "g": "animationFactory", "z": "wanderGoal",
    "o": "watchClosestGirlGoal", "k": "GLOBAL_GIRL_CACHE", "B": "cameraOriginPos",
    "r": "cameraYaw", "m": "entityDataManager", "f": "pathNavigator",
    "l": "homePos", "q": "activeEnderPearl", "n": "scaleFactor", "F": "isSpecialState",
    "i": "isLocallyRegistered", "x": "boneOffsetCache", "C": "actionController",
    "E": "movementController", "s": "eyesController", "A": "animationVariantMap",
    "H": "cachedAnimationProcessor", "p": "boneTrackingList", "d": "customPartsData",
    "I": "TEMPTATION_ITEMS",
}
STATIC = {
    "v": "MASTER", "G": "IS_ANCHORED", "e": "TARGET_POS", "w": "YAW_ROTATION",
    "u": "GIRL_ID", "D": "OUTFIT_INDEX", "J": "CUR_ACTION", "h": "GIRL_HAND_STATES",
    "y": "INTERACTION_PARTNER_UUID", "a": "WALK_SPEED", "b": "CUSTOM_MODEL_KEY",
    "c": "CUSTOM_NAME",
}
CLASS = "BaseGirlEntity"

# direct subclasses of BaseGirlEntity (this.X = inherited field there)
HIERARCHY = [
    "AbstractGirlNpcEntity.java", "AbstractNpcOnlyEntity.java",
    "AbstractPlayerGirlEntity.java", "AbstractKoboldPlayerEntity.java",
    "BeeEntityBase.java", "AllieEntity.java", "BeeEntity.java", "BiaEntity.java",
    "EllieEntity.java", "GalathEntity.java", "GoblinEntity.java", "JennyEntity.java",
    "KoboldEntity.java", "LunaEntity.java", "ManglelieEntity.java", "SlimeEntity.java",
    "AlliePlayerEntity.java", "BeePlayerEntity.java", "BiaPlayerEntity.java",
    "ElliePlayerEntity.java", "GalathPlayerEntity.java", "GoblinPlayerEntity.java",
    "JennyPlayerEntity.java", "KoboldPlayerEntity.java", "LunaPlayerEntity.java",
    "SlimePlayerEntity.java",
]
BASE_FILE = "BaseGirlEntity.java"

def is_base(path):
    return path.endswith("/entity/" + BASE_FILE)

def is_hierarchy(path):
    name = os.path.basename(path)
    return name in HIERARCHY

def field_decl_re(letter, new):
    # declaration/assignment of field:  `TYPE X;` or `TYPE X = ...;` or `static ... X;`
    # match the bare identifier as a declared field (preceded by whitespace/type, followed by = ; , )
    return re.compile(r"(?<=[a-zA-Z0-9_])\s+\b%s\s*[=;,]" % re.escape(letter)), None

def main():
    base_path = None
    for dirpath, _dirs, files in os.walk(ROOT):
        for name in files:
            if name.endswith(".java"):
                p = os.path.join(dirpath, name)
                if is_base(p):
                    base_path = p

    def sub_this(text, mapping):
        for letter, new in mapping.items():
            text = re.sub(r"\bthis\.%s\b(?!\s*\()" % re.escape(letter), "this." + new, text)
        return text

    # 1) BaseGirlEntity.java: declarations, this.X, static-class refs
    with open(base_path, encoding="utf-8") as fh:
        btext = fh.read()

    # rename field declarations: match ` <letter>[ ;=]` on lines that are field decls
    decl_map = {**INSTANCE, **STATIC}
    for letter, new in decl_map.items():
        btext = re.sub(
            r"^(\s*(?:public|protected|private|static|final|@\w+[\w.]*\([^)]*\)\s*)*[\w<>,?\[\].]+?)\s+(%s)\s*(=|[;,])" % re.escape(letter),
            lambda m, _new=new: m.group(1) + " " + _new + " " + m.group(3),
            btext, flags=re.M)
    btext = sub_this(btext, decl_map)
    # bare static DataParameter refs (u, D, J, h, y, G, w, e, v, a, b, c, I, k, t)
    for letter, new in STATIC.items():
        btext = re.sub(r"(?<![.\w])%s\b(?!\s*\()" % re.escape(letter), new, btext)
    for letter, new in {"I": "TEMPTATION_ITEMS", "k": "GLOBAL_GIRL_CACHE", "t": "TICK_RATE"}.items():
        btext = re.sub(r"(?<![.\w])%s\b(?!\s*\()" % re.escape(letter), new, btext)

    with open(base_path, "w", encoding="utf-8") as fh:
        fh.write(btext)
    print("updated", base_path)

    # 2) descendant files: this.X
    for dirpath, _dirs, files in os.walk(ROOT):
        for name in files:
            if not name.endswith(".java"):
                continue
            p = os.path.join(dirpath, name)
            if not is_hierarchy(p):
                continue
            with open(p, encoding="utf-8") as fh:
                text = fh.read()
            new_text = sub_this(text, decl_map)
            if new_text != text:
                with open(p, "w", encoding="utf-8") as fh:
                    fh.write(new_text)
                print("updated", p)

    # 3) everywhere: BaseGirlEntity.X static refs
    for dirpath, _dirs, files in os.walk(ROOT):
        for name in files:
            if not name.endswith(".java"):
                continue
            p = os.path.join(dirpath, name)
            with open(p, encoding="utf-8") as fh:
                text = fh.read()
            new_text = text
            for letter, new in STATIC.items():
                new_text = re.sub(r"\b%s\.%s\b" % (CLASS, re.escape(letter)), CLASS + "." + new, new_text)
            if new_text != text:
                with open(p, "w", encoding="utf-8") as fh:
                    fh.write(new_text)
                print("updated", p)

if __name__ == "__main__":
    main()
