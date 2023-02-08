package sts_exporter;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.BlightHelper;

import java.util.ArrayList;
import java.util.Collections;

class BlightExportData implements Comparable<BlightExportData> {
    public AbstractBlight blight;
    public ModExportData mod;
    public String tier;
    public String pool;
    public AbstractCard.CardColor poolColor;
    public ExportPath image;
    public ExportPath popupImage;
    public ExportPath smallPopupImage;
    public String id;
    public String name;
    public String description, descriptionHTML, descriptionPlain;
    public String flavorText, flavorTextHTML, flavorTextPlain;

    BlightExportData(ExportHelper export, AbstractBlight blight) {
        this.blight = blight;
        this.mod = export.findMod(blight.getClass());
        this.mod.blights.add(this);
        this.id = blight.blightID;
        this.name = blight.name;
        this.description = RelicExportData.smartTextToPlain(blight.description, true, true);
        this.image = export.exportPath(this.mod, "blights", this.id, ".png");
    }

    public void exportImages() {
        this.image.mkdir();
        exportImageToFile();
    }

    private void exportImageToFile() {
        Exporter.logger.info("Rendering blight image to " + this.image.absolute);
        // Render to a png
        ExportHelper.renderSpriteBatchToPNG(0.f, 0.f, 256.f, 256.f, 1.0f, this.image.absolute, (SpriteBatch sb) -> {
            sb.setColor(new Color(0.0f, 0.0f, 0.0f, 0.33f));
            sb.draw(this.blight.outlineImg, 64.0f, 64.0f, 64.0f, 64.0f, 128.0f, 128.0f, 2.0f, 2.0f, 0.0f, 0, 0, 128, 128, false, false);
            sb.setColor(Color.WHITE);
            sb.draw(blight.img,      64.0f, 64.0f, 64.0f, 64.0f, 128.0f, 128.0f, 2.0f, 2.0f, 0.0f, 0, 0, 128, 128, false, false);
        });
    }


    public static ArrayList<BlightExportData> exportAllBlights(ExportHelper export) {
        ArrayList<BlightExportData> blights = new ArrayList<>();
        BlightHelper.initialize();
        for (String blight : BlightHelper.blights) {
            blights.add(new BlightExportData(export, BlightHelper.getBlight(blight)));
        }
        Collections.sort(blights);
        return blights;
    }

    @Override
    public int compareTo(BlightExportData that) {
        return name.compareTo(that.name);
    }
}