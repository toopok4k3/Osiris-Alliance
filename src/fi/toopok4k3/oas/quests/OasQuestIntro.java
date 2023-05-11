package fi.toopok4k3.oas.quests;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

// yoinked GAIntro
public class OasQuestIntro extends HubMissionWithSearch {

	private PersonAPI noel;

	public static enum Stage {
		GO_TO_HARMAN,
		COMPLETED,
	}

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$oasQuestIntro_ref")) {
			return false;
		}
		
		noel = getImportantPerson("noel_crow");
		if (noel == null) return false;

		MarketAPI harman = Global.getSector().getEconomy().getMarket("harman");
		if(harman == null) return false;

		setStartingStage(Stage.GO_TO_HARMAN);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		
		makeImportant(noel, "$oasQuestIntro_contact", Stage.GO_TO_HARMAN);
		makeImportant(noel.getMarket(), null, Stage.GO_TO_HARMAN);
		setStageOnGlobalFlag(Stage.COMPLETED, "$oasQuestIntro_completed");
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		return true;
	}

	@Override
	protected void updateInteractionDataImpl() {
	}

	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		if(currentStage == Stage.GO_TO_HARMAN) {
			info.addPara("Go to the Harman Station and meet "+noel.getNameString(), 10.0f);
			addStandardMarketDesc("Noel Crow is located "+ noel.getMarket().getOnOrAt(), noel.getMarket(), info, 10.0f);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		if(currentStage == Stage.GO_TO_HARMAN) {
			info.addPara("Go to the Harman Station", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Talk to Noel Crow";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}
