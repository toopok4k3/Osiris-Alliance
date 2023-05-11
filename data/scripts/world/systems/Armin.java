package data.scripts.world.systems;

import java.awt.Color;

import data.scripts.Oas;
import data.scripts.Oas.AnyEntity;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.util.Misc;

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
			Oas.star("armin", "star_orange", 775f, 400f, 10f, 1f, 3f),
			Oas.background("graphics/backgrounds/background2.jpg"),
			Oas.systemColor(new Color(255, 245, 200)),
            Oas.relay(2000f, 0f, 0f, 150f, "Armin Relay", "armin_relay", "comm_relay_makeshift").faction("osiris"),
            Oas.jump(500f, 500f, 0f, 25f, "Armin Jump-point", "armin_jump1"),
            //Oas.random(200f, 700f, StarAge.OLD, 3, 5, false, true),
			Oas.planet(500f, 1000f, 150f, 230f, "Armin I", "armin1", "barren", 0f),
            Oas.ring(100f, 0f, 170f, "rings_dust0", 0, false),
            Oas.station(0f, 100f, 30f, "Abandoned Mining Station", "harman_abandoned_station", "station_side06", "abandoned_station2", 0f).description("harman_abandoned_station"),
			Oas.ring(100f, 100f, 160f, "rings_dust0", 2, false),
			Oas.ring(100f, 150f, 180f, "rings_dust0", 1, false),
            Oas.rings(-125f, 125f, 250f, "Harman's Belt"),
            Oas.relay(300f, 700f, 0f, 230f, "Harman Sensor Array", "harman_sensor_array", "sensor_array_makeshift").faction("osiris"),
            Oas.planet(500f, 0f, 60f, 230f, "Harman", "harman", "barren", 0f).description("planet_harman"),
            Oas.station(180f, 0f, 30f, "Harman Fuel Depot", "harman_station", "station_lowtech1", "hound_hangar", 0f).focusTo("harman").description("station_harman"),
            Oas.jump(0f, 0f, 0f, 290f, "Harman Jump-point", "armin_jump2"),
            //Oas.random(300f, 700f, StarAge.OLD, 1, 2, false, true),
            Oas.relay(0f, 1500f, 0f, 170f, "Harman Nav Buoy", "harman_nav_buoy", "nav_buoy_makeshift").faction("osiris"),
            Oas.planet(2250f, 1200f, 450f, 115f, "Gura", "gura", "gas_giant", 0f),//.description("planet_gura"),
            Oas.ring(800f, 150f, 180f, "rings_dust0", 1, true).focusTo("gura"),
            Oas.planet(250f, 200f, 95f, 300f, "Fugura", "fugura", "cryovolcanic", 0f).focusTo("gura").description("planet_fugura"),
            Oas.nebula(StarAge.OLD)
		};
		Oas.generateAnyEntity(sector, entities, "Armin");
		SectorEntityToken entity = Global.getSector().getStarSystem("armin").getEntityById("harman_abandoned_station");
		Misc.setAbandonedStationMarket("harman_abandoned_station_market", entity);
        entity.setDiscoverable(true);
        entity.setDiscoveryXP(1000.0f);
        entity.setSensorProfile(150.0f);
        entity.getMemoryWithoutUpdate().set("$oasOccupied", true);
		//entity.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "hermes_d_Hull", null);
	
    }
}