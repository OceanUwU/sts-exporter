package sts_exporter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.Skeleton;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.monsters.city.Byrd;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;
import com.megacrit.cardcrawl.rooms.EmptyRoom;

import basemod.BaseMod;
import basemod.ReflectionHacks;

public class CreatureExportData implements Comparable<CreatureExportData> {
    public AbstractCreature creature;
    public ExportPath image;
    public String id;
    public String name;
    public String type;
    public int minHP, maxHP, minHPAsc, maxHPAsc;
    public ModExportData mod;
    // for players
    public boolean isPlayer;
    public String cardColor;
    // for creatures
    private static boolean highAsc = false;
    private static HashMap<Class<?>, Integer> minHPs = new HashMap<>();
    private static HashMap<Class<?>, Integer> maxHPs = new HashMap<>();
    private static HashMap<Class<?>, Integer> ascensionMinHPs = new HashMap<>();
    private static HashMap<Class<?>, Integer> ascensionMaxHPs = new HashMap<>();

    public CreatureExportData(ExportHelper export, AbstractCreature creature) {
        this.creature = creature;
        this.id = creature.id != null ? creature.id : creature.getClass().getSimpleName();
        this.name = creature.name;
        this.mod = export.findMod(creature.getClass());
        this.mod.creatures.add(this);
        this.image = export.exportPath(this.mod, "creatures", this.id, ".png");
        if (creature instanceof AbstractPlayer) {
            AbstractPlayer player = (AbstractPlayer)creature;
            this.isPlayer = true;
            this.type = "Player";
            this.cardColor = Exporter.colorName(player.getCardColor());
            this.maxHP = player.startingMaxHP;
            this.maxHPAsc = player.startingMaxHP - player.getAscensionMaxHPLoss();
            this.minHP = MathUtils.round(maxHP * 0.9f);
            this.minHPAsc = MathUtils.round(maxHPAsc * 0.9f);
        } else if (creature instanceof AbstractMonster) {
            AbstractMonster monster = (AbstractMonster)creature;
            this.type = Exporter.toTitleCase(monster.type.toString());
            minHP = maxHP = minHPAsc = maxHPAsc = creature.maxHealth;
            if (minHPs.containsKey(creature.getClass()))
                minHP = minHPs.get(creature.getClass());
            if (maxHPs.containsKey(creature.getClass()))
                maxHP = maxHPs.get(creature.getClass());
            if (ascensionMinHPs.containsKey(creature.getClass()))
                minHPAsc = ascensionMinHPs.get(creature.getClass());
            if (ascensionMaxHPs.containsKey(creature.getClass()))
                maxHPAsc = ascensionMaxHPs.get(creature.getClass());
        }
    }

    public void exportImages() {
        this.image.mkdir();
        exportImageToFile(this.image.absolute);
    }

