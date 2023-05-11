package data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.lang.Math;
import java.awt.Color;
import java.util.EnumSet;

import data.scripts.OasUtil;
import data.scripts.OasUtil.Trail;
import data.scripts.OasUtil.TrailConfig;
import data.scripts.weapons.OasDoglegOnHitEffect;

public class OasMiningBlasterOnFireEffect /*extends BaseCombatLayeredRenderingPlugin*/ implements OnFireEffectPlugin, OnHitEffectPlugin {

	@Override
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
			ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (!shieldHit && target != null && projectile != null && projectile.getLocation() != null && target.getVelocity() != null) {
			OasDoglegOnHitEffect fx = new OasDoglegOnHitEffect(point, target, 0.18f + (Misc.random.nextFloat() * 0.04f), 1.0f);
			fx.setFractions(10f, 10f, 10f);
			CombatEntityAPI entity = engine.addLayeredRenderingPlugin(fx);
			entity.getLocation().set(point.x, point.y);
			if(target instanceof ShipAPI) {
				OasUtil.dealArmorDamage(projectile.getDamageAmount() * 0.5f, projectile, (ShipAPI)target, point);
			}
		}
	}

	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI combatEngine) {
		SpriteAPI sprite = Global.getSettings().getSprite("graphics/oas/fx/oas_benergy_core.png");
		TrailConfig configBright = new TrailConfig(sprite,
			new Color(1.0f, 0.6f, 0.6f, 1.0f),
			new Color(0.8f, 0.1f, 1.0f, 0.0f));
		configBright.setSegmentSpawnFrequency(10.0f);
		configBright.setWidth(10.0f);
		configBright.useInitialVelocityDamp();
		configBright.setFrequncies(3.0f, 1.5f);
		Trail trailBright = new Trail(projectile, configBright);
		TrailConfig configDark = new TrailConfig(sprite,
			new Color(0.7f, 0.1f, 0.3f, 1.0f),
			new Color(0.4f, 0.0f, 0.1f, 0.0f));
		configDark.setAmplitudes(8.0f, 15.0f);
		configDark.setDuration(0.35f);
		configDark.setWidth(80.0f);
		configDark.setTexSpeed(4.0f, 8.0f);
		configDark.setSegmentSpawnFrequency(2.5f);
		//configDark.setFrequncies(5.0f, 8.0f);
		Trail trailDark = new Trail(projectile, configDark);
		//OasMiningBlasterOnFireEffect trail = new OasMiningBlasterOnFireEffect(projectile);
		//CombatEntityAPI entity = combatEngine.addLayeredRenderingPlugin(trail);
		//entity.getLocation().set(projectile.getLocation());
	}

	//private final SpriteAPI sprite;
	//private final DamagingProjectileAPI projectile;
	//private final Trail trailBright;
	//private final Trail trailDark;

	// just the empty constructor that is used elsewhere
	/*public OasMiningBlasterOnFireEffect() {
		this.projectile = null;
		this.sprite = null;
		this.trailBright = null;
		this.trailDark = null;
	}

	public OasMiningBlasterOnFireEffect(DamagingProjectileAPI projectile) {
		this.sprite = Global.getSettings().getSprite("graphics/oas/fx/oas_benergy_core.png");
		this.projectile = projectile;
		TrailConfig configBright = new TrailConfig(sprite,
			new Color(1.0f, 0.6f, 0.6f, 1.0f),
			new Color(0.8f, 0.1f, 0.0f, 0.0f));
		configBright.setSegmentSpawnFrequency(30.0f);
		configBright.setWidth(20.0f);
		this.trailBright = new Trail(projectile, configBright);
		TrailConfig configDark = new TrailConfig(sprite,
			new Color(0.7f, 0.1f, 0.3f, 1.0f),
			new Color(0.4f, 0.0f, 0.1f, 0.0f));
		configDark.setAmplitudes(8.0f, 15.0f);
		configDark.setDuration(0.35f);
		configDark.setWidth(150.0f);
		//configDark.setSegmentSpawnFrequency(15.0f);
		//configDark.setFrequncies(5.0f, 8.0f);
		this.trailDark = new Trail(projectile, configDark);
	}

	public void advance(float amount) {
		if (Global.getCombatEngine().isPaused()) return;
		entity.getLocation().set(projectile.getLocation());
		if(trailBright != null) {
			trailBright.advance(amount);
		}
		if(trailDark != null) {
			trailDark.advance(amount);
		}
	}

	public float getRenderRadius() {
		return 400f; // we could do this smarter.
	}

	public boolean isExpired() {
		return this.projectile.isExpired() && trailBright.isExpired() && trailDark.isExpired();
		//return this.projectile.isExpired() || !Global.getCombatEngine().isEntityInPlay(this.projectile);
	}

	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		if(trailBright != null && trailDark != null) {
			trailBright.render(layer, viewport);
			trailDark.render(layer, viewport);
		}
	}

	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);

	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return layers;
	}*/
}
