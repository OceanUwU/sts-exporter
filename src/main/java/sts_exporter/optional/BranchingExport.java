package sts_exporter.optional;

import com.evacipated.cardcrawl.mod.stslib.cards.interfaces.BranchingUpgradesCard;
import com.megacrit.cardcrawl.cards.AbstractCard;
import sts_exporter.CardExportData;
import sts_exporter.ExportHelper;

public class BranchingExport {
    public static boolean testAndExport(ExportHelper export, AbstractCard c, CardExportData data, int upgradeCount)
    {
        if (c instanceof BranchingUpgradesCard)
        {
            AbstractCard normal = c.makeCopy();
            ((BranchingUpgradesCard) normal).doNormalUpgrade();
            normal.displayUpgrades();
            data.upgrade = new CardExportData(export, normal, upgradeCount + 1);

            AbstractCard alt = c.makeCopy();
            ((BranchingUpgradesCard) alt).doBranchUpgrade();
            alt.displayUpgrades();
            data.altUpgrade = new CardExportData(export, alt, upgradeCount + 1);

            return true;
        }

        return false;
    }
}
