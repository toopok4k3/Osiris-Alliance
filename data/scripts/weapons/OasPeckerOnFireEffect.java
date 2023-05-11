package data.scripts.weapons;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

import data.scripts.OasUtil.Trail;
import data.scripts.OasUtil.TrailConfig;

public class OasPeckerOnFireEffect implements OnFireEffectPlugin {
	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        SpriteAPI sprite = Global.getSettings().getSprite("graphics/oas/fx/oas_benergy_core.png");
		TrailConfig configBright = new TrailConfig(sprite,
			new Color(1.0f, 0.6f, 0.6f, 1.0f),
			new Color(0.8f, 0.1f, 1.0f, 0.0f));
		configBright.setSegmentSpawnFrequency(10.0f);
		configBright.setWidth(6.0f);
		configBright.setDuration(0.3f);
		configBright.setAcceleration(-10000.0f);
		configBright.useInitialVelocityDamp();
		Trail trailBright = new Trail(projectile, configBright);
		TrailConfig configDark = new TrailConfig(sprite,
			new Color(0.7f, 0.1f, 0.3f, 1.0f),
			new Color(0.4f, 0.0f, 0.1f, 0.0f));
		configDark.setAmplitudes(4.0f, 7.5f);
		configDark.setDuration(0.25f);
		configDark.setWidth(60.0f);
		configDark.setTexSpeed(4.0f, 8.0f);
		configDark.setSegmentSpawnFrequency(5.0f);
		configDark.setAcceleration(-10000.0f);
		configDark.useInitialVelocityDamp();
		Trail trailDark = new Trail(projectile, configDark);
	}
}
