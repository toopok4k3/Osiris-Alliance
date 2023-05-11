package data.scripts;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.combat.DisintegratorEffect;
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

public class OasUtil {
    
	public static float componentAlongB(Vector2f a, Vector2f b) {
		final float dot = (a.x * b.x) + (a.y * b.y);
		final float div = (float)Math.sqrt((double) ((b.x * b.x) + (b.y * b.y)));
		final float retval;
		if(div > 0.0f || div < 0.0f) {
			retval = dot/div;
		} else {
			retval = 0.0f;
		}
		return retval;
	}

	// stage is from 0 to 1
	public static Color fadeColor(final float stage, final Color source, final Color target) {
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

	

	// stage is from 0 to 1
	public static Color fadeColor(final float stage, final Color source, final Color target, float alphaMult) {
		float[] s = source.getComponents(null);
		float[] t = target.getComponents(null);
		float[] r = new float[4];
		Color retval = source;
		if (s.length == 4 && t.length == 4) {
			for (int i = 0; i < 4; i++) {
				r[i] = ((t[i] - s[i]) * stage) + s[i];
			}
			if(alphaMult >= 0.0f && alphaMult <=1.0f) {
				retval = new Color(r[0], r[1], r[2], r[3] * alphaMult);
			} else {
				retval = new Color(r[0], r[1], r[2], r[3]);
			}
		}
		return retval;
	}

	public static class TrailConfig {
		private final SpriteAPI sprite;

		private final Color startColor;
		private final Color endColor;
		private float segmentSpawnFrequency = 60.0f;
		private int maxSegments = 100;
		private float texTrailSpeed = 0.5f;
		private float texSpeed = 0.25f;
		private float fadeIn = 0.15f;
		private float fadeOut = 0.45f;

		private float segmentDuration = 0.45f;
		private float acceleration = -2000.0f;
		private float width = 60.0f;
		private float halfWidth = 30.0f;
		private float frequency = 10.0f;
		private float frequencyVariation = 15.0f;
		private float amplitudeRandom = 5.0f;
		private float amplitudeRise = 5.0f;
		private boolean useInitialVelocityDamp = false;

		public TrailConfig(SpriteAPI sprite, Color startColor, Color endColor) {
			this.sprite = sprite;
			this.startColor = startColor;
			this.endColor = endColor;
		}

		public void setSegmentSpawnFrequency(float frequency) {this.segmentSpawnFrequency = frequency;}
		public void setMaxSegments(int segments) {this.maxSegments = segments;}
		public void setTexSpeed(float speed, float trailSpeed) {this.texSpeed = speed; this.texTrailSpeed = trailSpeed;}
		public void setFading(float fadeIn, float fadeOut) {this.fadeIn = fadeIn; this.fadeOut = fadeOut;}
		public void setDuration(float duration) {this.segmentDuration = duration;}
		public void setAcceleration(float acceleration) {this.acceleration = acceleration;}
		public void setWidth(float width) {this.width = width; this.halfWidth = width / 2.0f;}
		public void setFrequncies(float frequency, float frequencyVariation) {this.frequency = frequency; this.frequencyVariation = frequencyVariation;}
		public void setAmplitudes(float range, float riseTo) {this.amplitudeRandom = range; this.amplitudeRise = riseTo;}
		public void useInitialVelocityDamp() {useInitialVelocityDamp = true;}
	}

	// Anyone reading this, USE MAGICTRAILS INSTEAD lol.
	public static class Trail extends BaseCombatLayeredRenderingPlugin {
		private final float timeBetweenSegments;
		private final List<Segment> segments = new ArrayList<Segment>();
		private final DamagingProjectileAPI projectile;

		private float textureScrollPoint = 1.0f;
		private float sinceLastSegment = 0.0f;
		private float fadeIn = 0.0f;
		private float fadeOut = 0.0f;
		private float alpha = 0.0f;
		private float age = 0.0f;
		private TrailConfig config;
		
		public Trail(final DamagingProjectileAPI projectile, final TrailConfig config) {
			this.config = config;
			this.projectile = projectile;
			this.timeBetweenSegments = 1.0f / config.segmentSpawnFrequency;
			this.fadeIn = config.fadeIn;
			this.fadeOut = config.fadeOut;
			CombatEntityAPI ent = Global.getCombatEngine().addLayeredRenderingPlugin(this);
			ent.getLocation().set(projectile.getLocation());
		}

		@Override
		public void advance(float amount) {
			if (Global.getCombatEngine().isPaused()) return;
			entity.getLocation().set(projectile.getLocation());
			age += amount;

			if((projectile.didDamage() || projectile.isExpired())) {
				fadeOut -= amount;
				alpha = Math.min(1.0f, Math.max((fadeOut / config.fadeOut),0.0f));
			} else if (fadeIn > 0.0f) {
				fadeIn -= amount;
				alpha = Math.max(0.0f, (1.0f - (fadeIn / config.fadeIn)));
			} else {
				alpha = 1.0f;
			}
			textureScrollPoint -= (amount * config.texTrailSpeed);
			if(textureScrollPoint < 0.0f) {
				textureScrollPoint = 1.0f + textureScrollPoint;
			}
			sinceLastSegment =- amount;
			if(!projectile.didDamage() && sinceLastSegment < 0.0f && !projectile.isExpired() && segments.size() < config.maxSegments) {
				// "spawn new"
				sinceLastSegment = timeBetweenSegments + sinceLastSegment;
				segments.add(0, new Segment(Trail.this, projectile.getVelocity(), projectile.getLocation(), textureScrollPoint));
			}

			Segment prev = null;
			Segment next = null;
			
			final List<Segment> expiredSegments = new ArrayList<Segment>();
			for(Segment current : segments) {
				next = next(segments.indexOf(current));
				if(current.isExpired(amount)) {
					expiredSegments.add(current);
				} else {
					current.advance(amount, prev, next);
				}
				if(current != null) {
					prev = current;
				}
			}
			segments.removeAll(expiredSegments);
		}

		@Override
		public float getRenderRadius() {
			return 400f; // we could do this smarter.
		}

		private Segment next(int i) {
			final Segment next;
			final int nexti = i + 1;
			final int maxi = segments.size() - 1;
			if(nexti <= maxi) {
				next = (Segment)segments.get(nexti);
			} else {
				next = null;
			}
			return next;
		}

		@Override
		public void render(CombatEngineLayers layer, ViewportAPI viewport) {
			if(segments.size() < 2) {
				return;
			}
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			config.sprite.bindTexture();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glBegin(GL11.GL_QUAD_STRIP);
	
			//int excludeLastIndex = segments.size();
			for(int i = 0; i < /*excludeLastIndex*/ segments.size(); i++) {
				Segment segment = (Segment)segments.get(i);
				Misc.setColor(segment.fadedColor);
				GL11.glTexCoord2f(segment.textureScrollPoint, 0.0f);
				GL11.glVertex2f(segment.left.x, segment.left.y);
				GL11.glTexCoord2f(segment.textureScrollPoint, 1.0f);
				GL11.glVertex2f(segment.right.x, segment.right.y);
			}
			GL11.glEnd();
			GL11.glPopMatrix();
		}

		public boolean isExpired() {
			return projectile.isExpired() && segments.size() < 2;
		}

		protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);

		@Override
		public EnumSet<CombatEngineLayers> getActiveLayers() {
			return layers;
		}
	}
	
