/*
 * File updated ~ 10 - 2 - 2023 ~ Leaf
 */

package leaf.cosmere.sandmastery.common.manifestation;

import leaf.cosmere.api.Manifestations;
import leaf.cosmere.api.Taldain;
import leaf.cosmere.api.spiritweb.ISpiritweb;
import leaf.cosmere.common.cap.entity.SpiritwebCapability;
import leaf.cosmere.sandmastery.common.capabilities.SandmasterySpiritwebSubmodule;
import leaf.cosmere.sandmastery.common.utils.MiscHelper;
import leaf.cosmere.sandmastery.common.utils.SandmasteryConstants;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class MasteryElevate extends SandmasteryManifestation
{
	public MasteryElevate(Taldain.Mastery mastery)
	{
		super(mastery);
	}

	@Override
	public void tick(ISpiritweb data)
	{
		boolean enabledViaHotkey = MiscHelper.enabledViaHotkey(data, SandmasteryConstants.ELEVATE_HOTKEY_FLAG);
		if (getMode(data) > 0 && enabledViaHotkey)
		{
			performEffectServer(data);
		}
	}

	protected void performEffectServer(ISpiritweb data)
	{
		SpiritwebCapability playerSpiritweb = (SpiritwebCapability) data;
		SandmasterySpiritwebSubmodule submodule = (SandmasterySpiritwebSubmodule) playerSpiritweb.getSubmodule(Manifestations.ManifestationTypes.SANDMASTERY);

		if (getMode(data) < 3)
		{
			return; // It's shown in White Sand that one can't lift themselves with fewer than 3 ribbons
		}
		if (!submodule.adjustHydration(-10, false))
		{
			return; // Too dehydrated to use sand mastery
		}
		if (!enoughChargedSand(data))
		{
			return;
		}

		LivingEntity living = data.getLiving();
		int distFromGround = MiscHelper.distanceFromGround(living);
		int maxLift = getMode(data) * 4;
		if (distFromGround > maxLift)
		{
			return;
		}


		Vec3 direction = (maxLift - distFromGround) > 3 ? new Vec3(0, 0.75, 0) : new Vec3(0, 0.15, 0);

		living.setDeltaMovement(direction);
		living.hurtMarked = true; // Allow the game to move the player
		living.resetFallDistance();

		submodule.adjustHydration(-10, true);
		useChargedSand(data);
	}
}
