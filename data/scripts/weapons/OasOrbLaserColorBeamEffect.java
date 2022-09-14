package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import java.awt.Color;

public class OasOrbLaserColorBeamEffect implements BeamEffectPlugin {

	private Color startCoreColor = null;
	private Color startFringeColor = null;
	private boolean wasZero = true;
	private IntervalUtil damageInterval = new IntervalUtil(0.1f, 0.1f);
	final static private Color endCoreColor = new Color(1.0f, 0.53f, 0.59f, 1.0f);
	final static private Color endFringeColor = new Color(1.0f, 0.0f, 0.4f, 0.66f);

	@Override
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (engine.isPaused()) return;
		ShipAPI ship = beam.getSource();
		if (ship == null) return;
		ShipSystemAPI system = ship.getSystem();
		if (system == null) return;

		final float effectLevel = system.getEffectLevel();
		final boolean systemOn = effectLevel > 0.0f && !ship.isHulk();

		if(startCoreColor == null) startCoreColor = beam.getCoreColor();
		if(startFringeColor == null) startFringeColor = beam.getFringeColor();

		final boolean applyAdditionalDamage = false;
		final CombatEntityAPI target = beam.getDamageTarget();
		if (target != null /*&& beam.getBrightness() >= 1f*/) {
			applyAdditionalDamage = true;
			/*float dur = beam.getDamage().getDpsDuration();
			if (!wasZero) dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			damageInterval.advance(dur);
			if (damageInterval.intervalElapsed()) {
				applyAdditionalDamage = true;
			}*/
		}

		if (systemOn) {
			final Color currentCoreColor = fadeColor(effectLevel, startCoreColor, endCoreColor);
			final Color currentFringeColor = fadeColor(effectLevel, startFringeColor, endFringeColor);
			beam.setCoreColor(currentCoreColor);
			beam.setFringeColor(currentFringeColor);
			if(applyAdditionalDamage) {
				engine.applyDamage(target, beam.getRayEndPrevFrame(), beam.getDamage().computeDamageDealt(amount), beam.getDamage().getType(), 0.0f,
				false, true, ship, false);
			}
		} else {
			beam.setCoreColor(startCoreColor);
			beam.setFringeColor(startFringeColor);
		}
	}

	// stage is from 0 to 1
	private static Color fadeColor(final float stage, final Color source, final Color target) {
		float[] s = source.getComponents(null);
		float[] t = target.getComponents(null);
		float[] r = new float[4];
		Color retval = source;
		if (s.length == 4 && t.length == 4) {
			for (int i = 0; i < 4; i++) {
				r[i] = ((t[i] - s[i]) * stage) + s[i];
			}
			retval = new Color(r[0], r[1], r[2], r[3]);
		}
		return retval;
	}
}