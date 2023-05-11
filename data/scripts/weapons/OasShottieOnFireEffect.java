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

//import data.scripts.OasUtil.Trail;
//import data.scripts.OasUtil.TrailConfig;

public class OasShottieOnFireEffect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin {
	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI combatEngine) {
		final float range = 0.3f * StarSystemGenerator.random.nextFloat();
		projectile.getVelocity().scale(0.85f + range);
		if (weapon != null) {
			// Everything inside this block is about adjusting the spread. ONLY WORKS FOR A
			// FEW HARDCODED WEAPONS.
			final float supposedAngle = solveSupposedAngle(projectile.getLocation(), weapon);
			final float calculatedShotCenterAngle = projectile.getFacing() - supposedAngle;
			final float angleAdjust = calculateSpreadAngleAdjust(weapon);
			final float newFacing = getNewFacing(calculatedShotCenterAngle, projectile.getFacing(), angleAdjust * 2.0f);
			final float angleDiff = newFacing - projectile.getFacing();
			adjustVelocity(projectile.getVelocity(), angleDiff);
			projectile.setFacing(newFacing);
		}

		// the fx trail
		if(!"oas_l_pd".equals(weapon.getId())) { // don't do trails on PD
			OasShottieOnFireEffect trail = new OasShottieOnFireEffect(projectile);
			CombatEntityAPI entity = combatEngine.addLayeredRenderingPlugin(trail);
			entity.getLocation().set(projectile.getLocation());

			/*SpriteAPI sprite = Global.getSettings().getSprite("graphics/oas/fx/oas_benergy_core.png");
			TrailConfig configBright = new TrailConfig(sprite,
				startColor,
				endColor);
			configBright.setSegmentSpawnFrequency(5.0f);
			configBright.setDuration(0.10f);
			configBright.setWidth(40.0f);
			configBright.setAcceleration(-10000.0f);
			configBright.useInitialVelocityDamp();
			configBright.setFrequncies(1.5f, 0.75f);
			configBright.setAmplitudes(1.5f, 5.0f);
			Trail trailBright = new Trail(projectile, configBright);
			TrailConfig configDark = new TrailConfig(sprite,
				smokeStartColor,
				smokeEndColor);
			configDark.setAmplitudes(2.0f, 15.0f);
			configDark.setDuration(0.25f);
			configDark.setAcceleration(-20000.0f);
			configDark.setWidth(60.0f);
			configDark.setTexSpeed(1.0f, 2.0f);
			configDark.setSegmentSpawnFrequency(2.5f);
			configBright.setFrequncies(0.8f, 0.4f);
			//configDark.setFrequncies(5.0f, 8.0f);
			Trail trailDark = new Trail(projectile, configDark);*/
		}
	}

	// hackety hack hack. as if I will ever remember to change these as I modify the
	// .wpn,
	// BTW! "separateRecoilForLinkedBarrels":"true" exists that would do what I do
	// here. Wish I'd known that before coding this hacked garbage.
	private static final float sMediumAngles[] = new float[] { -3.5f, 3.5f, -2.5f, 2.5f, -1.5f, 1.5f, -0.5f, 0.5f }; // oas_m_shottie
	private static final float sLargeAngles[] = new float[] { -3.5f, 3.5f, -2.5f, 2.5f, -1.5f, 1.5f, -0.5f, 0.5f }; // oas_l_shottie
	private static final float sLargePdAngles[] = new float[] { -3.5f,3.5f,-3.0f,3.0f,-2.5f,2.5f,-2.0f, 2.0f,-1.5f, 1.5f,-1.0f,1.0f,-0.5f,0.5f,0f}; // oas_l_pd
	private static final float sDefaultAngles[] = new float[] { 0.0f };

	// This is so stupid. I don't even know if this works, apparently it does..?
	// We find the barrel index that did the shot by comparing barrel location to
	// projectile location
	// Needs every barrel offset to be unique location to find the actual index.
	// After finding barrel index, we use the lookup tables to check the angle on
	// that barrel
	private static float solveSupposedAngle(final Vector2f location, final WeaponAPI weapon) {
		final float supposedAngle;
		final WeaponSpecAPI spec = weapon.getSpec();
		final float[] array;
		if (spec != null) {
			if ("oas_m_shottie".equals(spec.getWeaponId())) {
				array = sMediumAngles;
			} else if ("oas_l_shottie".equals(spec.getWeaponId())) {
				array = sLargeAngles;
			} else if ("oas_l_pd".equals(spec.getWeaponId())) {
				array = sLargePdAngles;
			} else {
				array = sDefaultAngles;
			}
			float angle = 0.0f;
			for (int i = 0; i < array.length; i++) { // index lookup
				final Vector2f firingLocation = weapon.getFirePoint(i);
				if (firingLocation.getX() == location.getX()
						&& firingLocation.getY() == location.getY()) { // seriously stupidest way to do this.
					angle = array[i];
					break;
				}
			}
			supposedAngle = angle;
		} else {
			supposedAngle = 0.0f;
		}
		return supposedAngle;
	}

	private static void adjustVelocity(final Vector2f velocity, final float angleDiff) {
		final double angle = Math.toRadians(angleDiff);
		final float oldX = velocity.getX();
		final float oldY = velocity.getY();
		final double cos = Math.cos(angle);
		final double sin = Math.sin(angle);
		final double newX = (oldX * cos) - (oldY * sin);
		final double newY = (oldX * sin) + (oldY * cos);
		velocity.set((float) newX, (float) newY);
	}

	private static float getNewFacing(final float weaponFacing, final float old, final float adjust) {
		//final float diff = weaponFacing - old;
		final float diff = old - weaponFacing;
		final float scaledDiff = diff * (1.0f + adjust);
		final float newFacing = weaponFacing + scaledDiff;
		return newFacing;
	}

	// We get a number between 0 and 1, depending what's the current spread on
	// weapon.
	private static float calculateSpreadAngleAdjust(WeaponAPI weapon) {
		final float retVal;
		final WeaponSpecAPI spec = weapon.getSpec();
		if (spec != null) {
			final float maxSpread = Math.abs(spec.getMaxSpread());
			final float currentSpread = Math.abs(weapon.getCurrSpread());
			final float min = Math.min(maxSpread, currentSpread); // to save us from the trouble if currentSpread is
																	// actually > max, not sure if this can happen?
			final float ratio;
			if (maxSpread > 0.0f) {
				ratio = min / maxSpread;
			} else {
				ratio = -1.0f;
			}
			if (ratio > 0.0f && ratio <= 1.0f) {
				retVal = ratio;
			} else {
				retVal = 0.0f;
			}
		} else {
			retVal = 0.0f;
		}
		return retVal;
	}

	// Stuff for the fx render begins here (based on
	// BaseCombatLayeredRenderingPlugin)!
	final static private int SMOKE_PARTICLE_AMOUNT = 12;
	final static private int PARTICLE_AMOUNT = 6;
	final private DamagingProjectileAPI projectile; // stored for the rendering stuff
	final private List<ParticleData> particles = new ArrayList<ParticleData>(PARTICLE_AMOUNT);
	final private List<ParticleData> smokeParticles = new ArrayList<ParticleData>(SMOKE_PARTICLE_AMOUNT);
	final private Vector2f particleDirection;
	final private float startSize;
	final private float sizeDelta;
	final private float waveLengthInUnits;
	final private float smokeOffset;

	private float timeExisted = 0.0f;

	final static private Color startColor = new Color(1.0f, 0.7f, 0.3f, 1.0f);
	final static private Color endColor = new Color(0.8f, 0.1f, 0.0f, 0.0f);
	final static private Color smokeStartColor = new Color(0.3f, 0.1f, 0.1f, 1.0f);
	final static private Color smokeEndColor = new Color(0.2f, 0.2f, 0.2f, 0.0f);
	final static private float waveLengthInSeconds = 0.75f;
	final static private float endSize = 0.0f;

	// just the empty constructor that is used elsewhere
	public OasShottieOnFireEffect() {
		this.projectile = null;
		this.particleDirection = null;
		this.startSize = 0.0f;
		this.smokeOffset = this.startSize;
		this.sizeDelta = startSize - endSize;
		this.waveLengthInUnits = 1.0f;
	}

	public OasShottieOnFireEffect(DamagingProjectileAPI projectile) {
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
