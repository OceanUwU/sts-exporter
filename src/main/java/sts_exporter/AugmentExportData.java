package sts_exporter;

import CardAugments.CardAugmentsMod;
import CardAugments.cardmods.AbstractAugment;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;

import java.util.ArrayList;
import java.util.Map.Entry;

public class AugmentExportData {
    private static AbstractCard dummyCard = new Madness();
    public String id;
    public String name;
    public String descriptionHTML;
    public String descriptionPlain;
    public String rarity;
    public ModExportData mod;

    AugmentExportData(ExportHelper export, String id, AbstractAugment augment) {
        this.id = id;
        this.name = augment.modifyName("", dummyCard).replace("  ", " ").trim();
        this.descriptionHTML = RelicExportData.smartTextToHTML(augment.getAugmentDescription(),true,true);
        this.descriptionPlain = RelicExportData.smartTextToPlain(augment.getAugmentDescription(),true,true);
        this.rarity = augment.getModRarity().toString();
        this.mod = export.findMod(augment.getClass());
        this.mod.augments.add(this);
    }

    public static ArrayList<AugmentExportData> exportAllAugments(ExportHelper export) {
        ArrayList<AugmentExportData> augments = new ArrayList<>();
        if (Loader.isModLoaded("CardAugments"))
            for (Entry<String, AbstractAugment> entry : CardAugmentsMod.modMap.entrySet())
                augments.add(new AugmentExportData(export, entry.getKey(), entry.getValue()));
        return augments;
    }
}