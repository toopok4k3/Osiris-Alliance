package data.scripts.world.systems;

import java.awt.Color;

import data.scripts.Oas;
import data.scripts.Oas.AnyEntity;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;

public class Armin {

/*
star(String id, String starType, float radius, float coronaSize)
star(String id, String starType, float radius, float coronaSize, float solarWindBurnLevel, float flareProbability, float crLossMultiplier)
planet(float spaceBefore, float spaceAfter, float radius, float angle, String name, String id, String planetType, float orbitDays)
station(float spaceBefore, float spaceAfter, float angle, String name, String id, String entityType, String illustration, float orbitDays)
asteroids(float spaceBefore, float spaceAfter, float width, String name, float minOrbitDays, float maxOrbitDays)
jump(float spaceBefore, float spaceAfter, float orbitDays, float angle, String name, String id)
relay(float spaceBefore, float spaceAfter, float orbitDays, float angle, String name, String id, String entitytype)
systemColor(Color color)
background(String bgFile)
nebula(StarAge age)
ring(float spaceBefore, float spaceAfter, float orbitDays, String entitytype, int bandIndex, boolean terrain)
rings(float spaceBefore, float spaceAfter, float width, String name)
random(float spaceBefore, float spaceAfter, StarAge age, int min, int max, boolean allowHabitable, boolean withSpecialNames)
*/
	public void generate(SectorAPI sector) {

		AnyEntity[] entities = {
			Oas.star("armin", "star_orange", 775f, 500f, 10f, 1f, 3f),
			Oas.background("graphics/backgrounds/background2.jpg"),
			Oas.systemColor(new Color(255, 245, 200)),
            Oas.relay(1700f, 500f, 0f, 150f, "Armin Relay", "armin_relay", "comm_relay_makeshift").faction("osiris"),
            Oas.random(200f, 700f, StarAge.OLD, 3, 5, false, true),
            Oas.ring(100f, 100f, 170f, "rings_dust0", 0, false),
			Oas.ring(100f, 100f, 160f, "rings_dust0", 2, false),
			Oas.ring(100f, 150f, 180f, "rings_dust0", 1, false),
            Oas.rings(0f, 0f, 300f, "Harman's Belt"),
            Oas.jump(100f, 300f, 0f, 200f, "Harman Jump-point", "armin_jump2"),
            Oas.relay(1000f, 500f, 0f, 230f, "Harman Sensor Array", "harman_sensor_array", "sensor_array_makeshift").faction("osiris"),
            Oas.planet(1000f, 1200f, 60f, 230f, "Harman", "harman", "barren", 0f).description("planet_harman"),
            Oas.station(180f, 0f, 30f, "Harman Fuel Depot", "harman_station", "station_lowtech1", "hound_hangar", 0f).focusTo("harman").description("station_harman"),
            Oas.random(300f, 700f, StarAge.OLD, 1, 2, false, true),
            Oas.relay(1000f, 500f, 0f, 230f, "Harman Nav Buoy", "harman_nav_buoy", "nav_buoy_makeshift").faction("osiris"),
            Oas.planet(500f, 1200f, 95f, 115f, "Fugura", "fugura", "cryovolcanic", 0f).description("planet_fugura"),
            Oas.nebula(StarAge.OLD)
		};
		Oas.generateAnyEntity(sector, entities, "Armin");
    }
}