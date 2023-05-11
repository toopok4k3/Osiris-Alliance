package fi.toopok4k3.oas.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerActionContext;
import com.fs.starfarer.api.util.Misc;

public class QuestUtils {
    public static FleetMemberAPI createFakeMember(String variantId) {
		return createFakeMember(variantId, null);
	}

	public static FleetMemberAPI createFakeMember(String variantId, PersonAPI captain) {
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId);
		if(captain != null) {
			member.setCaptain(captain);
		}
		return member;
	}

	public static MissionTrigger.TriggerAction getWinConditionTrigger(HubMissionWithTriggers quest, String tag) {
		return new OasWincondition(new QuestBattleWinnerDecision(quest, tag));
	}

	public static String getVictoryKey(HubMissionWithTriggers quest, String tag) {
		if(quest == null) return "$oas_error_stuff";
		return "$" + quest.getMissionId() +"_" + tag + "_victory";
	}

	public static String getFailureKey(HubMissionWithTriggers quest, String tag) {
		if(quest == null) return "$oas_error_stuff";
		return "$" + quest.getMissionId() +"_" + tag + "_failed";
	}

    public static class AddShipToFleet implements MissionTrigger.TriggerAction {
		final private String variantId;
		final private String shipName;

		public AddShipToFleet(String variantId) {
			this.variantId = variantId;
			this.shipName = null;
		}

		@Override
		public void doAction(TriggerActionContext context) {
			if(context == null) return;
			if(context.fleet == null) return;
			CampaignFleetAPI fleet = context.fleet;
			FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId);
			final String name;
			if(shipName != null) {
				name = shipName;
			} else {
				name = fleet.getFleetData().pickShipName(member, Misc.random);
			}
			member.setShipName(name);
			fleet.getFleetData().addFleetMember(member);
			fleet.getFleetData().sort();
            member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
			fleet.forceSync();
		}
	}

	public static class QuestBattleWinnerDecision implements FleetEventListener {

		private final HubMissionWithTriggers quest;
		private final String tag;

		public QuestBattleWinnerDecision(HubMissionWithTriggers quest, String tag) {
			this.quest = quest;
			this.tag = tag;
		}

		/**
		 * "fleet" will be null if the listener is registered with the ListenerManager, and non-null
		 * if the listener is added directly to a fleet.
		 * @param fleet
		 * @param primaryWinner
		 * @param battle
		 */
		@Override
		public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
			if(quest.isDone() || quest.getResult() != null) return;
			if(fleet == null) return;
			if(!fleet.hasTag(tag)) return;			
			// this is default implementation of figuring out the winner.
			final boolean flagShipDestroyed;
			if (fleet.getFlagship() == null) {
				flagShipDestroyed = true;
			} else {
				flagShipDestroyed = false;
			}
			// we want player to be involved as the fleets are ignoring/ignored by everyone else.
			if(!battle.isInvolved(fleet) || !battle.isPlayerInvolved() || battle.onPlayerSide(fleet)) {
				return;
			}
			if(!flagShipDestroyed) { // need to destroy flagship...
				return;
			}
			if(quest.getPerson() != null) {
				quest.getPerson().getMemoryWithoutUpdate().set(QuestUtils.getVictoryKey(quest, tag), true);
			}
		}

		@Override
		public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
			if(quest.isDone() || quest.getResult() != null) return;
			if(fleet == null) return;
			if(fleet.hasTag(tag)) {
				if(quest.getPerson() != null) {
					quest.getPerson().getMemoryWithoutUpdate().set(QuestUtils.getFailureKey(quest, tag), true);
				}
			}
		}
	}

	public static class OasWincondition implements MissionTrigger.TriggerAction {

		private final FleetEventListener listener;

		public OasWincondition(FleetEventListener listener) {
			this.listener = listener;
		}

		@Override
		public void doAction(TriggerActionContext context) {
			if(context == null) return;
			if(context.fleet == null) return;
			CampaignFleetAPI fleet = context.fleet;
			if(listener != null && fleet != null) {
				fleet.addEventListener(listener);
			}
		}
	}
}
