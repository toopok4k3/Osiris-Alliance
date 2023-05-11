package data.scripts;

import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.lang.Math;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

public class Oas {

	private static final int OAS_KEY_STAR = 0;
	private static final int OAS_KEY_PLANET = 1;
	private static final int OAS_KEY_ASTEROID = 2;
	private static final int OAS_KEY_JUMP = 3;
	private static final int OAS_KEY_RELAY = 4;
	private static final int OAS_KEY_SYSTEM_COLOR = 5;
	private static final int OAS_KEY_RING = 6;
	private static final int OAS_KEY_BG = 7;
	private static final int OAS_KEY_STAR_EXTENDED = 8;
	private static final int OAS_KEY_NEBULA = 9;
	private static final int OAS_KEY_RANDOM_REST = 10;
	private static final int OAS_KEY_STATION = 11;
	private static final int OAS_KEY_COMBINE_RINGS = 12;
	private static final int OAS_KEY_SECONDARY_STAR = 13;
	private static final int OAS_KEY_SECONDARY_STAR_EXT = 14;

	public static class AnyEntity {
		public int type;
		public float spaceBefore;
		public float spaceAfter;
		public float radius;
		public float coronaSize;
		public String entityType;
		public String name;
		public String id;
		public String description;
		public Color color;
		public float angle;
		public float orbitDays;
		public float minOrbitDays;
		public float maxOrbitDays;
		public String toId;
		public String factionId;
		public int bandIndex;
		public boolean terrain;
		public float solarWindBurnLevel;
		public float flareProbability;
		public float crLossMultiplier;
		public int min;
		public int max;
		public boolean allowHabitable;
		public boolean withSpecialNames;
		public StarAge starAge;
		public String illustration;
		public String customDescrioptionId;

		public AnyEntity focusTo(String id) {
			this.toId = id;
			return this;
		}

		public AnyEntity faction(String factionId) {
			this.factionId = factionId;
			return this;
		}

