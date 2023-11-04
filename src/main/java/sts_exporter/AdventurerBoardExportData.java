package sts_exporter;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.Loader;
import theFishing.boards.AbstractBoard;

import java.util.ArrayList;
import java.util.HashMap;

public class AdventurerBoardExportData {
    public String id;
    public String name;
    public String specialRule;
    public String effects;
    public ModExportData mod;

    AdventurerBoardExportData(ExportHelper export, String id) {
        AbstractBoard board = AbstractBoard.getBoardByID(id);
        this.id = id;
        this.name = board.name;
        this.specialRule = RelicExportData.smartTextToPlain(board.getSpecialRule(),true,true);
        this.effects = RelicExportData.smartTextToPlain(ReflectionHacks.privateMethod(AbstractBoard.class, "getEffectsText").invoke(board), true, true);
        this.mod = export.findMod(board.getClass());
        this.mod.adventurerBoards.add(this);
    }

    public static ArrayList<AdventurerBoardExportData> exportAllBoards(ExportHelper export) {
        ArrayList<AdventurerBoardExportData> boards = new ArrayList<>();
        if (Loader.isModLoaded("fishing"))
            for (String boardID : ((HashMap<String, Object>)ReflectionHacks.getPrivateStatic(AbstractBoard.class, "ids")).keySet())
                boards.add(new AdventurerBoardExportData(export, boardID));
        return boards;
    }
}