	private static class Segment {

		private final Trail trail;
		private final Vector2f velocity;
		private final Vector2f location;
		private final Vector2f left;
		private final Vector2f right;

		private float textureScrollPoint = 0.0f;
		private float existed = 0.0f;
		private final float randomMult;
		private float alpha = 1.0f;
		private float level = 0.0f;
		private float fadeInFix = 0.0f;

		private Color fadedColor = null;

		private TrailConfig config;

		public Segment(final Trail trail, final Vector2f velocity, final Vector2f location, float textureScrollPoint) {
			this.trail = trail;
			this.config = trail.config;
			float initialVelocityDamp = 1.0f;
			if(trail.age < config.segmentDuration && config.useInitialVelocityDamp) {
				initialVelocityDamp = trail.age / config.segmentDuration;
			}
			this.velocity = new Vector2f(velocity.x * initialVelocityDamp, velocity.y * initialVelocityDamp);
			this.location = new Vector2f(location.x, location.y);
			this.left = new Vector2f(location.x, location.y);
			this.right = new Vector2f(location.x, location.y);
			this.textureScrollPoint = textureScrollPoint;
			this.randomMult = Misc.random.nextFloat();
			this.fadedColor = config.startColor;
		}

		public void advance(final float amount, final Segment prev, final Segment next) {
			existed += amount;
			level = existed / config.segmentDuration;
			alpha = 1.0f - level;
			
			fadedColor = fadeColor(level, config.startColor, config.endColor, trail.alpha);
			textureScrollPoint -= (amount * config.texSpeed);
			if(textureScrollPoint < 0.0f) {
				textureScrollPoint = 1.0f + textureScrollPoint;
			}

			// decelerate
			//velocity.set(0.0f, 0.0f);
			final float velocityDelta = amount * config.acceleration;
			float vel = Math.max((velocity.length() + velocityDelta), 0.0f);
			if(vel <= 0.0f) {
				velocity.set(0.0f, 0.0f);
			} else {
				Vector2f newVelocity = velocity.normalise(new Vector2f());
				newVelocity.scale(vel);
				velocity.set(newVelocity.x, newVelocity.y);
			}

			// calculate new position 
			location.set((velocity.x * amount) + location.x, (velocity.y * amount) + location.y);

			// calculate the left and right positions for the quads
			final Vector2f direction;
			if(next != null) {
				if(prev != null) {
					direction = Misc.getUnitVector(prev.location, next.location);
				} else {
					direction = Misc.getUnitVector(location, next.location);
				}
			} else {
				if(prev != null) {
					direction = Misc.getUnitVector(prev.location, location);
				} else {
					direction = Misc.getUnitVector(location, trail.projectile.getLocation());
				}
			}
			final Vector2f leftDirection = new Vector2f(-direction.y, direction.x);
			final Vector2f rightDirection = new Vector2f(direction.y, -direction.x);
			final float positionOffset = waveOffset(existed, ((1.0f - level) * config.amplitudeRandom) + (level*config.amplitudeRise));
			final float width = level * config.halfWidth;
			final float offsetX = leftDirection.x * positionOffset;
			final float offsetY = leftDirection.y * positionOffset;

			left.set((leftDirection.x*width) + location.x + offsetX, (leftDirection.y*width) + location.y + offsetY);
			right.set((rightDirection.x*width) + location.x + offsetX, (rightDirection.y*width) + location.y + offsetY);
		}