		public AnyEntity description(String customDescrioptionId) {
			this.customDescrioptionId = customDescrioptionId;
			return this;
		}
	}
	public static AnyEntity star(String id, String starType, float radius, float coronaSize) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_STAR;
		entity.id = id;
		entity.entityType = starType;
		entity.radius = radius;
		entity.coronaSize = coronaSize;
		return entity;
	}

	public static AnyEntity star(String id, String starType, float radius, float coronaSize, float solarWindBurnLevel, float flareProbability, float crLossMultiplier) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_STAR_EXTENDED;
		entity.id = id;
		entity.entityType = starType;
		entity.radius = radius;
		entity.coronaSize = coronaSize;
		entity.solarWindBurnLevel = solarWindBurnLevel;
		entity.flareProbability = flareProbability;
		entity.crLossMultiplier = crLossMultiplier;
		return entity;
	}
	
	public static AnyEntity secondaryStar(float spaceBefore, float spaceAfter, float angle, String id, String starType, float radius, float coronaSize) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_SECONDARY_STAR;
		entity.id = id;
		entity.entityType = starType;
		entity.radius = radius;
		entity.coronaSize = coronaSize;
		entity.angle = angle;
		entity.spaceBefore = spaceBefore;
		entity.spaceAfter = spaceAfter;
		return entity;
	}

	public static AnyEntity secondaryStar(float spaceBefore, float spaceAfter, float angle, String id, String starType, float radius, float coronaSize, float solarWindBurnLevel, float flareProbability, float crLossMultiplier) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_SECONDARY_STAR_EXT;
		entity.id = id;
		entity.entityType = starType;
		entity.radius = radius;
		entity.coronaSize = coronaSize;
		entity.solarWindBurnLevel = solarWindBurnLevel;
		entity.flareProbability = flareProbability;
		entity.crLossMultiplier = crLossMultiplier;
		entity.angle = angle;
		entity.spaceBefore = spaceBefore;
		entity.spaceAfter = spaceAfter;
		return entity;
	}

	public static AnyEntity planet(float spaceBefore, float spaceAfter, float radius, float angle, String name, String id, String planetType, float orbitDays) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_PLANET;
		entity.spaceBefore = spaceBefore;
		entity.spaceAfter = spaceAfter;
		entity.angle = angle;
		entity.name = name;
		entity.id = id;
		entity.entityType = planetType;
		entity.radius = radius;
		entity.orbitDays = orbitDays;
		return entity;
	}

	public static AnyEntity station(float spaceBefore, float spaceAfter, float angle, String name, String id, String entityType, String illustration, float orbitDays) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_STATION;
		entity.spaceBefore = spaceBefore;
		entity.spaceAfter = spaceAfter;
		entity.angle = angle;
		entity.name = name;
		entity.id = id;
		entity.entityType = entityType;
		entity.orbitDays = orbitDays;
		entity.illustration = illustration;
		return entity;
	}

	public static AnyEntity asteroids(float spaceBefore, float spaceAfter, float width, String name, float minOrbitDays, float maxOrbitDays) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_ASTEROID;
		entity.spaceBefore = spaceBefore;
		entity.spaceAfter = spaceAfter;
		entity.radius = width;
		entity.minOrbitDays = minOrbitDays;
		entity.maxOrbitDays = maxOrbitDays;
		entity.name = name;
		return entity;
	}

	public static AnyEntity jump(float spaceBefore, float spaceAfter, float orbitDays, float angle, String name, String id) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_JUMP;
		entity.spaceBefore = spaceBefore;
		entity.spaceAfter = spaceAfter;
		entity.angle = angle;
		entity.name = name;
		entity.id = id;
		entity.orbitDays = orbitDays;
		return entity;
	}

	public static AnyEntity relay(float spaceBefore, float spaceAfter, float orbitDays, float angle, String name, String id, String entitytype) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_RELAY;
		entity.spaceBefore = spaceBefore;
		entity.spaceAfter = spaceAfter;
		entity.angle = angle;
		entity.name = name;
		entity.id = id;
		entity.entityType = entitytype;
		entity.orbitDays = orbitDays;
		return entity;
	}

	public static AnyEntity systemColor(Color color) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_SYSTEM_COLOR;
		entity.color = color;
		return entity;
	}

	public static AnyEntity background(String bgFile) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_BG;
		entity.id = bgFile;
		return entity;
	}

	public static AnyEntity nebula(StarAge age) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_NEBULA;
		entity.starAge = age;
		return entity;
	}

	public static AnyEntity ring(float spaceBefore, float spaceAfter, float orbitDays, String entitytype, int bandIndex, boolean terrain) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_RING;
		entity.spaceBefore = spaceBefore;
		entity.spaceAfter = spaceAfter;
		entity.entityType = entitytype;
		entity.orbitDays = orbitDays;
		entity.bandIndex = bandIndex;
		entity.terrain = terrain;
		return entity;
	}

	public static AnyEntity rings(float spaceBefore, float spaceAfter, float width, String name) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_COMBINE_RINGS;
		entity.spaceBefore = spaceBefore;
		entity.spaceAfter = spaceAfter;
		entity.radius = width;
		entity.name = name;
		return entity;
	}

	public static AnyEntity random(float spaceBefore, float spaceAfter, StarAge age, int min, int max, boolean allowHabitable, boolean withSpecialNames) {
		AnyEntity entity = new AnyEntity();
		entity.type = OAS_KEY_RANDOM_REST;
		entity.spaceBefore = spaceBefore;
		entity.spaceAfter = spaceAfter;
		entity.starAge = age;
		entity.min = min;
		entity.max = max;
		entity.allowHabitable = allowHabitable;
		entity.withSpecialNames = withSpecialNames;
		return entity;
	}

	public static void generateAnyEntity(SectorAPI sector, AnyEntity[] anyentities, String starSystemName) {
		StarSystemAPI system = sector.createStarSystem(starSystemName);
		system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");
		AnyEntity previous = null;
		AnyEntity current = null;
		PlanetAPI star = null;
		float calculatedDistance = 0.0f;
		float calculatedDistanceFromStar = 0.0f;
		int planetOffset = 0;
		for(int i = 0 ; i < anyentities.length; i++) {
			SectorEntityToken createdEntity = null;
			current = anyentities[i];
			if(i > 0) {
				previous = anyentities[i-1];
			}
			SectorEntityToken focus = star;
			if(previous == null) {
				calculatedDistanceFromStar = calculatedDistanceFromStar + current.spaceBefore;
				calculatedDistance = calculatedDistanceFromStar;
				calculatedDistanceFromStar = calculatedDistanceFromStar + current.spaceAfter;
			} else {
				if(current.toId != null && previous.toId != null) { // we aint the first
					focus = system.getEntityById(current.toId);
					calculatedDistance = calculatedDistance + previous.spaceAfter + current.spaceBefore;
				} else if(current.toId != null && previous.toId == null) { // we are first
					focus = system.getEntityById(current.toId);
					calculatedDistance = 0.0f + current.spaceBefore;
				} else {
					calculatedDistanceFromStar = calculatedDistanceFromStar + current.spaceBefore;
					calculatedDistance = calculatedDistanceFromStar;
					calculatedDistanceFromStar = calculatedDistanceFromStar + current.spaceAfter;
				}
			}
			float calculatedOrbitDays = 0f;
			if(current.orbitDays > 0f) {
				calculatedOrbitDays = current.orbitDays;
			} else {
				calculatedOrbitDays = 6.28f * (0.0075f * calculatedDistance);
			}
			if(current.type == OAS_KEY_STAR) {
				star = system.initStar(current.id, current.entityType, current.radius, current.coronaSize);
				createdEntity = star;
			} else if(current.type == OAS_KEY_STAR_EXTENDED) {
				star = system.initStar(current.id, current.entityType, current.radius, current.coronaSize, current.solarWindBurnLevel, current.flareProbability, current.crLossMultiplier);
				createdEntity = star;
			} else if(current.type == OAS_KEY_SECONDARY_STAR) {
				createdEntity = system.addPlanet(current.id, focus, current.name, current.entityType, current.angle, current.radius, calculatedDistance, calculatedOrbitDays);
				system.addCorona(createdEntity, 250, 2f, 0.1f, 2f);
				planetOffset++;
			} else if(current.type == OAS_KEY_SECONDARY_STAR_EXT) {
				createdEntity = system.addPlanet(current.id, focus, current.name, current.entityType, current.angle, current.radius, calculatedDistance, calculatedOrbitDays);
				planetOffset++;
			} else if(current.type == OAS_KEY_PLANET) {
				createdEntity = system.addPlanet(current.id, focus, current.name, current.entityType, current.angle, current.radius, calculatedDistance, calculatedOrbitDays);
				planetOffset++;
			} else if(current.type == OAS_KEY_ASTEROID) {
				createdEntity = system.addAsteroidBelt(focus, 100, calculatedDistance, current.radius, current.minOrbitDays, current.maxOrbitDays, Terrain.ASTEROID_BELT, current.name);
			} else if(current.type == OAS_KEY_JUMP) {
				JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(current.id, current.name);
				OrbitAPI orbit = Global.getFactory().createCircularOrbit(focus, current.angle, calculatedDistance, calculatedOrbitDays);
				jumpPoint.setOrbit(orbit);
				if(current.toId != null) {
					jumpPoint.setRelatedPlanet(focus);
				}
				jumpPoint.setStandardWormholeToHyperspaceVisual();
				system.addEntity(jumpPoint);
				createdEntity = jumpPoint;
			} else if(current.type == OAS_KEY_RELAY) {
				SectorEntityToken entity = system.addCustomEntity(current.id, current.name, current.entityType, current.factionId);
				entity.setCircularOrbit(star, current.angle, calculatedDistance, calculatedOrbitDays);
				createdEntity = entity;
			} else if(current.type == OAS_KEY_RING) {
				if(current.terrain) {
					system.addRingBand(focus, "misc", current.entityType, 256f, current.bandIndex, Color.white, 256f, calculatedDistance, current.orbitDays, Terrain.RING, null);
				} else {
					system.addRingBand(focus, "misc", current.entityType, 256f, current.bandIndex, Color.white, 256f, calculatedDistance, current.orbitDays);
				}
			} else if(current.type == OAS_KEY_COMBINE_RINGS) {
				SectorEntityToken ring = system.addTerrain(Terrain.RING, new RingParams(current.radius+256, calculatedDistance - (current.radius/2), null, current.name));
				ring.setCircularOrbit(focus, 0, 0, 100);
				createdEntity = ring;
			} else if(current.type == OAS_KEY_SYSTEM_COLOR) {
				system.setLightColor(current.color);
			} else if(current.type == OAS_KEY_BG) {
				system.setBackgroundTextureFilename(current.id);
			} else if(current.type == OAS_KEY_NEBULA) {
				StarSystemGenerator.addSystemwideNebula(system, current.starAge);
			} else if(current.type == OAS_KEY_RANDOM_REST) {
				calculatedDistanceFromStar = StarSystemGenerator.addOrbitingEntities(system, star, current.starAge, current.min, current.max, calculatedDistance, planetOffset, current.withSpecialNames, current.allowHabitable);
				calculatedDistanceFromStar = calculatedDistanceFromStar + current.spaceAfter;
			} else if(current.type == OAS_KEY_STATION) {
				SectorEntityToken station = system.addCustomEntity(current.id, current.name, current.entityType, current.factionId);
				station.setInteractionImage("illustrations", current.illustration);
				station.setCircularOrbitPointingDown(focus, current.angle, calculatedDistance, calculatedOrbitDays);
				createdEntity = station;
			}
			if(createdEntity != null && current.customDescrioptionId != null) {
				createdEntity.setCustomDescriptionId(current.customDescrioptionId);
			}
			
		}
		system.autogenerateHyperspaceJumpPoints(true, true, true);
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);        
        float minRadius = plugin.getTileSize() * 2f;
        
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
	}

}