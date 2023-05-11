package fi.toopok4k3.oas.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

import fi.toopok4k3.oas.quests.OasQuestNoel1;
import fi.toopok4k3.oas.quests.OasQuestNoel2;

// enables us to show some intel before mission has been started
public class OasShowQuestIntel extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
        if(params.isEmpty() ) return false;
		String text = params.get(0).getString(memoryMap);
        if("noel1".equals(text)) {
            OasQuestNoel1.showIntel(dialog.getTextPanel());
        } else if("noel2".equals(text)) {
            OasQuestNoel2.showIntel(dialog.getTextPanel());
        } else {
            dialog.getTextPanel().addPara("OasShowQuestIntel no intel code for mission: "+text);
            dialog.getTextPanel().addPara("This is an error message that should not be visible unless something is wrong.");
        }
		return true;
	}
}
