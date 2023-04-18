package sts_exporter.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import downfall.patches.BanSharedContentPatch.PotionPatch;

public class DownfallPatches {
    @SpirePatch(clz=PotionPatch.class, method="Postfix", requiredModId="downfall")
    public static class KeepPotions {
        public static SpireReturn<Void> Prefix() {
            return SpireReturn.Return();
        }
    }
}
