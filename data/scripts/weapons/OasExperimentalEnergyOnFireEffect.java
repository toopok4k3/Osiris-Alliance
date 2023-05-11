package data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.Math;
import java.awt.Color;
import java.util.EnumSet;

public class OasExperimentalEnergyOnFireEffect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin {
	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		OasExperimentalEnergyOnFireEffect fx = new OasExperimentalEnergyOnFireEffect(projectile);
		CombatEntityAPI entity = engine.addLayeredRenderingPlugin(fx);
		entity.getLocation().set(projectile.getLocation());
		if(weapon != null) {
			ShipAPI ship = weapon.getShip();
			if(ship != null) {
				engine.addNebulaSmokeParticle(projectile.getLocation(), ship.getVelocity(), 80.0f, 0.1f, 1.0f, 1.0f, 0.2f, fringeColor);
			}
		}
	}

	final private DamagingProjectileAPI projectile; // stored for the rendering stuff
	final private SpriteAPI baseTexture;
	final private SpriteAPI jitterTexture;
	final static private Color coreColor = new Color(0.8f, 0.9f, 1.0f, 0.8f);
	final static private Color fringeColor = new Color(0.5f, 0.6f, 1.0f, 0.8f);
	private float xoffset, yoffset, particleTimer;
	final private FaderUtil fader;

	public OasExperimentalEnergyOnFireEffect() {
		this.projectile = null;
		this.baseTexture = null;
		this.jitterTexture = null;
		this.fader = null;
	}
	
	public OasExperimentalEnergyOnFireEffect(DamagingProjectileAPI projectile) {
		this.projectile = projectile;
		this.baseTexture = Global.getSettings().getSprite("misc", "oas_noise_ball1");
		this.jitterTexture = Global.getSettings().getSprite("misc", "oas_noise_ball2");
		this.particleTimer = 0.3f + (Misc.random.nextFloat() * 0.5f);
		this.fader = new FaderUtil(0f, 0.05f, 0.05f);
		this.fader.fadeIn();
	}

	private static void randomizeSpriteTexture(SpriteAPI sprite) {
		final float x = Misc.random.nextInt(8);
		final float y = Misc.random.nextInt(8);
		sprite.setTexWidth(0.125f);
		sprite.setTexHeight(0.125f);
		sprite.setTexX(x * 0.125f);
		sprite.setTexY(y * 0.125f);
	}

	@Override
	public void advance(float amount) {
		if(Global.getCombatEngine().isPaused()) { return; }
		fader.advance(amount);
		entity.getLocation().set(projectile.getLocation());
		randomizeSpriteTexture(baseTexture);
		randomizeSpriteTexture(jitterTexture);
		xoffset = (float)Math.sin(Misc.random.nextDouble()*2.0*Math.PI) * 6.0f;
		yoffset = (float)Math.cos(Misc.random.nextDouble()*2.0*Math.PI) * 6.0f;

		particleTimer -= amount;
		if(particleTimer < 0.0f) {
			particleTimer = 0.3f + (Misc.random.nextFloat() * 0.5f);
			// let's just reuse this frames offsets
			float empx = xoffset * 2.8f;
			float empy = yoffset * 2.8f;
			Vector2f loc = projectile.getLocation();
			Vector2f futureLoc = new Vector2f(loc.x + /*(projectile.getVelocity().x * 0.05f) +*/ empx, loc.y +/*  (projectile.getVelocity().y * 0.05f)+*/ empy);
			float thickness = 1.0f + (Misc.random.nextFloat() * 3.0f);
			EmpArcEntityAPI arc = Global.getCombatEngine().spawnEmpArcVisual(projectile.getLocation(), projectile, futureLoc, projectile, thickness, fringeColor, coreColor);
			Global.getSoundPlayer().playSound("oas_crackle", 1.0f, 0.4f, projectile.getLocation(), projectile.getVelocity());
			arc.setSingleFlickerMode();
		}
	}

	@Override
	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		final float x = projectile.getLocation().x;
		final float y = projectile.getLocation().y;
		float brightness = projectile.getBrightness();
		brightness *= viewport.getAlphaMult();

		baseTexture.setAngle(projectile.getFacing());
		baseTexture.setSize(32.0f, 32.0f);
		baseTexture.setAlphaMult(1.0f * fader.getBrightness() * brightness);
		baseTexture.setColor(coreColor);
		baseTexture.setAdditiveBlend();
		baseTexture.renderAtCenter(x, y);
		baseTexture.setAlphaMult(0.8f* fader.getBrightness() * brightness);
		baseTexture.renderAtCenter(x+xoffset, y+yoffset);
		
		jitterTexture.setAngle(projectile.getFacing());
		jitterTexture.setSize(32.0f, 32.0f);
		jitterTexture.setAlphaMult(1.0f* fader.getBrightness() * brightness);
		jitterTexture.setColor(fringeColor);
		jitterTexture.setAdditiveBlend();
		jitterTexture.renderAtCenter(x, y);
		jitterTexture.setSize(40.0f, 40.0f);
		jitterTexture.setAlphaMult(0.8f* fader.getBrightness() * brightness);
		jitterTexture.renderAtCenter(x+xoffset, y+yoffset);
	}

	@Override
	public float getRenderRadius() {
		return 100f;
	}

	@Override
	public boolean isExpired() {
		return this.projectile.isExpired() || !Global.getCombatEngine().isEntityInPlay(this.projectile);
	}

	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);

	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return layers;
	}
}
