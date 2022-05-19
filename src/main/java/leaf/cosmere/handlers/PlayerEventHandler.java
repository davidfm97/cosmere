/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 */

package leaf.cosmere.handlers;

import leaf.cosmere.Cosmere;
import leaf.cosmere.cap.entity.ISpiritweb;
import leaf.cosmere.cap.entity.SpiritwebCapability;
import leaf.cosmere.constants.Metals;
import leaf.cosmere.manifestation.AManifestation;
import leaf.cosmere.registry.AttributesRegistry;
import leaf.cosmere.registry.EffectsRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Cosmere.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler
{
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event)
	{
		event.getOriginal().revive();

		SpiritwebCapability.get(event.getOriginal()).ifPresent((oldSpiritWeb) ->
				SpiritwebCapability.get(event.getPlayer()).ifPresent((newSpiritWeb) ->
				{
					//copy across anything from the old player if needed
					//Metals ingested?
					//Stormlight?

					newSpiritWeb.transferFrom(oldSpiritWeb);
				}));

		var oldAttMap = event.getOriginal().getAttributes();
		var newAttMap = event.getPlayer().getAttributes();

		// A player's manifestations is now determined by attributes, which lets me do cool things like mess with strength in a power.
		// So if we've set a base value for an attribute on the player, copy it to the new one.
		for (RegistryObject<Attribute> attributeRegistryObject : AttributesRegistry.COSMERE_ATTRIBUTES.values())
		{
			if (attributeRegistryObject != null && attributeRegistryObject.isPresent())
			{
				AttributeInstance oldPlayerAttribute = oldAttMap.getInstance(attributeRegistryObject.get());
				AttributeInstance newPlayerAttribute = newAttMap.getInstance(attributeRegistryObject.get());

				if (newPlayerAttribute != null && oldPlayerAttribute != null)
				{
					// make sure that they match what the old player entity had.
					if (oldPlayerAttribute.getBaseValue() > 0)
					{
						newPlayerAttribute.setBaseValue(oldPlayerAttribute.getBaseValue());
					}
					//clear out the attributes that were placed on the newly cloned player at creation
					else if (newPlayerAttribute.getBaseValue() > 0)
					{
						newPlayerAttribute.setBaseValue(0);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onTrackPlayer(PlayerEvent.StartTracking startTracking)
	{
		SpiritwebCapability.get(startTracking.getPlayer()).ifPresent(cap ->
		{
			cap.syncToClients(null);
		});
	}

	@SubscribeEvent
	public void onItemTossEvent(ItemTossEvent event)
	{
		if (!event.getPlayer().level.isClientSide)
		{
			//if (event.getEntityItem().getItem().getItem() instanceof ItemShardBlade)
			{
				/*
				//if we haven't got a shard blade in inv, put it in the inventory
				if (event.getPlayer().getCapability(CosmereCapabilities.SUMMON_SHARDBLADE, null).getInventoryShardblade().getStackInSlot(0) == null)
				{
					event.getPlayer().getCapability(CosmereCapabilities.SUMMON_SHARDBLADE, null).setIsShardbladeInInventory(true);

					ItemStack itemStack = event.getEntityItem().getEntityItem();
					ItemStack test = itemStack.copy();

					event.getPlayer().getCapability(CosmereCapabilities.SUMMON_SHARDBLADE, null).getInventoryShardblade().setInventorySlotContents(0, test);


					event.getEntityItem().isDead = true;
				}
				PacketDispatcher.sendTo(new SyncShardbladeData(event.getPlayer().getCapability(CosmereCapabilities.SUMMON_SHARDBLADE, null)), (EntityPlayerMP) event.getPlayer());
				*/
			}
		}
	}

	@SubscribeEvent
	public void onXPChange(PlayerXpEvent.XpChange event)
	{
		boolean isRemote = event.getEntityLiving().level.isClientSide;
		if (isRemote)
		{
			return;
		}

		//Feruchemical Zinc
		{
			MobEffectInstance tappingZincEffect = event.getPlayer().getEffect(EffectsRegistry.TAPPING_EFFECTS.get(Metals.MetalType.ZINC).get());
			MobEffectInstance storingZincEffect = event.getPlayer().getEffect(EffectsRegistry.STORING_EFFECTS.get(Metals.MetalType.ZINC).get());

			if (tappingZincEffect != null)
			{
				event.setAmount(event.getAmount() * (tappingZincEffect.getAmplifier() + 2));
			}
			if (storingZincEffect != null)
			{
				event.setAmount(event.getAmount() / (storingZincEffect.getAmplifier() + 2));
			}
		}

		RegistryObject<Attribute> xpGainRateAttribute = AttributesRegistry.COSMERE_ATTRIBUTES.get(Metals.MetalType.COPPER.getName());
		if (xpGainRateAttribute != null && xpGainRateAttribute.isPresent())
		{
			AttributeInstance attribute = event.getPlayer().getAttribute(xpGainRateAttribute.get());
			if (attribute != null)
			{
				event.setAmount((int) (event.getAmount() * attribute.getValue()));
			}
		}
	}
}
