package sts_exporter.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.DisplayConfig;

public class ForceResPatches {
    @SpirePatch(clz=DisplayConfig.class, method="getWidth")
    public static class Width {
        public static SpireReturn<Integer> Prefix() {
            return SpireReturn.Return(1920);
        }
    }
    
    @SpirePatch(clz=DisplayConfig.class, method="getHeight")
    public static class Height {
        public static SpireReturn<Integer> Prefix() {
            return SpireReturn.Return(1080);
        }
    }

    @SpirePatch(clz=DisplayConfig.class, method="getWFS")
    public static class Borderless {
        public static SpireReturn<Boolean> Prefix() {
            return SpireReturn.Return(false);
        }
    }

    @SpirePatch(clz=DisplayConfig.class, method="getIsFullscreen")
    public static class Fullscreen {
        public static SpireReturn<Boolean> Prefix() {
            return SpireReturn.Return(false);
        }
    }
}