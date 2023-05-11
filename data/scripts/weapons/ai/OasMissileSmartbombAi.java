package data.scripts.weapons.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import data.scripts.OasUtil;

import java.awt.Color;
import java.lang.Math;
import java.util.List;
import java.util.Random;

public class OasMissileSmartbombAi implements GuidedMissileAI, MissileAIPlugin {

	private final MissileAPI missile;
	private final ShipAPI launchingShip;
	private CombatEntityAPI target;
	private CombatEntityAPI guidedOverrideTarget;
	private PID pid;
	private float sinceLastSearch = 0.0f;
	private float sinceLastDistance = 0.0f;
	private int inRangeCount = 0;
	private float randomOffset;

	private boolean triggered = false;
	private float sinceTrigger = 0.0f;

	private static final float RETARGET_ARC = 90.0f;
	private static final float SPLIT_DISTANCE = 800.0f;
	private static final float SPLIT_DISTANCE_SQ = SPLIT_DISTANCE * SPLIT_DISTANCE;
	//private static final float MIRV_ANGLE_BETWEEN_SUBMUNITIONS = 1.8f;
	private static final float MIRV_ANGLE_BETWEEN_SUBMUNITIONS = 1.65f;
	private static final Color SMOKE_COLOR = new Color(0.3f,0.3f,0.1f,0.9f);
	private static final float TRIGGER_TIME = 0.5f;

	public OasMissileSmartbombAi(MissileAPI missile, ShipAPI launchingShip) {
		this.missile = missile;
		this.launchingShip = launchingShip;
		// solve original target.
		target = launchingShip.getShipTarget();
		pid = new PID();
		randomOffset = Misc.random.nextFloat();
	}

	@Override
	public void advance(float amount) {
		if (missile == null || Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling()) {return;}
		sinceLastSearch += amount;
		sinceLastDistance += amount;
		if(triggered) {
			sinceTrigger += amount;
		}
		target = getTarget();
		if(guidedOverrideTarget != null) {
			target = guidedOverrideTarget;
			guidedOverrideTarget = null;
		}
		missile.giveCommand(ShipCommand.ACCELERATE);
		if (target == null || target.getOwner() == 100) {
			// should solve target...
			if(sinceLastSearch > 0.25f) {
				target = findEnemyShipInArc(missile, RETARGET_ARC);
				pid = new PID();
				sinceLastSearch = 0.0f;
			}
			return;
		}

		// ensure we act like vanilla in the cases of phase and collisionclass NONE,
		// that is accelerate.
		if (CollisionClass.NONE == target.getCollisionClass()) {
			//missile.giveCommand(ShipCommand.ACCELERATE);
			return;
		}
		if (target instanceof ShipAPI) {
			final ShipAPI targetShip;
			targetShip = (ShipAPI) target;
			if (targetShip.isPhased()) {
				//missile.giveCommand(ShipCommand.ACCELERATE);
				return;
			}
		}

		final Vector2f targetLocation = target.getLocation();
		if (targetLocation == null) {return;}
		
		// we have a location to aim to.
		// is it to our left, or right?
		TargetAnalysis targetAnalysis = new TargetAnalysis();
		if (targetAnalysis.isHopelessToHit()) {
			// missile.giveCommand(ShipCommand.ACCELERATE);
			return;
		} else {
			final float angleToTarget = targetAnalysis.getAngleToTarget();
			float angularVelocity = missile.getAngularVelocity();
			float pidResult = pid.advance(amount, angleToTarget, missile.getAngularVelocity());
			float correction = angularVelocity + pidResult;
			if (correction < 0.0f) {
				missile.giveCommand(ShipCommand.TURN_LEFT);
			} else if (correction > 0.0f) {

				missile.giveCommand(ShipCommand.TURN_RIGHT);
			}
		}
		if (sinceLastDistance > 0.1f && !triggered) {
			sinceLastDistance = 0.0f;
			// should we explode, or not
			//final float distanceSq = Misc.getDistanceSq(missile.getLocation(), targetLocation);
			final float distance = Misc.getDistance(missile.getLocation(), targetLocation) + (target.getCollisionRadius()/2);
			target.getCollisionRadius();
			if (distance < SPLIT_DISTANCE) {
				final float arcBonus = (1.0f - (distance / SPLIT_DISTANCE)) * 50.0f;
				final float lastAngleBeforeExplosion = Misc.getAngleInDegrees(missile.getVelocity());
				final boolean isInArc = Misc.isInArc(lastAngleBeforeExplosion, 2.5f + arcBonus,
				missile.getLocation(), targetLocation);
				if (isInArc || inRangeCount > 15) {
					triggered = true;
					Global.getSoundPlayer().playSound("oas_bomb_chargeup", 1.0f, 0.55f, missile.getLocation(), missile.getVelocity());
				}
				inRangeCount++;
			}
		}
		if(triggered) {
			float intensity = Math.min(1.0f, sinceTrigger / TRIGGER_TIME);
			float range = (1 - Math.min(1.0f, sinceTrigger / TRIGGER_TIME)) * 6.0f;
			missile.setJitter(missile, missile.getSpec().getExplosionColor(), intensity, 5, range);
			//void setJitter(Object source, Color color, float intensity, int copies, float range);
			//void setJitter(Object source, Color color, float intensity, int copies, float minRange, float range);
		}
		if(triggered && sinceTrigger > TRIGGER_TIME) {
			final float lastAngleBeforeExplosion = Misc.getAngleInDegrees(missile.getVelocity());
			split(lastAngleBeforeExplosion);
			
			//Global.getSoundPlayer().playSound(null, amount, amount, null, null);
		}
		/*if (!triggered) {
		} else if(triggered && sinceTrigger > 0.3f) {
			split(lastAngleBeforeExplosion);
			//Global.getSoundPlayer().playSound(null, amount, amount, null, null);
		} else {
			float intensity = Math.min(1.0f, sinceTrigger / 0.3f);
			float range = Math.min(1.0f, sinceTrigger / 0.3f) * 10.0f + 3.0f;
			missile.setJitter(missile, missile.getSpec().getExplosionColor(), intensity, 5, 2.0f, range);
			//void setJitter(Object source, Color color, float intensity, int copies, float range);
			//void setJitter(Object source, Color color, float intensity, int copies, float minRange, float range);
		}*/
	}
	
