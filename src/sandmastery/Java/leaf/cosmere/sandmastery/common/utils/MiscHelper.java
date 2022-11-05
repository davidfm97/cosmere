package leaf.cosmere.sandmastery.common.utils;

import leaf.cosmere.api.Manifestations;
import leaf.cosmere.api.Metals;
import leaf.cosmere.api.Roshar;
import leaf.cosmere.common.cap.entity.SpiritwebCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiscHelper {
    public static boolean checkIfNearbyInvestiture(ServerLevel pLevel, BlockPos pPos) {

        int range = 10;
        AABB areaOfEffect = new AABB(pPos).inflate(range, range, range);
        List<LivingEntity> entitiesToCheckForInvesiture = pLevel.getEntitiesOfClass(LivingEntity.class, areaOfEffect, e-> true);

        AtomicBoolean foundSomething = new AtomicBoolean(false);
        for (LivingEntity targetEntity : entitiesToCheckForInvesiture)
        {
			MobEffectInstance copperEffect = targetEntity.getEffect(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("allomancy", "copper_cloud")));
			if (copperEffect != null && copperEffect.getDuration() > 0)
			{
				//skip clouded entities.
				continue;
			}

            SpiritwebCapability.get(targetEntity).ifPresent(targetSpiritweb ->
            {
                // Check for Allomancy nearby
                for (Metals.MetalType metalType : Metals.MetalType.values())
                {
                    if (metalType == Metals.MetalType.COPPER) continue;
                    if (!metalType.hasAssociatedManifestation()) continue;

                    int metalTypeID = metalType.getID();
                    if (targetSpiritweb.canTickManifestation(Manifestations.ManifestationTypes.ALLOMANCY.getManifestation(metalTypeID)))
                    {
                        foundSomething.set(true);
                    }
                }

                // Check for Surgebinding nearby
                for (Roshar.Surges surge : Roshar.Surges.values())
                {
                    int surgeID = surge.getID();
                    if(targetSpiritweb.canTickManifestation(Manifestations.ManifestationTypes.SURGEBINDING.getManifestation(surgeID))) {
                        foundSomething.set(true);
                    }
                }
            });
        }
        return foundSomething.get();
    }
}
