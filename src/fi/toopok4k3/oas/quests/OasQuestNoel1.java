package fi.toopok4k3.oas.quests;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class OasQuestNoel1 extends HubMissionWithSearch {

	private PersonAPI noel;

	public static enum Stage {
		KILL_TARGET,
		BACK_TO_NOEL,
		COMPLETED
	}
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission was already accepted by the player, abort
		if (!setGlobalReference("$oasQuestNoel1_ref")) {
			return false;
		}

		setCreditReward(10000, 10000);

		noel = getImportantPerson("noel_crow");
		if (noel == null) return false;

		MarketAPI harman = Global.getSector().getEconomy().getMarket("harman");
		if(harman == null) return false;

		setStartingStage(Stage.KILL_TARGET);
		addSuccessStages(Stage.COMPLETED);

		makeImportant(noel, "$oasQuestNoel1_contact", Stage.BACK_TO_NOEL);
		makeImportant(noel.getMarket(), null, Stage.BACK_TO_NOEL);
		setStoryMission();

		// trigger createfleet
		beginStageTrigger(Stage.KILL_TARGET);
		StarSystemAPI armin = noel.getMarket().getStarSystem();
		SectorEntityToken fringePoint = armin.getEntityById("armin_jump2");
		SectorEntityToken fugura = armin.getEntityById("fugura");
		for(SectorEntityToken entity : armin.getJumpPoints()) {
			if(!"armin_jump1".equals(entity.getId()) && !"armin_jump2".equals(entity.getId())) {
				fringePoint = entity;
			}
		}
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.HIGHER, Factions.PIRATES, FleetTypes.PERSON_BOUNTY_FLEET, armin);
		triggerAutoAdjustFleetStrengthModerate();
		triggerFleetSetName("Jump-point Menace");
		triggerFleetSetFlagship("oas_wayfarer2_p_standard");
		triggerPickLocationAroundEntity(fringePoint, 100.0f);
		triggerMakeAllFleetFlagsPermanent();
		triggerMakeFleetIgnoreOtherFleetsExceptPlayer();
		triggerMakeFleetIgnoredByOtherFleets();
		triggerMakeFleetGoAwayAfterDefeat();
		triggerSpawnFleetAtPickedLocation("$oasQuestNoel1_pirate", null);
		//triggerSetPatrol();
		triggerFleetSetPatrolLeashRange(200.0f);
		triggerFleetSetPatrolActionText("Enjoying the view.");
		triggerOrderFleetPatrol(true, fringePoint, fugura);
		triggerFleetMakeImportant(null, Stage.KILL_TARGET);
		triggerFleetAddDefeatTrigger("oasQuestNoel1PirateDefeated");
		final String fleetTag = "oasQuestNoel1fleet";
		triggerFleetAddTags(fleetTag);
		triggerCustomAction(QuestUtils.getWinConditionTrigger(this, fleetTag));
		endTrigger();
		
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$oasQuestNoel1_missionCompleted", true);
		endTrigger();
		
		//setStageOnGlobalFlag(Stage.BACK_TO_NOEL, "$oasQuestNoel1PirateDefeated");
		connectWithMemoryFlag(Stage.KILL_TARGET, Stage.BACK_TO_NOEL, noel, QuestUtils.getVictoryKey(this, fleetTag));
		setStageOnGlobalFlag(Stage.COMPLETED, "$oasQuestNoel1GotReward");

		return true;
	}

	public static void showIntel(TextPanelAPI text) {
		PersonAPI noel = Global.getSector().getImportantPeople().getData("noel_crow").getPerson();
		if (noel == null) return;
		
		float opad = 10f;
		List<FleetMemberAPI> list = new ArrayList<FleetMemberAPI>();
		list.add(QuestUtils.createFakeMember("oas_wayfarer2_p_standard"));
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
		if(currentStage == Stage.KILL_TARGET) {
			info.addPara("Defeat the pirate fleet that usually hangs around Fugura or the fringe jump point in the Armin system", 10.0f);
		}
		if(currentStage == Stage.BACK_TO_NOEL) {
			info.addPara("Get back to the Harman Station talk to "+noel.getNameString()+ " to receive your reward", 10.0f);
			addStandardMarketDesc("Noel Crow is located "+ noel.getMarket().getOnOrAt(), noel.getMarket(), info, 10.0f);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		if(currentStage == Stage.KILL_TARGET) {
			info.addPara("Find and Destroy the menace at fringe jump-point, the fleet ports at Fugura ", tc, pad);
		} else if(currentStage == Stage.BACK_TO_NOEL) {
			info.addPara("Get back to Noel Crow for your reward.", tc, pad);
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Fringe Jump-point Menace";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}