	private void split(float lastAngleBeforeExplosion) {
		CombatEngineAPI engine = Global.getCombatEngine();
		final float totalAngle = (float)missile.getMirvNumWarheads() * MIRV_ANGLE_BETWEEN_SUBMUNITIONS;
		final float angleStartOffset = Misc.normalizeAngle(totalAngle / 2.0f * -1.0f);
		final float bombHitpoints = (float) missile.getBehaviorSpecParams().optDouble("hitpoints", 0);
		final float width = missile.getCollisionRadius() * 1.15f; // maybe this is okay to get some separation so the warheads are not too tight
		final Vector2f offsetVector = Misc.getUnitVectorAtDegreeAngle(Misc.normalizeAngle(lastAngleBeforeExplosion+90.0f));

		for(int i = 0; i < missile.getMirvNumWarheads(); i++) {
			float angle = lastAngleBeforeExplosion + angleStartOffset + (MIRV_ANGLE_BETWEEN_SUBMUNITIONS * (float)i);
			angle = Misc.normalizeAngle(angle);
			final float singleWarheadOffset = width / (float)missile.getMirvNumWarheads(); // we are inside loop, we have > 0
			final float halfWayOffset = width / 2;
			final float xOffset = offsetVector.x * (float)i * singleWarheadOffset - offsetVector.x * halfWayOffset;
			final float yOffset = offsetVector.y * (float)i * singleWarheadOffset - offsetVector.y * halfWayOffset;
			
			final Vector2f spawnLocation = new Vector2f(missile.getLocation().x + xOffset, missile.getLocation().y + yOffset);
			final CombatEntityAPI entity = engine.spawnProjectile(missile.getSource(), missile.getWeapon(), missile.getWeapon().getId(), "oas_m_smartbomb_shot3", spawnLocation, angle, missile.getVelocity());
			if(entity != null && entity instanceof MissileAPI) {
				MissileAPI bomb = (MissileAPI) entity;
				bomb.setDamageAmount(missile.getMirvWarheadDamage());
				bomb.setHitpoints(bombHitpoints);
				bomb.setMass(missile.getMass()); // steal it from the parent
				float scale = Misc.random.nextFloat();
				Vector2f smokeVel = new Vector2f(entity.getVelocity().x * scale, entity.getVelocity().y * scale);
				engine.addNebulaSmokeParticle(entity.getLocation(), smokeVel, 5.0f + 10.0f * Misc.random.nextFloat(), 3.0f, 0.1f, 0.1f, 0.7f + Misc.random.nextFloat(), SMOKE_COLOR);
			}
		}
		engine.addNebulaSmokeParticle(missile.getLocation(), missile.getVelocity(), 15.0f, 3.0f, 0.1f, 0.1f, 1.0f, SMOKE_COLOR);
		engine.spawnExplosion(missile.getLocation(), missile.getVelocity(), missile.getSpec().getExplosionColor(), missile.getSpec().getExplosionRadius()/2.0f, 0.1f);
		//void spawnExplosion(Vector2f loc, Vector2f vel, Color color, float size, float maxDuration);
		Global.getSoundPlayer().playSound("oas_bomb_woosh", 0.66f, 0.35f, missile.getLocation(), missile.getVelocity());
		engine.removeEntity(missile);
	}

