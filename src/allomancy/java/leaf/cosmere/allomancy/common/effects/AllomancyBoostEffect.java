/*
 * File updated ~ 28 - 2 - 2023 ~ Leaf
 */

package leaf.cosmere.allomancy.common.effects;

import leaf.cosmere.allomancy.common.capabilities.AllomancySpiritwebSubmodule;
import leaf.cosmere.allomancy.common.registries.AllomancyAttributes;
import leaf.cosmere.api.Manifestations;
import leaf.cosmere.api.Metals;
import leaf.cosmere.common.cap.entity.SpiritwebCapability;
import leaf.cosmere.common.effects.MobEffectBase;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AllomancyBoostEffect extends MobEffectBase
{
	public AllomancyBoostEffect(Metals.MetalType type, MobEffectCategory effectType)
	{
		super(effectType, type.getColorValue());

		for (Metals.MetalType metalType : Metals.MetalType.values())
		{
			if (metalType.hasAssociatedManifestation() && AllomancyAttributes.ALLOMANCY_ATTRIBUTES.containsKey(metalType))
			{
				addAttributeModifier(
						AllomancyAttributes.ALLOMANCY_ATTRIBUTES.get(metalType).get(),
						"ad9ba05c-d9e5-4f74-8f25-fa65139d178c",
						0.334D,// todo config - Need to figure out an alternative to config boost amount //AllomancyConfigs.SERVER.boostAmount.get(),
						AttributeModifier.Operation.MULTIPLY_TOTAL);
				//todo boost other manifestation types

			}
		}
		//todo boost other manifestation types

	}

	@Override
	public void applyEffectTick(LivingEntity livingEntity, int amplifier)
	{
		//todo boost metal drain balancing

		if (isActiveTick(livingEntity))
		{
			SpiritwebCapability.get(livingEntity).ifPresent(data ->
			{
				AllomancySpiritwebSubmodule allo = (AllomancySpiritwebSubmodule) ((SpiritwebCapability) data).getSubmodule(Manifestations.ManifestationTypes.ALLOMANCY);

				for (Metals.MetalType metalType : Metals.MetalType.values())
				{
					if (!metalType.hasAssociatedManifestation() || metalType == Metals.MetalType.DURALUMIN)
					{
						continue;
					}

					int ingestedMetalAmount = allo.getIngestedMetal(metalType);

					//if metal exists
					if (ingestedMetalAmount > 0)
					{
						//drain metals that are actively being burned
						if (data.canTickManifestation(Manifestations.ManifestationTypes.ALLOMANCY.getManifestation(metalType.getID())))
						{
							final int amountToAdjust =
									ingestedMetalAmount > 30 ? (ingestedMetalAmount / 2) : ingestedMetalAmount;
							allo.adjustIngestedMetal(
									metalType,
									-amountToAdjust, //take amount away
									true);

						}
					}
				}
			});
		}
	}
}
