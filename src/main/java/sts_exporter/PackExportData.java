package sts_exporter;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.helpers.BlightHelper;
import thePackmaster.SpireAnniversary5Mod;
import thePackmaster.hats.HatMenu;
import thePackmaster.packs.AbstractCardPack;
import thePackmaster.packs.PackPreviewCard;
import thePackmaster.summaries.PackSummaryDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class PackExportData implements Comparable<PackExportData> {
    public AbstractCardPack pack;
    public ModExportData mod;
    public ExportPath image;
    public String id;
    public String name;
    public String author;
    public String credits;
    public AbstractCardPack.PackSummary summary;
    public ArrayList<String> cards;
    public ArrayList<String> tags;
    public String description, descriptionHTML, descriptionPlain;
    public String flavorText, flavorTextHTML, flavorTextPlain;

    PackExportData(ExportHelper export, AbstractCardPack pack) {
        this.pack = pack;
        this.mod = export.findMod(pack.getClass());
        this.mod.packs.add(this);
        this.id = pack.packID;
        this.name = pack.name;
        this.author = pack.author;
        this.credits = RelicExportData.smartTextToPlain(pack.credits, true, true);
        this.summary = pack.summary;
        List<AbstractCardPack.PackSummary.Tags> tagsList = this.summary.tags.stream().collect(Collectors.toList());
        this.tags = new ArrayList<String>();
        for (AbstractCardPack.PackSummary.Tags tag : tagsList)
            tags.add(((String)ReflectionHacks.privateStaticMethod(PackSummaryDisplay.class, "getTagString", AbstractCardPack.PackSummary.Tags.class).invoke(new Object[]{tag})).replace("#y", ""));
        List<String> cardsList = pack.cards.stream().map(c -> c.name).collect(Collectors.toList());
        this.cards = new ArrayList<String>(cardsList);
        this.description = RelicExportData.smartTextToPlain(pack.description, true, true);
        this.image = export.exportPath(this.mod, "packs", this.id, ".png");
    }

    public void exportImages() {
        this.image.mkdir();
        exportImageToFile();
    }

    private void exportImageToFile() {
        Exporter.logger.info("Rendering pack image to " + this.image.absolute);
        ExportHelper.renderSpriteBatchToPNG(-240.f/2.f, -322.f/2.f, 240.f, 322.f, 1.0f, this.image.absolute, (SpriteBatch sb) -> {
            PackPreviewCard preview = pack.makePreviewCard();
            preview.render(sb);
        });
    }

    public static ArrayList<PackExportData> exportAllPacks(ExportHelper export) {
        ArrayList<PackExportData> packs = new ArrayList<>();
        if (Loader.isModLoaded("anniv5")) {
            HatMenu.currentlyUnlockedHats.clear();
            HatMenu.currentlyUnlockedRainbows.clear();
            BlightHelper.initialize();
            for (AbstractCardPack pack : SpireAnniversary5Mod.unfilteredAllPacks)
                packs.add(new PackExportData(export, pack));
        }
        Collections.sort(packs);
        return packs;
    }

    @Override
    public int compareTo(PackExportData that) {
        return name.compareTo(that.name);
    }
}