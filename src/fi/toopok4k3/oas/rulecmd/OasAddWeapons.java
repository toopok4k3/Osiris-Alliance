package fi.toopok4k3.oas.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

// based on the vanilla AddRemoveCommodity
public class OasAddWeapons extends BaseCommandPlugin {

	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
			Map<String, MemoryAPI> memoryMap) {
		if (dialog == null)
			return false;

		String weaponId = params.get(0).getString(memoryMap);
		float quantity = 0;
		int next = 2;
		if (params.get(1).isOperator()) {
			quantity = -1 * params.get(2).getFloat(memoryMap);
			next = 3;
		} else {
			quantity = params.get(1).getFloat(memoryMap);
		}
		boolean withText = Math.abs(quantity) >= 1;
		if (dialog != null && params.size() >= next + 1) {
			withText = params.get(next).getBoolean(memoryMap) && withText;
		}

		if (quantity > 0) {
			Global.getSector().getPlayerFleet().getCargo().addWeapons(weaponId, Math.round(quantity));
			if (withText) {
				addWeaponGainText(weaponId, (int) quantity, dialog.getTextPanel());
			}
		} else {
			Global.getSector().getPlayerFleet().getCargo().removeWeapons(weaponId, Math.round(quantity));
			if (withText) {
				addWeaponLossText(weaponId, (int) Math.abs(quantity), dialog.getTextPanel());
			}
		}

		// there's not $weaponid memory so no need to update memory like in AddRemoveCommodity class
		return true;
	}

	public static void addWeaponGainText(String weaponId, int quantity, TextPanelAPI text) {
		WeaponSpecAPI weaponSpec = Global.getSettings().getWeaponSpec(weaponId);
		text.setFontSmallInsignia();
		String name = weaponSpec.getWeaponName();
		text.addParagraph("Gained " + Misc.getWithDGS(quantity) + Strings.X + " " + name + "", Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), Misc.getWithDGS(quantity) + Strings.X);
		text.setFontInsignia();
	}
	
	public static void addWeaponLossText(String weaponId, int quantity, TextPanelAPI text) {
		WeaponSpecAPI weaponSpec = Global.getSettings().getWeaponSpec(weaponId);
		text.setFontSmallInsignia();
		String name = weaponSpec.getWeaponName();
		text.addParagraph("Lost " + Misc.getWithDGS(quantity) + Strings.X + " " + name + "", Misc.getNegativeHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), Misc.getWithDGS(quantity) + Strings.X);
		text.setFontInsignia();
	}
}
