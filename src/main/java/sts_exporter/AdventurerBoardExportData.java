package sts_exporter;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.Loader;
import theFishing.boards.AbstractBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AdventurerBoardExportData {
    public String id;
    public String name;
    public String type;
    public String specialRule;
    public ArrayList<String> effects;
    public ModExportData mod;

    AdventurerBoardExportData(ExportHelper export, String id, AbstractBoard board, String type) {
        this.id = id;
        this.name = board.name;
        this.type = type;
        this.specialRule = RelicExportData.smartTextToPlain(board.getSpecialRule(),true,true);
        this.effects = new ArrayList<>(board.effects.stream().map(e -> RelicExportData.smartTextToPlain(e.description,true,true)).collect(Collectors.toList()));
        this.mod = export.findMod(board.getClass());
        this.mod.adventurerBoards.add(this);
    }

    public static ArrayList<AdventurerBoardExportData> exportAllBoards(ExportHelper export) {
        ArrayList<AdventurerBoardExportData> boards = new ArrayList<>();
        if (Loader.isModLoaded("fishing")) {
            for (String boardID : ((HashMap<String, Object>)ReflectionHacks.getPrivateStatic(AbstractBoard.class, "ids")).keySet())
                boards.add(new AdventurerBoardExportData(export, boardID, AbstractBoard.getBoardByID(boardID), "Regular"));
            for (String boardID : ((HashMap<String, Object>)ReflectionHacks.getPrivateStatic(AbstractBoard.class, "complexIds")).keySet())
                boards.add(new AdventurerBoardExportData(export, boardID, AbstractBoard.getBoardByID(boardID), "Complex"));
        }
        return boards;
    }
}