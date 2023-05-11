package fi.toopok4k3.oas.quests;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
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
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class OasQuestNoel2 extends HubMissionWithSearch {

	private PersonAPI noel;
	private SectorEntityToken station;

	public static enum Stage {
		CONTACT_AGENT,
		USE_RELAY,
		KILL_TARGET,
		INSPECT_STATION,
		BACK_TO_NOEL,
		COMPLETED
	}
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission was already accepted by the player, abort
		if (!setGlobalReference("$oasQuestNoel2_ref")) {
			return false;
		}

		setCreditReward(CreditReward.VERY_LOW);
		noel = getImportantPerson("noel_crow");
		if (noel == null) return false;

		MarketAPI harman = Global.getSector().getEconomy().getMarket("harman");
		if(harman == null) return false;
		MarketAPI fugura = Global.getSector().getEconomy().getMarket("fugura");
		if(fugura == null) return false;
		final StarSystemAPI armin = noel.getMarket().getStarSystem();
		if(armin == null) return false;
		station = armin.getEntityById("harman_abandoned_station");
		if(station == null) return false;
		SectorEntityToken commRelay = armin.getEntityById("armin_relay");

		setStartingStage(Stage.CONTACT_AGENT);
		addSuccessStages(Stage.COMPLETED);

		PersonAPI fuguraAgent = findOrCreatePerson("osiris", fugura, true, Ranks.AGENT, Ranks.POST_AGENT);
		if(fuguraAgent == null) {
			return false;
		}

		makeImportant(fuguraAgent, "$oasQuestNoel2_agent", Stage.CONTACT_AGENT);
		makeImportant(noel, "$oasQuestNoel2_contact", Stage.BACK_TO_NOEL);
		makeImportant(noel.getMarket(), null, Stage.BACK_TO_NOEL);
		makeImportant(station, "$oasQuestNoel2_inspect", Stage.INSPECT_STATION);
		//makeImportant(station, null, Stage.KILL_TARGET);
		makeImportant(commRelay, "$oasQuestNoel2GotIntel", Stage.USE_RELAY);
		setStoryMission();

		// let's spawn a random pirate patrol around the relay just for fun
		beginStageTrigger(Stage.USE_RELAY);
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.DEFAULT, Factions.PIRATES, FleetTypes.PATROL_SMALL, armin);
		triggerAutoAdjustFleetStrengthMajor();
		triggerPickLocationAroundEntity(commRelay, 100.0f);
		triggerMakeAllFleetFlagsPermanent();
		triggerSpawnFleetAtPickedLocation(null, null);
		triggerFleetSetPatrolLeashRange(500.0f);
		triggerOrderFleetPatrol(false, commRelay);
		
		endTrigger();

		beginStageTrigger(Stage.KILL_TARGET);
		triggerCreateFleet(FleetSize.TINY, FleetQuality.VERY_HIGH, Factions.PIRATES, FleetTypes.PERSON_BOUNTY_FLEET, armin);
		triggerAutoAdjustFleetStrengthModerate();
		triggerFleetSetName("Scorpion Platform");
		triggerFleetSetFlagship("toopo_scorpion_standard");
		triggerPickLocationAroundEntity(station, 100.0f);
		triggerMakeAllFleetFlagsPermanent();
		triggerMakeFleetIgnoreOtherFleets();
		triggerMakeFleetIgnoredByOtherFleets();
		triggerSpawnFleetAtPickedLocation("$oasQuestNoel2_pirate", null);
		//triggerSetPatrol();
		triggerFleetSetPatrolLeashRange(50.0f);
		triggerFleetSetPatrolActionText("Hiding in plain sight");
		triggerOrderFleetPatrol(false, station);
		triggerFleetMakeImportant(null, Stage.KILL_TARGET);
		triggerFleetAddDefeatTrigger("oasQuestNoel2PirateDefeated");
		final String fleetTag = "oasQuestNoel2fleet";
		triggerFleetAddTags(fleetTag);
		triggerCustomAction(QuestUtils.getWinConditionTrigger(this, fleetTag));
		endTrigger();
		
		beginStageTrigger(Stage.INSPECT_STATION);
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.VERY_HIGH, Factions.PIRATES, FleetTypes.PERSON_BOUNTY_FLEET, armin);
		triggerAutoAdjustFleetStrengthModerate();
		triggerFleetSetName("Returning Raiding party");
		triggerFleetSetFlagship("toopo_ecuador_p_starter");
		triggerPickLocationTowardsPlayer(harman.getPrimaryEntity(), 5.0f, 500.0f);
		//triggerPickLocationAroundEntity(station, 100.0f);
		triggerMakeAllFleetFlagsPermanent();
		triggerMakeFleetIgnoreOtherFleetsExceptPlayer();
		triggerMakeFleetIgnoredByOtherFleets();
		triggerSpawnFleetAtPickedLocation();
		//triggerSetPatrol();
		//triggerFleetSetPatrolLeashRange(50.0f);
		triggerOrderFleetInterceptPlayer();
		triggerFleetMakeImportant("$oasQuestNoel2Pirate2", Stage.INSPECT_STATION, Stage.BACK_TO_NOEL);
		triggerFleetAddDefeatTrigger("oasQuestNoel2PirateReturnersDefeated");

		triggerCustomAction(new QuestUtils.AddShipToFleet("toopo_eraser_p_strike"));
		triggerCustomAction(new QuestUtils.AddShipToFleet("toopo_eraser_p_strike"));
		triggerCustomAction(new QuestUtils.AddShipToFleet("oas_raccoon_p_strike"));
		triggerCustomAction(new QuestUtils.AddShipToFleet("oas_raccoon_p_strike"));
		
		triggerRunScriptAfterDelay(0.0f, new Script() {
			@Override
			public void run() {
				if(station.getMarket() != null && station.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE) != null) {
					station.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "toopo_bucket2_standard", "Power Beef");
					station.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("fuel", 200.0f);
					station.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("supplies", 50.0f);
				}
			}
		});
		
		endTrigger();

		setStageOnMemoryFlag(Stage.USE_RELAY, fuguraAgent, "$gotIntel");
		
		setStageOnGlobalFlag(Stage.KILL_TARGET, "$oasQuestNoel2UsedRelay");
		//setStageOnGlobalFlag(Stage.INSPECT_STATION, "$oasQuestNoel2PirateDefeated");
		connectWithMemoryFlag(Stage.KILL_TARGET, Stage.INSPECT_STATION, noel, QuestUtils.getVictoryKey(this, fleetTag));
		setStageOnGlobalFlag(Stage.BACK_TO_NOEL, "$oasQuestNoel2StationInspected");
		setStageOnGlobalFlag(Stage.COMPLETED, "$oasQuestNoel2GotReward");
		//setStageInRangeOfEntity(Stage.KILL_TARGET, station, 250.0f);

		//beginStageTrigger(Stage.USE_RELAY);
		//triggerSetGlobalMemoryValue("$oasQuestNoel2GotIntel", true);
		//endTrigger();

		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$oasQuestNoel2_missionCompleted", true);
		endTrigger();

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
		if(currentStage == Stage.CONTACT_AGENT || currentStage == Stage.KILL_TARGET || currentStage == Stage.USE_RELAY) {
			info.addPara("Find and destroy the Pirate's Nest.", 10.0f);
		} else if(currentStage == Stage.INSPECT_STATION) {
			info.addPara("Inspect the abandoned station.", 10.0f);
		} else if(currentStage == Stage.BACK_TO_NOEL) {
			info.addPara("Get back to the Harman Station talk to "+noel.getNameString()+ " to receive your reward", 10.0f);
			addStandardMarketDesc("Noel Crow is located "+ noel.getMarket().getOnOrAt(), noel.getMarket(), info, 10.0f);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		if(currentStage == Stage.CONTACT_AGENT) {
			info.addPara("Contact secret Osiris agent at Fugura to find out possible intel on how to find your target.", tc, pad);
		} else if(currentStage == Stage.USE_RELAY) {
			info.addPara("Use the Armin Relay to locate the Pirate's nest with the frequencies given by the agent", tc, pad);
		} else if(currentStage == Stage.KILL_TARGET) {
			info.addPara("Destroy the Pirate's Nest at the Abandoned Mining Station orbiting Harman's Belt.", tc, pad);
		} else if(currentStage == Stage.INSPECT_STATION) {
			info.addPara("Inspect the abandoned station.", tc, pad);
		} else if(currentStage == Stage.BACK_TO_NOEL) {
			info.addPara("Get back to Noel Crow for your reward.", tc, pad);
		}
		return false;
	}

	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
			Map<String, MemoryAPI> memoryMap) {
		if("showStation".equals(action)) {
			station.setSensorProfile(10000.0f);
			return true;
		}
		if("seeStationPermanently".equals(action)) {
			//station.setDiscoverable(false);
			Misc.setSeen(station.getMarket(), null, false);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "The Pirate's nest";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}