	@Override
	public CombatEntityAPI getTarget() {
		return target;
	}

	@Override
	public void setTarget(CombatEntityAPI target) {
		this.guidedOverrideTarget = target;
	}

	private class TargetAnalysis {
		final private float angleToTarget;

		public TargetAnalysis() {
			final Vector2f targetLocation = target.getLocation();
			final Vector2f targetVelocity = target.getVelocity();
			final Vector2f us = missile.getLocation();
			final float distanceSq = Misc.getDistanceSq(us, targetLocation);
			final float distanceSqToSplit = Math.min(distanceSq - SPLIT_DISTANCE_SQ, SPLIT_DISTANCE_SQ);
			final float courseRandomLevel = Math.max(distanceSqToSplit / SPLIT_DISTANCE_SQ, 0.0f);
			final float randomTarget = (randomOffset - 0.5f) * 2.0f;
			final Vector2f randomOffsetDir = Misc.getUnitVectorAtDegreeAngle(Misc.normalizeAngle(Misc.getAngleInDegrees(us, targetLocation) - 90.0f));
			final float speed = missile.getMaxSpeed();
			final float speedSq = speed * speed;
			final float timeSq;
			if(speedSq > 0.0f) {
				timeSq = distanceSq / speedSq;
			} else {
				timeSq = 0.0f;
			}
			final float time = Math.min((float) Math.sqrt((double)timeSq), 1.5f);
			final float maxRadius = Math.min(target.getCollisionRadius(), 150.0f);
			final float leadx = Math.min(targetVelocity.x * time, maxRadius + (maxRadius * courseRandomLevel * 0.5f)); // we could do 0...1 scale based upon ECCM
			final float leady = Math.min(targetVelocity.y * time, maxRadius + (maxRadius * courseRandomLevel * 0.5f));
			final float randomOffsetX = randomOffsetDir.x * courseRandomLevel * randomTarget * maxRadius * 4.0f;
			final float randomOffsetY = randomOffsetDir.y * courseRandomLevel * randomTarget * maxRadius * 4.0f;
			//final Vector2f leadLocation = new Vector2f(leadx + targetLocation.x + randomOffsetX, leady + targetLocation.y + randomOffsetY);
			//final Vector2f leadLocationPoint = new Vector2f(leadx + targetLocation.x + randomOffsetX, leady + targetLocation.y + randomOffsetY);
			//final Vector2f leadLocationVectorNoDepth = vectorFromLineToPoint(us, targetLocation, leadLocationPoint);
			//final Vector2f leadLocation = new Vector2f(targetLocation.x + leadLocationVectorNoDepth.x, targetLocation.y + leadLocationVectorNoDepth.y);
			//Global.getCombatEngine().addHitParticle(leadLocation, new Vector2f(0.0f, 0.0f), 30.0f, 1.0f, Color.WHITE);

			//final Vector2f leadLocation = new Vector2f(leadx + targetLocation.x + randomOffsetX, leady + targetLocation.y + randomOffsetY);
			final Vector2f lead = new Vector2f(leadx + randomOffsetX, leady + randomOffsetY);
			Vector2f dir = Misc.rotateAroundOrigin(Misc.getUnitVector(us, targetLocation), -90.0f);
			final float component = OasUtil.componentAlongB(lead, dir);
			dir.scale(component);
			final Vector2f leadLocation = new Vector2f(dir.x + targetLocation.x, dir.y + targetLocation.y);
			//Global.getCombatEngine().addHitParticle(leadLocation, new Vector2f(0.0f, 0.0f), 15.0f, 1.0f, Color.WHITE);

			final float angle = Misc.normalizeAngle(Misc.getAngleInDegrees(us, leadLocation));
			final float usAngle = missile.getFacing();
			angleToTarget = usAngle - angle;
			if(angleToTarget > 180.0f) {
				angleToTarget = angleToTarget - 360.0f;
			} else if(angleToTarget < -180.0f) {
				angleToTarget = angleToTarget + 360.0f; 
			}
		}

