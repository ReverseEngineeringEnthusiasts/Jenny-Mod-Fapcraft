import glob, re, sys

base = '<REPO>/src/main/java/com/trolmastercard/sexmod/'
targets = []
targets += [base + f for f in [
    'GalathActionListener.java','MatrixHelper.java','MobPredicates.java','PositionData.java',
    'SexNetworkManager.java','SkinFetcher.java','client/ClientNetHandlerOverride.java',
    'client/SexWorldClient.java','client/ShaderHelper.java','client/gui/BeeDialogueScreen.java',
    'client/gui/BeeScreen.java','client/gui/ChestContainer.java','client/gui/ChestContainerGui.java',
    'client/gui/CustomModelList.java','client/gui/EscapeMinigameHud.java','client/gui/GalathFlightHud.java',
    'client/gui/GalathScreen.java','client/gui/GenderSwapScreen.java','client/gui/GirlInventoryContainer.java',
    'client/gui/GirlInventoryContainer2.java','client/gui/GirlInventoryContainerGui.java',
    'client/gui/GirlInventoryContainerGui2.java','client/gui/GirlInventoryScreen.java',
    'client/gui/GirlInventorySlot.java','client/gui/GirlScreenBase.java','client/gui/GuiHandler.java',
    'client/gui/StructureCommandScreen.java','client/gui/TribeNameScreen.java','client/gui/UnknownScreen.java',
    'client/renderer/AlliesLampRenderer.java','client/renderer/BasicGirlRenderer.java',
    'client/renderer/BeeRenderer.java','client/renderer/DragonRenderer.java',
    'client/renderer/DragonStaffRenderer.java','client/renderer/EllieRenderer.java',
    'client/renderer/GalathCoinRenderer.java','client/renderer/GalathRenderer.java',
    'client/renderer/GirlLayerRenderer.java','client/renderer/GirlPlayerRenderer.java',
    'client/renderer/GirlRenderer.java','client/renderer/GirlRendererBase.java',
    'client/renderer/GoblinRenderer.java','client/renderer/JennyRenderer.java',
    'client/renderer/KoboldEggItemRenderer.java','client/renderer/KoboldEggRenderer.java',
    'client/renderer/KoboldRenderer.java','client/renderer/ManglelieRenderer.java',
    'client/renderer/SexEntityRenderer.java','client/renderer/SexSceneRenderer.java',
    'client/renderer/SummonItemRenderer.java','client/renderer/WildSlimeFaceLayer.java',
    'client/renderer/WildSlimeRenderer.java','client/renderer/api/IGirlRenderer.java',
    'client/model/AllieModel.java','client/model/AllieNpcModel.java','client/model/AlliesLampModel.java',
    'client/model/BeeModel.java','client/model/BeeNpcModel.java','client/model/BiaModel.java',
    'client/model/BiaNpcModel.java','client/model/CatModel.java','client/model/CatNpcModel.java',
    'client/model/DragonStaffModel.java','client/model/EllieModel.java','client/model/EllieNpcModel.java',
    'client/model/GalathCoinModel.java','client/model/GalathModel.java','client/model/GalathNpcModel.java',
    'client/model/GirlAnimationController.java','client/model/GirlAnimationProcessor.java',
    'client/model/GirlModel.java','client/model/GirlModelBase.java','client/model/GoblinModel.java',
    'client/model/GoblinNpcModel.java','client/model/JennyModel.java','client/model/JennyNpcModel.java',
    'client/model/KoboldEggItemModel.java','client/model/KoboldEggModel.java','client/model/KoboldModel.java',
    'client/model/KoboldNpcModel.java','client/model/LunaModel.java','client/model/ManglelieNpcModel.java',
    'client/model/SexSceneModel.java','client/model/SlimeModel.java','client/model/SlimeNpcModel.java',
    'client/model/SummonItemModel.java','client/model/api/IGirlModelInfo.java','client/model/api/IVanillaModel.java',
    'client/particle/DragonBreathParticle.java']]

ok = True
for f in targets:
    src = open(f).read()
    stripped = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    stripped = re.sub(r'//[^\n]*', '', stripped)
    # crude balance: ignore string literals and chars
    stripped = re.sub(r'"(\\.|[^"\\])*"', '""', stripped)
    stripped = re.sub(r"'(\\.|[^'\\])'", "''", stripped)
    for ch in '{}()[]':
        o = stripped.count(ch)
        c = stripped.count({'{':'}','}':'{','(' : ')',')':'(' ,'[':']',']':'['}[ch])
        if o != c:
            print(f"IMBALANCED {ch}: {f} ({o} vs {c})")
            ok = False
print("ALL BALANCED" if ok else "PROBLEMS")
sys.exit(0 if ok else 1)
