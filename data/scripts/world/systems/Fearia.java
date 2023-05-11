package data.scripts.world.systems;

import java.awt.Color;

import data.scripts.Oas;
import data.scripts.Oas.AnyEntity;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;


public class Fearia {
    public void generate(SectorAPI sector) {
        AnyEntity[] entities = {
            Oas.star("fearia", "star_yellow", 850f, 400f),
            Oas.background("graphics/backgrounds/background5.jpg"),
            Oas.systemColor(new Color(255, 255, 233)),
            Oas.planet(1500f, 0f, 150f, 230f, "Gaero", "gaero", "barren3", 0f),
            Oas.relay(0f, 0f, 0f, 35f, "Fearia Sensor Array", "fearia_sensor_array", "sensor_array_makeshift").faction("osiris"),
            Oas.relay(0f, 0f, 0f, 75f, "Fearia Relay", "fearia_relay", "comm_relay").faction("osiris"),
            Oas.relay(0f, 0f, 0f, 160f, "Fearia Nav Buoy", "fearia_nav_buoy", "nav_buoy").faction("osiris"),
            Oas.planet(600f, 400f, 110f, 270f, "Eria", "eria", "terran", 0f).description("planet_eria")
            // baopab
            // Oba
        };
        Oas.generateAnyEntity(sector, entities, "Fearia");
    }
}
