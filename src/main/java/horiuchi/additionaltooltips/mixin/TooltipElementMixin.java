package horiuchi.additionaltooltips.mixin;

import horiuchi.additionaltooltips.AdditionalTooltipOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.TooltipElement;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionEnum;
import net.minecraft.client.option.enums.DescriptionPromptEnum;
import net.minecraft.core.block.BlockLogicEdible;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.item.*;
import net.minecraft.core.item.material.ArmorMaterial;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.player.inventory.slot.Slot;
import net.minecraft.core.player.inventory.slot.SlotResult;
import net.minecraft.core.util.helper.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(TooltipElement.class)
public abstract class TooltipElementMixin {
	@Unique
	private String formatNum(float num) {
		if (num == (int) num) {
			return Integer.toString((int) num);
		}
		return String.format("%.1f", num);
	}

	@Unique
	private boolean shouldDisplayTooltip(OptionEnum<AdditionalTooltipOptions.ShowTooltip> option) {
		return option.value != AdditionalTooltipOptions.ShowTooltip.DONT_SHOW &&
			((option.value == AdditionalTooltipOptions.ShowTooltip.ON_SHOW_DESCRIPTION && DescriptionPromptEnum.showDescription()) ||
				(option.value == AdditionalTooltipOptions.ShowTooltip.PROMPT && AdditionalTooltipOptions.KEY_SHOW_ADDITIONAL_TOOLTIP.isPressed()) ||
				option.value == AdditionalTooltipOptions.ShowTooltip.ALWAYS_SHOW);
	}

	@Unique
	private boolean AttemptDrawPrompt(boolean alreadyDrawn, StringBuilder text, OptionEnum<AdditionalTooltipOptions.ShowTooltip> option) {
		if (!alreadyDrawn && option.value == AdditionalTooltipOptions.ShowTooltip.PROMPT) {
			text.append('\n').append(TextFormatting.LIGHT_GRAY).append((I18n.getInstance().translateKeyAndFormat("gui.tooltip.prompt.description", AdditionalTooltipOptions.KEY_SHOW_ADDITIONAL_TOOLTIP.getKeyName())));
			return true;
		}
		return alreadyDrawn;
	}

	@Inject(method = "getTooltipText(Lnet/minecraft/core/item/ItemStack;ZLnet/minecraft/core/player/inventory/slot/Slot;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
	private void addAdditionalTooltipText(ItemStack itemStack, boolean showDescription, Slot slot, CallbackInfoReturnable<String> cir) {
		boolean drawnPrompt = (!showDescription && !(slot instanceof SlotResult) && GameSettings.ITEM_DESCRIPTIONS.value != DescriptionPromptEnum.NEVER_PROMPT) && GameSettings.KEY_DESCRIPTION.getKeyCode() == AdditionalTooltipOptions.KEY_SHOW_ADDITIONAL_TOOLTIP.getKeyCode();
		StringBuilder text = new StringBuilder(cir.getReturnValue());
		Item item = itemStack.getItem();

		// Armor Stats
		if (item instanceof ItemArmor<?> armor) {
			if (shouldDisplayTooltip(AdditionalTooltipOptions.SHOW_ARMOR_PROTECTION)) {
				text.append("\n");
				if (armor.getArmorShape() instanceof HumanArmorShape) {
					String slotName = "?";
					switch (armor.getArmorShape().getSlotIndex()) {
						case 0 -> slotName = "Head";
						case 1 -> slotName = "Chest";
						case 2 -> slotName = "Legs";
						case 3 -> slotName = "Feet";
					}
					text.append(String.format("Protection when on %s:", slotName));
				}
				else {
					text.append("Protection when equipped:");
				}

				ArmorMaterial material = armor.getArmorMaterial();
				if (material != null) {
					text.append(TextFormatting.LIGHT_BLUE);
					for(DamageType damageType : DamageType.values()) {
						if(!damageType.shouldDisplay())
							continue;

						String damageTypeName = I18n.getInstance().translateKey(damageType.getLanguageKey());
						String protection = formatNum(100.0F * (material.getProtection(damageType) * armor.getArmorPieceProtectionPercentage()));
						text.append(String.format("\n%s%% %s", protection, damageTypeName));
					}
				}
			}
			else {
				drawnPrompt = AttemptDrawPrompt(drawnPrompt, text, AdditionalTooltipOptions.SHOW_ARMOR_PROTECTION);
			}
		}

		// Food Stats
		if (item instanceof ItemFood || (item instanceof ItemPlaceable placeable && placeable.block.getLogic() instanceof BlockLogicEdible)) {
			if (shouldDisplayTooltip(AdditionalTooltipOptions.SHOW_FOOD)) {
				if (item instanceof ItemFood food) {
					int healAmount = food.getHealAmount(itemStack);
					if (healAmount != 0.0F) {
						text.append('\n').append(TextFormatting.RED).append("♥").append(TextFormatting.LIGHT_GRAY).append(" x ").append(formatNum(healAmount / 2.0F));
						int ticksPerHeal = food.getTicksPerHeal(itemStack);
						if (ticksPerHeal != 0.0F && AdditionalTooltipOptions.SHOW_FOOD_REGEN_TIME.value) {
							text.append(" over ").append(formatNum(healAmount*(ticksPerHeal/20.0F))).append("s");
						}
					}
				}
				else if (item instanceof ItemPlaceable placeable && placeable.block.getLogic() instanceof BlockLogicEdible edibleLogic) {
					int healAmount = edibleLogic.getHealAmount(null, null);
					text.append('\n').append(TextFormatting.RED).append("♥").append(TextFormatting.LIGHT_GRAY).append(String.format(" x %s per slice (%s total)", formatNum(healAmount / 2.0F), edibleLogic.maxBites));
				}
			}
			else {
				drawnPrompt = AttemptDrawPrompt(drawnPrompt, text, AdditionalTooltipOptions.SHOW_FOOD);
			}
		}

		// Durability
		if (itemStack.isItemStackDamageable()) {
			if (shouldDisplayTooltip(AdditionalTooltipOptions.SHOW_DURABILITY)) {
				int offset = item == Items.ARMOR_QUIVER || item == Items.PAINTBRUSH ? 0 : 1;
				int durability = itemStack.getMaxDamage();
				int remainingUses = item == Items.PAINTBRUSH && itemStack.getData().getInteger("Color") == 0 ? 0 : durability - itemStack.getMetadata();
				text.append('\n').append(TextFormatting.LIGHT_GRAY).append(remainingUses + offset).append(" / ").append(durability + offset);
			}
			else {
				drawnPrompt = AttemptDrawPrompt(drawnPrompt, text, AdditionalTooltipOptions.SHOW_DURABILITY);
			}
		}

		cir.setReturnValue(text.toString());
	}
}
