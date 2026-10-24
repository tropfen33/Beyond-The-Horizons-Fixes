package com.Tropfen_33.beyond_the_horizons_fixes.mixin.backpack;

import com.Tropfen_33.beyond_the_horizons_fixes.config.FixesConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks Sophisticated Core's InventoryHandler (the storage behind backpacks and Sophisticated
 * Storage blocks). Every insertion path (hand, shift-click, hoppers, pickup upgrade, ...)
 * ends up asking isItemValid(slot, stack, player), so this is the single place to enforce the limit.
 *
 * The target is referenced by name so this mod needs no compile dependency on Sophisticated Core.
 */
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler", remap = false)
public abstract class InventoryHandlerMixin {

	@Inject(
			method = "isItemValid(ILnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Z",
			at = @At("RETURN"),
			cancellable = true,
			remap = false
	)
	private void bthFixes$limitSlotsPerItem(int slot, ItemStack stack, net.minecraft.world.entity.player.Player player, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() || stack.isEmpty()) {
			return;
		}

		ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		int limit = FixesConfig.getBackpackSlotLimit(id);
		if (limit <= 0) {
			return;
		}

		ItemStackHandler self = (ItemStackHandler) (Object) this;

		// Stacking into a slot that already holds this item never creates a new slot.
		if (self.getStackInSlot(slot).is(stack.getItem())) {
			return;
		}

		int slotsWithItem = 0;
		int total = self.getSlots();
		for (int i = 0; i < total; i++) {
			if (i != slot && self.getStackInSlot(i).is(stack.getItem())) {
				slotsWithItem++;
				if (slotsWithItem >= limit) {
					cir.setReturnValue(false);
					return;
				}
			}
		}
	}
}
