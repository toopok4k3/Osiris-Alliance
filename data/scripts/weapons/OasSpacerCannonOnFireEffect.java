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

public class OasSpacerCannonOnFireEffect implements OnFireEffectPlugin {
	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		projectile.getDamage().setSoftFlux(true);
		/*SpriteAPI sprite = Global.getSettings().getSprite("graphics/oas/fx/oas_benergy_core.png");
		TrailConfig config = new TrailConfig(sprite,
				new Color(0.9f, 0.9f, 1.0f, 1.0f),
				new Color(0.0f, 0.0f, 0.7f, 0.0f));
		config.setTexSpeed(2.0f, 0.5f);
		config.setAcceleration(-1000.0f);
		config.setDuration(0.5f);
		config.setWidth(4.0f*projectile.getProjectileSpec().getWidth());
		config.setFrequncies(0.5f, 0.0f);
		config.setSegmentSpawnFrequency(2.0f);
		//config.setMaxSegments(50);
		config.setAmplitudes(1.0f, 0.0f);
		Trail trail = new Trail(projectile, config);*/
	}
}
