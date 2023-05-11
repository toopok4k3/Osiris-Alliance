package fi.toopok4k3.oas.quests;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerActionContext;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class OasQuestNoel3 /*extends HubMissionWithSearch */{
/*
	private PersonAPI noel;

	public static enum Stage {
		CONTACT_AGENT,
		BACK_TO_NOEL,
		COMPLETED
	}
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission was already accepted by the player, abort
		if (!setGlobalReference("$oasQuestNoel3_ref")) {
			return false;
		}

		noel = getImportantPerson("noel_crow");
		if (noel == null) return false;

		MarketAPI harman = Global.getSector().getEconomy().getMarket("harman");
		if(harman == null) return false;

		setStartingStage(Stage.CONTACT_AGENT);
		addSuccessStages(Stage.COMPLETED);

		
		return true;
	}

	public static void showIntel(TextPanelAPI text) {
		PersonAPI noel = Global.getSector().getImportantPeople().getData("noel_crow").getPerson();
		if (noel == null) return;
		
		float opad = 10f;
		List<FleetMemberAPI> list = new ArrayList<FleetMemberAPI>();
		list.add(QuestUtils.createFakeMember("toopo_scorpion_standard"));
		int cols = 3; // default was 7
		float iconSize = 440 / cols;
		
		Global.getSector().getFaction("pirates");
		if (!list.isEmpty()) {
			TooltipMakerAPI info = text.beginTooltip();
			info.setParaSmallInsignia();
			info.addPara(Misc.ucFirst(noel.getHeOrShe()) + " taps a data pad, and an intel assessment shows up on your tripad.", 0f);
			//void addShipList(int cols, int rows, float iconSize, Color baseColor, List<FleetMemberAPI> ships, float pad);
			info.addShipList(cols, 1, iconSize, Global.getSector().getFaction("pirates").getBaseUIColor(), list, opad);
			info.addPara("The assessment contains a warning that the current fleet size is unknown and may contain numerous additional ships.", opad);
			text.addTooltip();
		}
		return;
	}

	@Override
	public void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height) {
		if(currentStage == Stage.CONTACT_AGENT) {
			info.addPara("Find and destroy the Pirate's Nest.", 10.0f);
		} else if(currentStage == Stage.BACK_TO_NOEL) {
			info.addPara("Get back to the Harman Station talk to "+noel.getNameString()+ " to receive your reward", 10.0f);
			addStandardMarketDesc("Noel Crow is located "+ noel.getMarket().getOnOrAt(), noel.getMarket(), info, 10.0f);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		if(currentStage == Stage.CONTACT_AGENT) {
			info.addPara("Contact secret Osiris agent at Fugura to find out possible intel on how to find your target.", tc, pad);
		} else if(currentStage == Stage.BACK_TO_NOEL) {
			info.addPara("Get back to Noel Crow for your reward.", tc, pad);
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Gathering Evidence";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
 */
}
