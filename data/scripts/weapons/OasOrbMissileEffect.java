package data.scripts.weapons;

import data.scripts.weapons.ai.OasMissileOrbAi;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CollisionGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.Math;
import java.awt.Color;
import java.util.EnumSet;
import java.util.Iterator;

public class OasOrbMissileEffect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin {

	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		OasOrbMissileEffect fx = new OasOrbMissileEffect(projectile);
		CombatEntityAPI entity = engine.addLayeredRenderingPlugin(fx);
		entity.getLocation().set(projectile.getLocation());
	}

	final static public Color coreColor = new Color(0.8f, 0.9f, 1.0f, 0.8f);
	final static public Color fringeColor = new Color(0.5f, 0.6f, 1.0f, 0.8f);
	private DamagingProjectileAPI projectile;
	private float particleTimer;
	final private SpriteAPI glowSprite;
	private float glowLevel = 0.0f;
	private Vector2f glowLocation = new Vector2f(0.0f, 0.0f);

	public OasOrbMissileEffect() {
		this.projectile = null;
		this.glowSprite = null;
	}

	public OasOrbMissileEffect(DamagingProjectileAPI projectile) {
		this.projectile = projectile;
		this.particleTimer = 0.3f + (Misc.random.nextFloat() * 0.5f);
		this.glowSprite = Global.getSettings().getSprite("graphics/oas/fx/oas_glow_blob.png");
	}

	@Override
	public void advance(float amount) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		if(engine.isPaused()) { return; }
		
		
		if(projectile.isExpired() || projectile.isFading() || projectile.getDamageAmount() <= 0.0f) {
			glowLevel = 0.0f;
			return;
		}
		
		entity.getLocation().set(projectile.getLocation());
		final float xoffset = (float)Math.sin(Misc.random.nextDouble()*2.0*Math.PI) * 15.0f;
		final float yoffset = (float)Math.cos(Misc.random.nextDouble()*2.0*Math.PI) * 15.0f;
		particleTimer -= amount;
		if(particleTimer < 0.0f) {
			particleTimer = 0.2f + (Misc.random.nextFloat() * 0.2f);
			// let's just reuse this frames offsets
			float empx = xoffset * 2.8f;
			float empy = yoffset * 2.8f;
			Vector2f orbCenterOffset = Misc.rotateAroundOrigin(new Vector2f(8.0f,0.0f), projectile.getFacing());
			Vector2f loc = new Vector2f(projectile.getLocation().x + orbCenterOffset.x, projectile.getLocation().y + orbCenterOffset.y);
			CombatEntityAPI target = findClosestEnemyObjectInRange(loc, projectile, projectile.getWeapon(), projectile.getSource());
			float thickness = 1.0f + (Misc.random.nextFloat() * 3.0f);
			if(target != null) {
				float emp = projectile.getEmpAmount() * 0.1f;
				float dam = projectile.getDamageAmount() * 0.1f;
				EmpArcEntityAPI arc = engine.spawnEmpArc(projectile.getSource(), loc, projectile,
						target,
						DamageType.ENERGY, 
						dam,
						emp, // emp 
						100000f, // max range 
						"oas_crackle",
						thickness, // thickness
						OasOrbMissileEffect.fringeColor,
						OasOrbMissileEffect.coreColor
						);
				arc.setSingleFlickerMode();
			} else {
				if(Misc.random.nextFloat() < 0.40f) { // let's decrease the idle zaps by 60%
					Vector2f futureLoc = new Vector2f(loc.x + empx, loc.y + empy);
					EmpArcEntityAPI arc = Global.getCombatEngine().spawnEmpArcVisual(loc, projectile, futureLoc, projectile, thickness, fringeColor, coreColor);
					Global.getSoundPlayer().playSound("oas_crackle", 1.0f, 0.5f, loc, projectile.getVelocity());
					arc.setSingleFlickerMode();
				}
			}
		}
		
		
		if(projectile instanceof MissileAPI) {
			MissileAPI missile = (MissileAPI) projectile;
			if(glowSprite != null && missile.getUnwrappedMissileAI() instanceof OasMissileOrbAi) {
				OasMissileOrbAi ai = (OasMissileOrbAi)missile.getUnwrappedMissileAI();
				if(ai.isTriggered() && !ai.isSplit()) {
					float level = ai.getTriggerLevel();
					if(level > 0.0f) {
						Vector2f orbCenterOffset = Misc.rotateAroundOrigin(new Vector2f(8.0f,0.0f), projectile.getFacing());
						glowLocation.set(projectile.getLocation().x + orbCenterOffset.x, projectile.getLocation().y + orbCenterOffset.y);
						//glowLevel = 1.0f;
						glowLevel = level;
					}
				} else {
					glowLevel = 0.0f;
				}
			}
		}
	}

	private static CombatEntityAPI findClosestEnemyObjectInRange(final Vector2f from
			, final CombatEntityAPI source
			, final WeaponAPI sourceWeapon
			, final ShipAPI sourceShip) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		final CollisionGridAPI grid = engine.getAllObjectGrid();
		final int owner = source.getOwner();
		final float range = 250.0f;
		final float size = range * 2.0f;
		CombatEntityAPI closestEntity = null;
		float closest = Float.MAX_VALUE;
		Iterator<Object> iterator = grid.getCheckIterator(from, size, size);
		boolean ignoreFlares = sourceShip != null && sourceShip.getMutableStats().getDynamic().getValue(Stats.PD_IGNORES_FLARES, 0) >= 1;
		ignoreFlares |= sourceWeapon.hasAIHint(AIHints.IGNORES_FLARES);
		while(iterator.hasNext()) {
			Object o = iterator.next();
			if (!(o instanceof MissileAPI) &&
					//!(o instanceof CombatAsteroidAPI) &&
					!(o instanceof ShipAPI)) continue;
			final CombatEntityAPI other = (CombatEntityAPI) o;
			if (other.getOwner() == owner) continue;
			
			if (other instanceof ShipAPI) {
				final ShipAPI otherShip = (ShipAPI) other;
				if (otherShip.isHulk()) continue;
				//if (!otherShip.isAlive()) continue;
				if (otherShip.isPhased()) continue;
			}
			
			if (other.getCollisionClass() == CollisionClass.NONE) continue;
			
			if (ignoreFlares && other instanceof MissileAPI) {
				final MissileAPI missile = (MissileAPI) other;
				if (missile.isFlare()) continue;
			}

			final float radius = Misc.getTargetingRadius(from, other, false);
			final float distance = Misc.getDistance(from, other.getLocation()) - radius;
			if (distance > range) continue;
			
			if(distance < closest) {
				closest = distance;
				closestEntity = other;
			}
		}
		return closestEntity;
	}

	@Override
	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		if(glowSprite != null && glowLocation != null && glowLevel > 0.0f) {
			glowSprite.setAdditiveBlend();
			glowSprite.setColor(OasOrbMissileEffect.coreColor);
			glowSprite.setSize(glowLevel*128.0f, glowLevel*128.0f);
			//glowSprite.setSize(4.0f*128.0f, 4.0f*128.0f);
			glowSprite.setAlphaMult(glowLevel);
			glowSprite.renderAtCenter(glowLocation.x, glowLocation.y);
		}
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