    private void exportImageToFile(String imageFile) {
        Exporter.logger.info("Rendering creature image to " + imageFile);
        // disable animation during rendering
        float dt = Gdx.graphics.getDeltaTime();
        setDeltaTime(0); // don't do animation steps
        // Get size of the creature.
        // We could use the hitbox, but that is not guaranteed to actually contain the whole image.
        // For now, just add a lot of padding.
        float scale = 1.0f / Settings.scale;
        float xpadding = 90.0f;
        float ypadding = 40.0f;
        float x = creature.hb.x-xpadding;
        float y = creature.hb.y-ypadding;
        float width  = creature.hb.width+2*xpadding;
        float height = creature.hb.height+2*ypadding;
        // get size from the Spine skeleton
        Skeleton skeleton = (Skeleton)ReflectionHacks.getPrivate(creature, AbstractCreature.class, "skeleton");
        if (skeleton != null) {
            Vector2 pos = new Vector2(), size = new Vector2();
            creature.state.update(Gdx.graphics.getDeltaTime());
            skeleton.updateWorldTransform();
            skeleton.setPosition(creature.drawX + creature.animX, creature.drawY + creature.animY + AbstractDungeon.sceneOffsetY);
            skeleton.getBounds(pos,size);
            x = pos.x;
            y = pos.y;
            width = size.x;
            height = size.y;
        }
        // Render to a png
        ExportHelper.renderSpriteBatchToPNG(x,y, width,height, scale, imageFile, (SpriteBatch sb) -> {
            // use AbstractCreature.render()
            // Note: the normal render code uses a PolygonSpriteBatch CardCrawlGame.psb, so make sure the projection is the same
            Matrix4 oldProjection = CardCrawlGame.psb.getProjectionMatrix();
            CardCrawlGame.psb.setProjectionMatrix(sb.getProjectionMatrix());
            boolean oldHideCombatElements = Settings.hideCombatElements;
            Settings.hideCombatElements = true; // don't render monster intent
            try {
                if (creature instanceof AbstractPlayer) {
                    ((AbstractPlayer)creature).renderPlayerImage(sb);
                } else {
                    creature.render(sb);
                }
            } finally {
                // cleanup
                setDeltaTime(dt);
                CardCrawlGame.psb.setProjectionMatrix(oldProjection);
                Settings.hideCombatElements = oldHideCombatElements;
            }
        });
    }

    private static void setDeltaTime(float deltaTime) {
        // When we call AbstractPlayer.renderPlayerImage, this updates animations based on Gdx.graphics.getDeltaTime().
        // So it would update the animation twice, resulting in a sped up animation.
        // As a fix, we set deltaTime to 0
        if (Gdx.graphics instanceof LwjglGraphics) {
            ReflectionHacks.setPrivate(Gdx.graphics, LwjglGraphics.class, "deltaTime", deltaTime);
        }
    }

    public static ArrayList<CreatureExportData> exportAllCreatures(ExportHelper export) {
        ArrayList<CreatureExportData> creatures = new ArrayList<>();
        for (AbstractCreature m : getAllCreatures()) {
            creatures.add(new CreatureExportData(export, m));
        }
        Collections.sort(creatures);
        return creatures;
    }

    public static ArrayList<AbstractCreature> getAllCreatures() {
        ArrayList<AbstractCreature> creatures = new ArrayList<>();
        highAsc = false;
        AbstractDungeon.ascensionLevel = 0;
        creatures.addAll(getAllPlayers());

        minHPs.clear();
        maxHPs.clear();
        ascensionMinHPs.clear();
        ascensionMaxHPs.clear();
        creatures.addAll(getAllMonsters());
        highAsc = true;
        AbstractDungeon.ascensionLevel = 20;
        getAllMonsters();

        return creatures;
    }

    // Get all player characters
    public static ArrayList<AbstractPlayer> getAllPlayers() {
        // We need to initialize DailyMods before creating AbstractPlayers
        ModHelper.setModsFalse();
        ArrayList<AbstractPlayer> players = new ArrayList<>();
        try {
            Method createCharacter = CardCrawlGame.class.getDeclaredMethod("createCharacter", AbstractPlayer.PlayerClass.class);
            createCharacter.setAccessible(true);
            for (AbstractPlayer.PlayerClass playerClass : AbstractPlayer.PlayerClass.values()) {
                try {
                    AbstractPlayer p = (AbstractPlayer)createCharacter.invoke(null, playerClass);
                    p.name = p.title;
                    players.add(p);
                } catch (Exception e) {
                    Exporter.logger.error("Exception occured when creating character", e);
                }
            }
        } catch (Exception e) {
            Exporter.logger.error("Exception occured when getting createCharacter method", e);
        }
        return players;
    }

