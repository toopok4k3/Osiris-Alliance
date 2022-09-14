package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import java.awt.Color;

// The frames need the love.
public class OasLoveEveryFrameEffect implements EveryFrameWeaponEffectPlugin {
    private float sinceLast = 0.0f;
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;
		ShipAPI ship = weapon.getShip();
		if(ship == null) return;
		ShipSystemAPI system = ship.getSystem();
		if(system == null) return;
		
		final float effectLevel = system.getEffectLevel();
		final boolean showPulse = effectLevel > 0.0f && !ship.isHulk();

        sinceLast += amount;
        if(showPulse) {
            weapon.getAnimation().setFrame(1);
            float alpha = (sinceLast % 0.666f) * 4.0f;
            alpha *= alpha;
            if(alpha > 1.0f) alpha = 1.0f;
            if(alpha < 0.0f) alpha = 0.0f;
            weapon.getSprite().setColor(new Color(1.0f, 0.4f, 0.4f, alpha));
        } else {
            weapon.getAnimation().setFrame(0);
            weapon.getSprite().setColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
        }
    }
}
