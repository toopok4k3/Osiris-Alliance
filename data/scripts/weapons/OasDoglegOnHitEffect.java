package data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;

import java.lang.Math;
import java.awt.Color;
import java.util.EnumSet;

import data.scripts.OasUtil;

public class OasDoglegOnHitEffect extends BaseCombatLayeredRenderingPlugin implements OnHitEffectPlugin {

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
			ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		Global.getSoundPlayer().playSound("oas_nuke5", 1.0f, 1.0f, point, new Vector2f(0.0f, 0.0f));
		OasDoglegOnHitEffect fx = new OasDoglegOnHitEffect(point, target, 0.95f + (Misc.random.nextFloat() * 0.1f), ALIVE_TIME);
		fx.setFractions(10f, 10f, 10f);
		CombatEntityAPI entity = engine.addLayeredRenderingPlugin(fx);
		entity.getLocation().set(point.x, point.y);
	}

	private static final float ALIVE_TIME = 5.0f;
	private static final float GLOW_TIME_S = 0.0f;
	private static final float GLOW_TIME_E = 0.25f;
	private static final float GLOW_SIZE_S = 1000.0f;
	private static final float GLOW_SIZE_E = 0.0f;
	private static final float CORE_TIME_S = 0.0f;
	private static final float CORE_TIME_E = 0.6f;
	private static final float CORE_SIZE_S = 100.0f;
	private static final float CORE_SIZE_E = 800.0f;
	private static final float FRINGE_TIME_S = 0.0f;
	private static final float FRINGE_TIME_E = 1.0f;
	private static final float FRINGE_SIZE_S = 50f;
	private static final float FRINGE_SIZE_E = 900f;
	private float glowFraction = 1.0f;
	private float coreFraction = 1.0f;
	private float fringeFraction = 1.0f;
	private float timeLeft;
	private float level = 0.0f;
	private Vector2f location = new Vector2f();
	private float initialGlowAngle = 0.0f;
	final private SpriteAPI glowSprite;
	final private SpriteAPI coreSprite;
	final private SpriteAPI fringeSprite;
	final private Vector2f velocity;
	final private float scale;
	final private float aliveTime;
	
	public OasDoglegOnHitEffect() {
		this.timeLeft = 0.0f;
		this.scale = 1.0f;
		this.glowSprite = null;
		this.coreSprite = null;
		this.fringeSprite = null;
		this.velocity = null;
		this.aliveTime = ALIVE_TIME;
	}

	public OasDoglegOnHitEffect(final Vector2f point, final CombatEntityAPI target, final float scale, final float aliveTime) {
		this.scale = scale;
		this.aliveTime = aliveTime;
		if(target != null && target.getVelocity() != null) {
			Vector2f v = target.getVelocity();
			this.velocity = new Vector2f(v.x, v.y);
		} else {
			this.velocity = null;
		}
		this.timeLeft = aliveTime;
		location.set(point.x, point.y);
		this.glowSprite = Global.getSettings().getSprite("graphics/oas/fx/oas_pinched_light2.png");
		this.coreSprite = Global.getSettings().getSprite("graphics/oas/fx/oas_blast_wave.png");
		this.fringeSprite = Global.getSettings().getSprite("graphics/oas/fx/oas_brown_gas.png");
		glowSprite.setAdditiveBlend();
		initialGlowAngle = 360.0f * Misc.random.nextFloat();
		coreSprite.setAdditiveBlend();
		coreSprite.setAngle(360.0f * Misc.random.nextFloat());
		fringeSprite.setAdditiveBlend();
		fringeSprite.setAngle(360.0f * Misc.random.nextFloat());
		adjustSprites(0.0f);
	}

	public void setFractions(float glow, float core, float fringe) {
		this.glowFraction = glow;
		this.coreFraction = core;
		this.fringeFraction = fringe;
	}

	@Override
	public void advance(float amount) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		if(engine.isPaused()) { return; }
		if(coreSprite == null || glowSprite == null || fringeSprite == null) { return; }
		if(velocity != null) {
			final float velocityDelta = amount * -10000.0f;
			float vel = Math.max((velocity.length() + velocityDelta), 0.0f);
			if(vel <= 0.0f) {
				velocity.set(0.0f, 0.0f);
			} else {
				Vector2f newVelocity = velocity.normalise(new Vector2f());
				newVelocity.scale(vel);
				velocity.set(newVelocity.x, newVelocity.y);
			}
			location.set(location.x + velocity.x * amount, location.y + velocity.y * amount);
			entity.getLocation().set(location.x, location.y);
		}
		timeLeft -= amount;
		level = Math.max(Math.max(aliveTime - timeLeft, 0.0f) / aliveTime, 0.0f);
		if(level > 0.0f) {
			adjustSprites(level);
		}
	}

	private void adjustSprites(final float level) {
		glowSprite.setAlphaMult(levelValue(level, GLOW_TIME_S, GLOW_TIME_E, 1.0f, 0.0f, glowFraction));
		float size = levelValue(level, GLOW_TIME_S, GLOW_TIME_E, GLOW_SIZE_S, GLOW_SIZE_E, glowFraction) * scale;
		glowSprite.setAngle(Misc.normalizeAngle(initialGlowAngle + levelValue(level, 0.0f, 1.0f, 0.0f, 90f)));
		glowSprite.setSize(size, size);
	
		coreSprite.setAlphaMult(levelValue(level, CORE_TIME_S, CORE_TIME_E, 1.0f, 0.0f, coreFraction));
		size = levelValue(level, CORE_TIME_S, CORE_TIME_E, CORE_SIZE_S, CORE_SIZE_E, coreFraction) * scale;
		coreSprite.setSize(size, size);
	
		fringeSprite.setAlphaMult(levelValue(level, FRINGE_TIME_S, FRINGE_TIME_E, 1.0f, 0.0f, fringeFraction));
		size = levelValue(level, FRINGE_TIME_S, FRINGE_TIME_E, FRINGE_SIZE_S, FRINGE_SIZE_E, fringeFraction) * scale;
		fringeSprite.setSize(size, size);
	}

	private static float levelValue(float level, float start, float end, float valueS, float valueE) {
		return levelValue(level, start, end, valueS, valueE, 1.0f);
	}

	private static float levelValue(float level, float start, float end, float valueS, float valueE, float fraction) {
		final float retval;
		if(level < start) {
			retval = valueS;
		} else if(level > end) {
			retval = valueE;
		} else {
			final float pointInDelta = level - start;
			final float delta = end - start;
			// we control the values, no fear of division by 0
			final float l = Math.min(1.0f, pointInDelta / delta);
			final float valueDelta = valueE - valueS;
			retval = (OasUtil.exponentialLevel(l, fraction) * valueDelta) + valueS;
		}
		return retval;
	}

	@Override
	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		if(coreSprite != null && glowSprite != null && fringeSprite != null) {
			glowSprite.renderAtCenter(location.x, location.y);
			coreSprite.renderAtCenter(location.x, location.y);
			fringeSprite.renderAtCenter(location.x, location.y);
		}
	}

	@Override
	public float getRenderRadius() {
		return 1000f*scale;
	}

	@Override
	public boolean isExpired() {
		return timeLeft <= 0.0f;
	}

	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);

	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return layers;
	}
}