    public static ArrayList<AbstractMonster> getAllMonsters() {
        // We need to initialize the random seeds before creating AbstractMonsters (for AbstractDungeon.monsterHpRng among others)
        Settings.seed = new Long(12345);
        AbstractDungeon.generateSeeds();

        // For rendering monsters we need:
        AbstractDungeon.player = CardCrawlGame.characterManager.getAllCharacters().get(0);
        AbstractDungeon.player.isDead = true; // don't render monster health bars
        AbstractDungeon.currMapNode = new MapRoomNode(0, -1);
        AbstractDungeon.currMapNode.room = new EmptyRoom();
        AbstractDungeon.currMapNode.room.monsters = new MonsterGroup(new AbstractMonster[0]); // needed to render monsters
        AbstractDungeon.id = ""; // for a Replay the Spire creature

        // Now get all monsters
        // There is unfortunately no list of all monsters in the game. The best we can do is to use MonsterHelper.getEncounter
        ArrayList<AbstractMonster> creatures = new ArrayList<>();
        HashSet<String> seenMonsters = new HashSet<>();
        for (String encounter : BaseMod.encounterList) {
            Exporter.logger.info("Getting monsters for encounter " + encounter);
            MonsterGroup monsters = MonsterHelper.getEncounter(encounter);
            for (AbstractMonster monster : monsters.monsters) {
                String id = monster.getClass().getName();
                if (seenMonsters.contains(id)) continue;
                creatures.add(monster);
                seenMonsters.add(id);
            }
        }

        // Custom monsters (BaseMod)
        @SuppressWarnings("unchecked")
        HashMap<String,BaseMod.GetMonsterGroup> customMonsters =
            (HashMap<String,BaseMod.GetMonsterGroup>) ReflectionHacks.getPrivateStatic(BaseMod.class, "customMonsters");
        for (BaseMod.GetMonsterGroup group : customMonsters.values()) {
            MonsterGroup monsters = group.get();
            for (AbstractMonster monster : monsters.monsters) {
                String id = monster.getClass().getName();
                if (seenMonsters.contains(id)) continue;
                creatures.add(monster);
                seenMonsters.add(id);
            }
        }

        // Awake lagavulin looks different
        Lagavulin lagavulin = new Lagavulin(false);
        lagavulin.id = lagavulin.id + "Awake";
        lagavulin.name = lagavulin.name + " (Awake)";
        creatures.add(lagavulin);

        // Downed byrd looks different
        Byrd byrd = new Byrd(500,500);
        byrd.changeState(Byrd.GROUND_STATE);
        byrd.id = byrd.id + "Grounded";
        byrd.name = byrd.name + " (Grounded)";
        creatures.add(byrd);

        Collections.sort(creatures, (AbstractMonster a, AbstractMonster b) -> { return a.name.compareTo(b.name); });
        return creatures;
    }

    @Override
    public int compareTo(CreatureExportData that) {
        if (creature.isPlayer && !that.creature.isPlayer) return -1;
        if (!creature.isPlayer && that.creature.isPlayer) return 1;
        return name.compareTo(that.name);
    }

    @SpirePatch(clz=AbstractMonster.class, method="setHp", paramtypez={int.class})
    public static class OnSetHP {
        public static void Prefix(AbstractMonster __instance, int hp) {
            if (highAsc) {
                ascensionMinHPs.put(__instance.getClass(), hp);
                ascensionMaxHPs.put(__instance.getClass(), hp);
            } else {
                minHPs.put(__instance.getClass(), hp);
                maxHPs.put(__instance.getClass(), hp);
            }
        }
    }

    @SpirePatch(clz=AbstractMonster.class, method="setHp", paramtypez={int.class, int.class})
    public static class OnSetHPMinMax {
        public static void Prefix(AbstractMonster __instance, int minHp, int maxHp) {
            if (highAsc) {
                ascensionMinHPs.put(__instance.getClass(), minHp);
                ascensionMaxHPs.put(__instance.getClass(), maxHp);
            } else {
                minHPs.put(__instance.getClass(), minHp);
                maxHPs.put(__instance.getClass(), maxHp);
            }
        }
    }
}