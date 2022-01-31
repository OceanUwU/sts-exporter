package sts_exporter;

import java.util.*;

import basemod.BaseMod;
import com.megacrit.cardcrawl.helpers.GameDictionary;

import sts_exporter.patches.BaseModPatches;

class KeywordExportData implements Comparable<KeywordExportData> {
    public String description, descriptionHTML, descriptionPlain;
    public String name;
    public ArrayList<String> names = new ArrayList<>();
    public ModExportData mod;

    KeywordExportData(ExportHelper export, String name, String key, String description) {
        this.name = name;
        this.description = description;
        this.descriptionHTML = RelicExportData.smartTextToHTML(description,true,true);
        this.descriptionPlain = RelicExportData.smartTextToPlain(description,true,true);
        this.mod = export.findMod(BaseModPatches.keywordClasses.get(key));
        this.mod.keywords.add(this);
    }

    public static ArrayList<KeywordExportData> exportAllKeywords(ExportHelper export) {
        ArrayList<KeywordExportData> keywords = new ArrayList<>();
        HashMap<String, KeywordExportData> keywordLookup = new HashMap<>();
        HashSet<String> parents = new HashSet<>();

        for (Map.Entry<String, String> kw : GameDictionary.keywords.entrySet()) {
            String name = kw.getKey();
            String parent = GameDictionary.parentWord.get(name);

            if (parent != null)
            {
                String proper = BaseMod.getKeywordProper(parent);
                if (proper != null)
                    name = proper;
            }

            if (parent != null && !parents.contains(parent)) {
                KeywordExportData keyword = new KeywordExportData(export, name, parent, kw.getValue());
                keywords.add(keyword);
                keywordLookup.put(parent, keyword);
                parents.add(parent);

                if (!kw.getKey().equals(parent)) {
                    keywordLookup.get(parent).names.add(kw.getKey());
                }
            }
            else if (parent != null) {
                keywordLookup.get(parent).names.add(kw.getKey());
            }
            else {
                KeywordExportData keyword = new KeywordExportData(export, name, kw.getKey(), kw.getValue());
                keywords.add(keyword);
                keywordLookup.put(kw.getKey(), keyword);
            }
        }
        Collections.sort(keywords);
        return keywords;
    }

    @Override
    public int compareTo(KeywordExportData that) {
        return name.compareTo(that.name);
    }
}