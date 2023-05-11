package data.hullmods;

import java.util.HashMap;
import java.util.Map;
import java.lang.StringBuilder;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class OasAuxiliary extends BaseHullMod {
	private static class Bonus {
		private float dp, speed;
		public Bonus(float dp, float speed) {
			this.dp = dp;
			this.speed = speed;
		}
	}

	private static final float ROF_BONUS = 50.0f;

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, new Bonus(1.0f, 50.0f));
		mag.put(HullSize.DESTROYER, new Bonus(3.0f, 30.0f));
		mag.put(HullSize.CRUISER, new Bonus(4.0f, 20.0f));
		mag.put(HullSize.CAPITAL_SHIP, new Bonus(10.0f, 10.0f));
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		final ShipVariantAPI variant = stats.getVariant();
		if(variant != null) {
			final ShipHullSpecAPI hullSpec = variant.getHullSpec();
			
			if(variant.hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS) && mag.containsKey(hullSize)) {
				stats.getSuppliesPerMonth().modifyFlat(id, ((Bonus)mag.get(hullSize)).dp);
				stats.getSuppliesToRecover().modifyFlat(id, ((Bonus)mag.get(hullSize)).dp);
				stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, ((Bonus)mag.get(hullSize)).dp);
				stats.getDynamic().getMod(Stats.ACT_AS_COMBAT_SHIP).modifyFlat(id, 1.0f);
				stats.getMaxSpeed().modifyFlat(id, ((Bonus)mag.get(hullSize)).speed);
				stats.getBallisticRoFMult().modifyPercent(id, ROF_BONUS);
				stats.getVariant().getHints().remove(ShipHullSpecAPI.ShipTypeHints.CIVILIAN);
			//} else if(hullSpec != null && hullSpec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.CIVILIAN)) {
				// ensure we put it on again...? 
			} else if(!variant.getHints().contains(ShipTypeHints.CIVILIAN)) {
				variant.getHints().add(ShipTypeHints.CIVILIAN);
			}
			//}
		}
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float)((Bonus)mag.get(HullSize.FRIGATE)).dp).intValue();
		if (index == 1) return "" + ((Float)((Bonus)mag.get(HullSize.DESTROYER)).dp).intValue();
		if (index == 2) return "" + ((Float)((Bonus)mag.get(HullSize.CRUISER)).dp).intValue();
		if (index == 3) return "" + ((Float)((Bonus)mag.get(HullSize.CAPITAL_SHIP)).dp).intValue();
		if (index == 4) return "" + ((Float)((Bonus)mag.get(HullSize.FRIGATE)).speed).intValue();
		if (index == 5) return "" + ((Float)((Bonus)mag.get(HullSize.DESTROYER)).speed).intValue();
		if (index == 6) return "" + ((Float)((Bonus)mag.get(HullSize.CRUISER)).speed).intValue();
		if (index == 7) return "" + ((Float)((Bonus)mag.get(HullSize.CAPITAL_SHIP)).speed).intValue();
		if (index == 8) return "" + ((Float) ROF_BONUS).intValue()+"%";
		return null;
	}
}
