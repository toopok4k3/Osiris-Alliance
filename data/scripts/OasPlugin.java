package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import data.scripts.world.systems.Armin;
import data.scripts.world.systems.Bereer;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyManager;

public class OasPlugin extends BaseModPlugin {
	
	public static String OAS_PLUGIN_REGISTERED_KEY = "OasPlugin_registered"; 
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
			boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
			StarSystemAPI corvus = sector.getStarSystem("Corvus");
			boolean corvusMode = false;
			if(corvus != null) corvusMode = true;
			if(!haveNexerelin || corvusMode) {
				new Armin().generate(sector);
				new Bereer().generate(sector);
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
}
