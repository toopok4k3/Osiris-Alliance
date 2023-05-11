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

public class OasMissileSnapperAi implements GuidedMissileAI, MissileAIPlugin {

	private final MissileAPI missile;
	private final ShipAPI launchingShip;
	private CombatEntityAPI target;
	private CombatEntityAPI guidedOverrideTarget;
	private PID pid;
	private float sinceLastSearch = 0.0f;
	private float randomOffset;
	//private float lastDebug = 1.0f;

	private static final float HALF_DISTANCE = 750.0f;
	private static final float HALF_DISTANCE_SQ = HALF_DISTANCE * HALF_DISTANCE;
	private static final float RETARGET_ARC = 30.0f;

	public OasMissileSnapperAi(MissileAPI missile, ShipAPI launchingShip) {
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
		target = getTarget();
		if(guidedOverrideTarget != null) {
			target = guidedOverrideTarget;
			guidedOverrideTarget = null;
		}
		missile.giveCommand(ShipCommand.ACCELERATE);
		if (target == null) {
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
		if(missile.getFlightTime() < missile.getArmingTime()) {
			return;
		}
		TargetAnalysis targetAnalysis = new TargetAnalysis();
		if(targetAnalysis.isHopelessToHit()) {
			//missile.giveCommand(ShipCommand.ACCELERATE);
			return;
		} else {
			final float angleToTarget = targetAnalysis.getAngleToTarget();
			float angularVelocity = missile.getAngularVelocity();
			float pidResult = pid.advance(amount, angleToTarget, missile.getAngularVelocity());
			float correction = angularVelocity + pidResult;
			//lastDebug += amount;
			//if(lastDebug > 0.5f) {
				//String text = String.format("D:%1$.2f RoV:%2$.2f C:%3$.2f", angleToTarget, angularVelocity, correction);
				//String text = "D:"+angleToTarget+" RoV:"+angularVelocity+" C:"+correction;
				//lastDebug = 0.0f;
				//Global.getCombatEngine().addFloatingText(missile.getLocation(), text, 10.0f, Color.WHITE, missile, 5.0f, 5.0f);
			//}
			//void addFloatingText(Vector2f loc, String text, float size, Color color, CombatEntityAPI attachedTo, float flashFrequency, float flashDuration);
			if(correction < 0.0f) {
				missile.giveCommand(ShipCommand.TURN_LEFT);
			} else if(correction > 0.0f) {
				missile.giveCommand(ShipCommand.TURN_RIGHT);
				
			}
		}

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

		/*public TargetAnalysis() {
			final Vector2f targetLocation = target.getLocation();
			//final Vector2f targetVelocity = target.getVelocity();
			final Vector2f us = missile.getLocation();
			final float angle = Misc.normalizeAngle(Misc.getAngleInDegrees(us, targetLocation));
			final float usAngle = missile.getFacing();
			angleToTarget = usAngle - angle;
			if(angleToTarget > 180.0f) {
				angleToTarget = angleToTarget - 360.0f;
			} else if(angleToTarget < -180.0f) {
				angleToTarget = angleToTarget + 360.0f; 
			}
			angleToTarget = usAngle - angle;
		}*/

		public TargetAnalysis() {
			final Vector2f targetLocation = target.getLocation();
			final Vector2f targetVelocity = target.getVelocity();
			final Vector2f us = missile.getLocation();
			final float distanceSq = Misc.getDistanceSq(us, targetLocation);
			final float distanceSqToSplit = Math.min(distanceSq - HALF_DISTANCE_SQ, HALF_DISTANCE_SQ);
			//final float courseRandomLevel = distanceSqToSplit / HALF_DISTANCE_SQ;
			final float courseRandomLevel = Math.max(distanceSqToSplit / HALF_DISTANCE_SQ, 0.0f);
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
			final float time = Math.min((float) Math.sqrt((double)timeSq), 0.5f);
			final float maxRadius = Math.min(target.getCollisionRadius(), 150.0f);
			final float leadx = Math.min(targetVelocity.x * time, maxRadius + (maxRadius * courseRandomLevel * 0.5f)); // we could do 0...1 scale based upon ECCM
			final float leady = Math.min(targetVelocity.y * time, maxRadius + (maxRadius * courseRandomLevel * 0.5f));
			final float randomOffsetX = randomOffsetDir.x * courseRandomLevel * randomTarget * maxRadius * 1.0f;
			final float randomOffsetY = randomOffsetDir.y * courseRandomLevel * randomTarget * maxRadius * 1.0f;
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

		// return 
		public float getAngleToTarget() {
			return angleToTarget;
		}
	}

	/*private static float componentAlongB(Vector2f a, Vector2f b) {
		final float dot = (a.x * b.x) + (a.y * b.y);
		final float div = (float)Math.sqrt((double) ((b.x * b.x) + (b.y * b.y)));
		final float retval;
		if(div > 0.0f || div < 0.0f) {
			retval = dot/div;
		} else {
			retval = 0.0f;
		}
		return retval;
	}*/

	private static ShipAPI findEnemyShipInArc(final MissileAPI missile, final float arc) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		final List<ShipAPI> ships = engine.getShips();
		//float minDist = Float.MAX_VALUE;
		ShipAPI closestShip = null;
		//float dpLeft = 60.0f;
		//int fighterPicked = 0;
		float closestDist = missile.getMaxRange();
		for (final ShipAPI other : ships) {
			//if (dpLeft <=0.0f && fighterPicked >= 8) break;
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
