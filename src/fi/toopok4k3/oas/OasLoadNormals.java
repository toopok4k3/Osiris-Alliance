package fi.toopok4k3.oas;

import com.fs.starfarer.api.Global;
//import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class OasLoadNormals {

    public static void loadNormals() {
        final boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");

        if (hasGraphicsLib) {
            ShaderLib.init();
            //LightData.readLightDataCSV("data/lights/oas_light_data.csv");
            TextureData.readTextureDataCSV("data/lights/oas_texture_data.csv");
        }
    }
}