		public boolean isHopelessToHit() {
			return false;
		}

		public float getAngleToTarget() {
			return angleToTarget;
		}
	}

	private static ShipAPI findEnemyShipInArc(final MissileAPI missile, final float arc) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		final List<ShipAPI> ships = engine.getShips();
		ShipAPI closestShip = null;
		float closestDist = missile.getMaxRange();
		for (final ShipAPI other : ships) {
			if (other.getHullSize().ordinal() <= HullSize.FIGHTER.ordinal()) continue;
			if (other.isShuttlePod()) continue;
			if (other.isHulk()) continue;
			if (missile.getOwner() != other.getOwner() && other.getOwner() != 100) {
				final float dist = Misc.getDistance(missile.getLocation(), other.getLocation()) + other.getCollisionRadius();
				if (dist < missile.getMaxRange() && dist < closestDist) { // isInArc(float direction, float arc, Vector2f from, Vector2f to)
					if(Misc.isInArc(missile.getFacing(), arc, missile.getLocation(), other.getLocation())) {
						closestShip = other;
						closestDist = dist;
					}
				}
			}
		}

		return closestShip;
	}

	private class PID {
/* Proportional
 * P = Proportional | kP = Proportional Gain | SP = Set point | PV = Process Value | Err = Error
 * Err = SP – PV
 * P = kP x Err
 */

/* Integral 
 * I = Integral | kI = Integral Gain | dt = cycle time of the controller | It = Integral Total
 * I = kI x Err x dt
 * It = It + I
 */

/* Derivative
 * D = Derivative | kD = Derivative Gain | dt = cycle time of the controller | pErr = Previous Error
 * D = kD x (Err – pErr) / dt
 */ 

/*
Err = Sp – PV
P = kP x Err
It = It + (Err x kI x dt)
D = kD x (pErr – Err) / dt
pErr = Err
Output = P + It + D */
		private static final float pgain = 0.08f;
		private static final float igain = 0.0035f;
		private static final float dgain = 0.0025f;

		private float previousError = 0.0f;
		private float integralTotal = 0.0f;

		public float advance(float amount, float targetAngle, float rotationVelocity) {
			final float error = (targetAngle/amount) - rotationVelocity;
			final float proportional = pgain * error;
			final float integral = igain * error * amount;
			integralTotal = integralTotal + integral;
			final float derivative = dgain * (previousError - error) / amount;
			previousError = error;
			final float output = proportional + integralTotal + derivative;
			return output;
		}
	}
}
