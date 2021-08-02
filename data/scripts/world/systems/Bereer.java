package data.scripts.world.systems;

import java.awt.Color;

import data.scripts.Oas;
import data.scripts.Oas.AnyEntity;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;

public class Bereer {

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
random(float spaceBefore, float spaceAfter, StarAge age, int min, int max, boolean allowHabitable, boolean withSpecialNames)
*/
	public void generate(SectorAPI sector) {
		AnyEntity[] entities = {
			Oas.star("bereer", "star_blue_giant", 1200f, 500f, 15f, 2f, 5f),
			Oas.background("graphics/backgrounds/background5.jpg"),
			Oas.systemColor(new Color(200, 200, 255)),
			Oas.relay(3000f, 200f, 0f, 35f, "Bereer Sensor Array", "bereer_sensor_array", "sensor_array_makeshift").faction("osiris"),
			Oas.planet(200f, 400f, 90f, 55f, "Bereer I", "bereer1", "lava", 0f),
			Oas.planet(400f, 400f, 110f, 110f, "Bereer II", "bereer2", "barren2", 0f),
			Oas.jump(500f, 400f, 0f, 240f, "Sun's Gate Jump Point", "suns_gate_jumppoint"),
			Oas.planet(400f, 400f, 140f, 135f, "Sun's Gate", "suns_gate", "desert", 0f).description("planet_suns_gate"),
			Oas.relay(500, 200f, 0f, 160f, "Sun's Gate Relay", "suns_gate_relay", "comm_relay").faction("osiris"),
			Oas.planet(400f, 400f, 110f, 270f, "Eria", "eria", "terran", 0f).description("planet_eria"),
			Oas.relay(300f, 200f, 0f, 160f, "Eria Nav Buoy", "eria_nav_buoy", "nav_buoy").faction("osiris"),
			Oas.ring(100f, 100f, 170f, "rings_dust0", 0, false),
			Oas.ring(100f, 100f, 160f, "rings_dust0", 2, false),
			Oas.ring(100f, 100f, 180f, "rings_dust0", 1, false),
			Oas.rings(0f, 400f, 600f, "Bereer's Belt"),
			Oas.planet(1400f, 1000f, 260f, 170f, "Airo", "airo", "gas_giant", 0f),
			Oas.planet(600f, 300f, 75f, 245f, "Fura", "fura", "lava", 0f).description("planet_fura").focusTo("airo"),
			Oas.ring(100f, 100f, 170f, "rings_dust0", 1, false).focusTo("airo"),
			Oas.ring(100f, 100f, 160f, "rings_dust0", 0, false).focusTo("airo"),
			Oas.ring(100f, 100f, 180f, "rings_dust0", 2, false).focusTo("airo"),
			Oas.rings(0f, 0f, 600f, "Airo's Belt").focusTo("airo"),
			Oas.nebula(StarAge.YOUNG)
		};
		Oas.generateAnyEntity(sector, entities, "Bereer");
	}
}