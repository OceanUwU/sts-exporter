package sts_exporter;

import SpireLocations.NodeModifierHelper;
import SpireLocations.nodemodifiers.AbstractNodeModifier;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;

import java.util.ArrayList;

public class NodeModifierExportData {
    public String id;
    public String name;
    public String descriptionHTML;
    public String descriptionPlain;
    public String type;
    public ModExportData mod;
    public ExportPath image;
    public ArrayList<String> roomClasses = new ArrayList<>();
    public AbstractNodeModifier modifier;

    NodeModifierExportData(ExportHelper export, AbstractNodeModifier modifier) {
        this.modifier = modifier;
        id = modifier.MODIFIER_ID;
        name = modifier.getTooltipStrings()[0];
        descriptionHTML = RelicExportData.smartTextToHTML(modifier.getTooltipStrings()[1],true,true);
        descriptionPlain = RelicExportData.smartTextToPlain(modifier.getTooltipStrings()[1],true,true);
        type = modifier.type.toString().substring(0, 1) + modifier.type.toString().substring(1).toLowerCase();
        mod = export.findMod(modifier.getClass());
        for (Class<?> roomClass : modifier.getRoomClasses()) {
            switch (roomClass.getSimpleName()) {
                case "MonsterRoom": roomClasses.add("fights"); break;
                case "MonsterRoomElite": roomClasses.add("elites"); break;
                case "MonsterRoomBoss": roomClasses.add("bosses"); break;
                case "EventRoom": roomClasses.add("question marks"); break;
                case "TreasureRoom": roomClasses.add("chests"); break;
                case "RestRoom": roomClasses.add("campfires"); break;
                case "ShopRoom": roomClasses.add("shops"); break;
            }
        }
        mod.nodemodifiers.add(this);
        image = export.exportPath(mod, "nodemodifiers", id, ".png");
    }

    public void exportImages() {
        this.image.mkdir();
        exportImageToFile();
    }

    private void exportImageToFile() {
        Exporter.logger.info("Rendering node modifier image to " + this.image.absolute);
        ExportHelper.renderSpriteBatchToPNG(0.f, 0.f, 64.f, 64.f, 1.0f, this.image.absolute, (SpriteBatch sb) -> {
            modifier.render(sb, 1f, -64f, 0f);
        });
    }

    public static ArrayList<NodeModifierExportData> exportAllModifiers(ExportHelper export) {
        ArrayList<NodeModifierExportData> nodeModifiers = new ArrayList<>();
        if (Loader.isModLoaded("spirelocations"))
            for (AbstractNodeModifier modifier : NodeModifierHelper.nodeModifiers)
                nodeModifiers.add(new NodeModifierExportData(export, modifier));
        return nodeModifiers;
    }
}