		static final float PI = 3.1415926f;
		private float waveOffset(float level, float amplitude) {
			float phase = 2*PI * randomMult; // in radians
			//float frequency = config.frequency + ((config.frequencyVariation * level )*randomMult);
			float frequency = config.frequency + ((config.frequencyVariation)*randomMult);
			float angularFrequency = 2*PI*frequency;
			float time = level * config.segmentDuration;
			float y = amplitude * (float)Math.sin((double)(angularFrequency*time + phase));
			return y;
		}

		public boolean isExpired(final float testWithAmount) {
			return config.segmentDuration < (existed + testWithAmount);
		}
	}

	// Mainly copy pasta from BreachOnHitEffect.
	public static void dealArmorDamage(final float dmg, DamagingProjectileAPI projectile, ShipAPI target, Vector2f point) {
		CombatEngineAPI engine = Global.getCombatEngine();

		ArmorGridAPI grid = target.getArmorGrid();
		int[] cell = grid.getCellAtLocation(point);
		if (cell == null) return;
		
		int gridWidth = grid.getGrid().length;
		int gridHeight = grid.getGrid()[0].length;
		
		float damageTypeMult = DisintegratorEffect.getDamageTypeMult(projectile.getSource(), target);
		
		float damageDealt = 0f;
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if ((i == 2 || i == -2) && (j == 2 || j == -2)) continue; // skip corners
				
				int cx = cell[0] + i;
				int cy = cell[1] + j;
				
				if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;
				
				float damMult = 1/30f;
				if (i == 0 && j == 0) {
					damMult = 1/15f;
				} else if (i <= 1 && i >= -1 && j <= 1 && j >= -1) { // S hits
					damMult = 1/15f;
				} else { // T hits
					damMult = 1/30f;
				}
				
				float armorInCell = grid.getArmorValue(cx, cy);
				float damage = dmg * damMult * damageTypeMult;
				damage = Math.min(damage, armorInCell);
				if (damage <= 0) continue;
				
				target.getArmorGrid().setArmorValue(cx, cy, Math.max(0, armorInCell - damage));
				damageDealt += damage;
			}
		}
		
		if (damageDealt > 0) {
			if (Misc.shouldShowDamageFloaty(projectile.getSource(), target)) {
				engine.addFloatingDamageText(point, damageDealt, Misc.FLOATY_ARMOR_DAMAGE_COLOR, target, projectile.getSource());
			}
			target.syncWithArmorGridState();
		}
	}

	// works only with != 1 and > 0 values.
	// returns exponentially curved value between 0 and 1 from level depending on the fraction
	// fraction of 1.0 will be linear.
	// fraction below 1 is opposite curve
	// give high numbers for more curve
	public static float exponentialLevel(float level, float fraction) {
		// y=(a^x - 1)/(a - 1), a > 1,
		float retval = level;
		if(fraction > 1.0f && fraction < 1.0f && fraction > 0.0f) {
			retval = ((float)Math.pow((double)fraction, (double)level) - 1.0f)/(fraction - 1.0f);
		}
		return retval;
	}
}
