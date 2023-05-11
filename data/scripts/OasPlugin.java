package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import data.scripts.world.systems.Armin;
import data.scripts.world.systems.Bereer;
import data.scripts.world.systems.Fearia;
import data.scripts.weapons.ai.OasMissileSnapperAi;
import data.scripts.weapons.ai.OasMissileSmartbombAi;
import data.scripts.weapons.ai.OasMissileOrbAi;
import fi.toopok4k3.oas.OasLoadNormals;

import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyManager;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class OasPlugin extends BaseModPlugin {
	
	public static String OAS_PLUGIN_REGISTERED_KEY = "OasPlugin_registered"; 

	@Override
	public void onApplicationLoad() throws Exception {
		super.onApplicationLoad();
		OasLoadNormals.loadNormals();
	}

	@Override
	public void onEnabled(boolean wasEnabled) {		
		//initOasPlugin();
	}

	@Override
	public void onNewGame() {
		initOasPlugin();
	}

	public void initOasPlugin() {
		if (!Global.getSector().getPersistentData().containsKey(OAS_PLUGIN_REGISTERED_KEY)) {
			Global.getSector().getPersistentData().put(OAS_PLUGIN_REGISTERED_KEY, true);
			SectorAPI sector = Global.getSector();
			//OasCampaignEventListener campaignEvents = new OasCampaignEventListener();
			//OasCampaignPlugin campaignPlugin = new OasCampaignPlugin();
			//sector.registerPlugin(campaignPlugin);
			boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
			StarSystemAPI corvus = sector.getStarSystem("Corvus");
			boolean corvusMode = false;
			if(corvus != null) corvusMode = true;
			if(!haveNexerelin || corvusMode) {
				new Armin().generate(sector);
				new Bereer().generate(sector);
				//new Fearia().generate(sector);// it's not ready yet
			}
			FactionAPI player = sector.getFaction("player");
			//player.setRelationship(osiris.getId(), 0.2f); // to get commission for debugging
			FactionAPI hegemony = sector.getFaction("hegemony");
			FactionAPI tritachyon = sector.getFaction("tritachyon");
			FactionAPI pirates = sector.getFaction("pirates");
			FactionAPI church = sector.getFaction("luddic_church");
			FactionAPI path = sector.getFaction("luddic_path");
			FactionAPI indep = sector.getFaction("independent");
			FactionAPI diktat = sector.getFaction("sindrian_diktat");
			FactionAPI persean = sector.getFaction("persean");
			FactionAPI remnant = sector.getFaction("remnant");
			FactionAPI osiris = sector.getFaction("osiris");
			osiris.setRelationship(hegemony.getId(), RepLevel.SUSPICIOUS);
			osiris.setRelationship(tritachyon.getId(), RepLevel.SUSPICIOUS);
			osiris.setRelationship(pirates.getId(), RepLevel.HOSTILE);
			osiris.setRelationship(church.getId(), RepLevel.NEUTRAL);
			osiris.setRelationship(path.getId(), RepLevel.HOSTILE);
			osiris.setRelationship(indep.getId(), RepLevel.FAVORABLE);
			osiris.setRelationship(diktat.getId(), RepLevel.SUSPICIOUS);
			osiris.setRelationship(persean.getId(), RepLevel.NEUTRAL);
			osiris.setRelationship(remnant.getId(), RepLevel.HOSTILE);
		}
	}

	@Override
	public void onNewGameAfterTimePass() {
		MarketAPI harman = Global.getSector().getEconomy().getMarket("harman");
		if(harman != null) {
			SystemBountyManager.getInstance().addOrResetBounty(harman);
		}
	}

	@Override
	public void onNewGameAfterEconomyLoad() {
		createPeople();
	}

	private static void createPeople() {
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		MarketAPI market = null;

		market =  Global.getSector().getEconomy().getMarket("suns_gate");
		if(market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId("osiris_sun");
			person.setFaction("osiris");
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Osiris");
			person.getName().setLast("Sun");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "osiris_sun"));
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
		}
		market =  Global.getSector().getEconomy().getMarket("harman");
		if(market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId("noel_crow");
			person.setFaction("osiris");
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.SPACE_COMMANDER);
			person.setPostId(Ranks.POST_SUPPLY_OFFICER);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Noel");
			person.getName().setLast("Crow");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "noel_crow"));
			person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
			person.addTag(Tags.CONTACT_MILITARY);
			person.getMemoryWithoutUpdate().set(BaseMissionHub.NUM_BONUS_MISSIONS, 1);
			//market.setAdmin(person);
			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			ip.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(false);
		}
	}

	@Override
	public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
		PluginPick<MissileAIPlugin> pick = null;
		if("oas_kinetic_missile".equals(missile.getProjectileSpecId())) {
			OasMissileSnapperAi ai = new OasMissileSnapperAi(missile, launchingShip);
			pick = new PluginPick<MissileAIPlugin>(ai, PickPriority.HIGHEST);
		} else if("oas_m_smartbomb_shot".equals(missile.getProjectileSpecId())) {
			OasMissileSmartbombAi ai = new OasMissileSmartbombAi(missile, launchingShip);
			pick = new PluginPick<MissileAIPlugin>(ai, PickPriority.HIGHEST);
		} else if("oas_s_smartbomb_shot".equals(missile.getProjectileSpecId())) {
			OasMissileSmartbombAi ai = new OasMissileSmartbombAi(missile, launchingShip);
			pick = new PluginPick<MissileAIPlugin>(ai, PickPriority.HIGHEST);
		} else if("oas_l_orb_missile_shot".equals(missile.getProjectileSpecId())) {
			OasMissileOrbAi ai = new OasMissileOrbAi(missile, launchingShip);
			pick = new PluginPick<MissileAIPlugin>(ai, PickPriority.HIGHEST);
		}
		return pick;
	}

	/*public class OasCampaignEventListener extends BaseCampaignEventListener{
		public OasCampaignEventListener() {
			super(true);
		}

		public void reportFleetSpawned(CampaignFleetAPI fleet) {
			if(fleet.getFaction() != null && "ameg".equals(fleet.getFaction().getId())) {
				fleet.setFaction("osiris");
				fleet.setNoFactionInName(true);
			}
		}
	}*/

	/*public class OasCampaignPlugin extends BaseCampaignPlugin {
		public String getId() {
			return "oasCampaignPlugin";
		}

		public com.fs.starfarer.api.PluginPick<com.fs.starfarer.api.campaign.ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object action, String factionId) {
			if("ameg".equals(factionId)) {
				return new PluginPick<ReputationActionResponsePlugin>(new ReputationActionResponsePlugin() {
					public com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult handlePlayerReputationAction(Object arg0, PersonAPI arg1) {

					}
					public com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult handlePlayerReputationAction(Object arg0, String arg1) {

					}
					
				}, PickPriority.MOD_GENERAL);
			}
			return null;
		}
	}*/
}