package data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
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

public class OasHyperOnFireEffect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin {
	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI combatEngine) {
        OasHyperOnFireEffect trail = new OasHyperOnFireEffect(projectile);
        CombatEntityAPI entity = combatEngine.addLayeredRenderingPlugin(trail);
        entity.getLocation().set(projectile.getLocation());
	}

	// Stuff for the fx render begins here (based on
	// BaseCombatLayeredRenderingPlugin)!
	final static private int SMOKE_PARTICLE_AMOUNT = 16;
	final static private int PARTICLE_AMOUNT = 10;
	final private DamagingProjectileAPI projectile; // stored for the rendering stuff
	final private List<ParticleData> particles = new ArrayList<ParticleData>(PARTICLE_AMOUNT);
	final private List<ParticleData> smokeParticles = new ArrayList<ParticleData>(SMOKE_PARTICLE_AMOUNT);
	final private Vector2f particleDirection;
	final private float startSize;
	final private float sizeDelta;
	final private float waveLengthInUnits;
	final private float smokeOffset;

	private float timeExisted = 0.0f;

	final static private Color startColor = new Color(0.4f, 1.0f, 0.4f, 1.0f);
	final static private Color endColor = new Color(0.1f, 0.7f, 0.1f, 0.0f);
	final static private Color smokeStartColor = new Color(0.1f, 0.3f, 0.1f, 1.0f);
	final static private Color smokeEndColor = new Color(0.0f, 0.2f, 0.8f, 0.0f);
	final static private float waveLengthInSeconds = 0.75f;
	final static private float endSize = 0.0f;

	// just the empty constructor that is used elsewhere
	public OasHyperOnFireEffect() {
		this.projectile = null;
		this.particleDirection = null;
		this.startSize = 0.0f;
		this.smokeOffset = this.startSize;
		this.sizeDelta = startSize - endSize;
		this.waveLengthInUnits = 1.0f;
	}

	public OasHyperOnFireEffect(DamagingProjectileAPI projectile) {
		this.startSize = projectile.getProjectileSpec().getWidth()*1.5f;
		this.smokeOffset = projectile.getProjectileSpec().getWidth()/2;
		this.waveLengthInUnits = projectile.getProjectileSpec().getLength();
		this.sizeDelta = startSize - endSize;
		this.projectile = projectile;
		this.particleDirection = Misc.getUnitVectorAtDegreeAngle(projectile.getFacing() + 180f);
		for (int i = 0; i < SMOKE_PARTICLE_AMOUNT; i++) {
			smokeParticles.add(new ParticleData(this.projectile, false));
		}
		for (int i = 0; i < PARTICLE_AMOUNT; i++) {
			particles.add(new ParticleData(this.projectile, true));
		}
	}

	public void advance(float amount) {
		if (Global.getCombatEngine().isPaused())
			return;
		timeExisted += amount;
		entity.getLocation().set(projectile.getLocation());

		int i = 0;
		for (ParticleData p : particles) {
			p.fader.advance(amount);
			final float indexOffset = (waveLengthInSeconds / particles.size()) * (float) i;
			final float timePointInPeriod = (timeExisted + indexOffset) % waveLengthInSeconds;
			final float pointInPeriod = timePointInPeriod / waveLengthInSeconds; // calculate 0..1 value in period
			final float positionInPeriod = pointInPeriod * waveLengthInUnits;
			p.size = ((1.0f - pointInPeriod) * sizeDelta) + endSize; // not sure if it matters if we let this down to 0
			float x = particleDirection.x * positionInPeriod;
			float y = particleDirection.y * positionInPeriod;
			p.offset.setX(x);
			p.offset.setY(y);
			p.color = fadeColor(pointInPeriod, startColor, endColor);
			if(timePointInPeriod < 0.1) {
				p.color = new Color(p.color.getColorSpace(), p.color.getColorComponents(null), timePointInPeriod * 10.0f);
			}
			i++;
		}

		i = 0;
		for (ParticleData p : smokeParticles) {
			final float indexOffset = (waveLengthInSeconds / smokeParticles.size()) * (float) i;
			final float timePointInPeriod = (timeExisted + indexOffset) % waveLengthInSeconds;
			final float pointInPeriod = timePointInPeriod / waveLengthInSeconds; // calculate 0..1 value in period
			final float positionInPeriod = pointInPeriod * waveLengthInUnits;
			final float smokePositionInPeriod = pointInPeriod * smokeOffset;
			p.size = ((/*1.0f - */pointInPeriod) * sizeDelta) + endSize; // not sure if it matters if we let this down to 0
			float x = particleDirection.x * positionInPeriod;
			float y = particleDirection.y * positionInPeriod;
			// now do the slight left/right offsets from 90 degree angled dir
			if(i % 2 == 0) {
				Vector2f right = new Vector2f(particleDirection.y, -particleDirection.x);
				x += right.x * smokePositionInPeriod;
				y += right.y * smokePositionInPeriod;
			} else {
				Vector2f left = new Vector2f(-particleDirection.y, particleDirection.x);
				x += left.x * smokePositionInPeriod;
				y += left.y * smokePositionInPeriod;
			}
			p.offset.setX(x);
			p.offset.setY(y);
			p.color = fadeColor(pointInPeriod, smokeStartColor, smokeEndColor);
			i++;
		}
	}

	public float getRenderRadius() {
		return 200f;
	}

	public boolean isExpired() {
		// if(this.projectile == null) return true;
		return this.projectile.isExpired() || !Global.getCombatEngine().isEntityInPlay(this.projectile);
	}

	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		float x = projectile.getLocation().x;
		float y = projectile.getLocation().y;

		Color color = projectile.getProjectileSpec().getFringeColor();
		 color = Misc.setAlpha(color, 30);
		float brightness = projectile.getBrightness();
		brightness *= viewport.getAlphaMult();

		for (ParticleData particle : smokeParticles) {
			float px = x + particle.offset.x;
			float py = y + particle.offset.y;
			particle.sprite.setAngle(particle.angle);
			particle.sprite.setSize(particle.size, particle.size);
			// particle.sprite.setAlphaMult(brightness * particle.fader.getBrightness());
			particle.sprite.setAlphaMult(brightness * 1.0f * particle.fader.getBrightness());
			particle.sprite.setColor(particle.color);
			particle.sprite.renderAtCenter(px, py);
		}

		for (ParticleData particle : particles) {
			float px = x + particle.offset.x;
			float py = y + particle.offset.y;
			particle.sprite.setAngle(particle.angle);
			particle.sprite.setSize(particle.size, particle.size);
			// particle.sprite.setAlphaMult(brightness * particle.fader.getBrightness());
			particle.sprite.setAlphaMult(brightness * 1.0f * particle.fader.getBrightness());
			particle.sprite.setColor(particle.color);
			particle.sprite.renderAtCenter(px, py);
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

	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);

	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return layers;
	}

	// Only copying the pattern Alex uses with CryoBlaster here.
	private static class ParticleData {
		final private SpriteAPI sprite;
		final private FaderUtil fader;
		final private DamagingProjectileAPI projectile;
		final public Vector2f offset = new Vector2f();
		private float angle = (float) Math.random() * 360f;
		private float size = 1.0f;
		private Color color = new Color(0.0f, 0.0f, 0.0f, 0.0f);

		private ParticleData(DamagingProjectileAPI projectile, boolean additive) {
			this.projectile = projectile;
			this.sprite = Global.getSettings().getSprite("misc", "nebula_particles");
			this.fader = new FaderUtil(0f, 0.05f, 0.05f);
			final float i = Misc.random.nextInt(4);
			final float j = Misc.random.nextInt(4);
			this.sprite.setTexWidth(0.25f);
			this.sprite.setTexHeight(0.25f);
			this.sprite.setTexX(i * 0.25f);
			this.sprite.setTexY(j * 0.25f);
			if(additive) {
				this.sprite.setAdditiveBlend();
			} else {
				this.sprite.setNormalBlend();
			}
			this.fader.fadeIn();
		}
	}
}
