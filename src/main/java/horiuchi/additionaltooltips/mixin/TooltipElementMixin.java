package horiuchi.additionaltooltips.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import horiuchi.additionaltooltips.AdditionalTooltipOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.TooltipElement;
import net.minecraft.client.gui.guidebook.SlotGuidebook;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionEnum;
import net.minecraft.client.option.enums.DescriptionPromptEnum;
import net.minecraft.client.render.MapItemRenderer;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.TextureBuffered;
import net.minecraft.client.util.helper.Colors;
import net.minecraft.core.block.BlockLogicEdible;
import net.minecraft.core.block.material.MaterialColor;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.item.*;
import net.minecraft.core.item.material.ArmorMaterial;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.player.inventory.slot.Slot;
import net.minecraft.core.player.inventory.slot.SlotResult;
import net.minecraft.core.util.helper.Color;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.saveddata.maps.ItemMapSavedData;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(TooltipElement.class)
public abstract class TooltipElementMixin extends Gui {
	@Unique
	private ItemStack renderItem = null;
	@Unique
	private static final int FLAG_WIDTH = 24;
	@Unique
	private static final int FLAG_HEIGHT = 16;
	@Unique
	private boolean renderFlag;
	@Unique
	private boolean renderMap;
	@Shadow
	Minecraft mc;

	@ModifyExpressionValue(method = "render(Ljava/lang/CharSequence;IIIIIIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/util/helper/MathHelper;ceil(D)I"))
	private int modifyBackgroundHeight(int original) {
		if (renderFlag) {
			return original + 9 * ((AdditionalTooltipOptions.FLAG_ART_SCALE.value + 1) * 2);
		}
		else if (renderMap) {
			return original + 9 * ((AdditionalTooltipOptions.MAP_ART_SCALE.value + 1) * 3);
		}
		return original;
	}

	@Unique
	private byte[] unpackFlagColors(byte[] packed) {
		byte[] unpacked = new byte[384];

		for(int i = 0; i < 96; ++i) {
			unpacked[i * 4 + 0] = (byte)((packed[i] & 3) >> 0);
			unpacked[i * 4 + 1] = (byte)((packed[i] & 12) >> 2);
			unpacked[i * 4 + 2] = (byte)((packed[i] & 48) >> 4);
			unpacked[i * 4 + 3] = (byte)((packed[i] & 192) >> 6);
		}

		return unpacked;
	}

	@Unique
	private void renderFlagTooltip(int x, int y) {
		CompoundTag flagData = renderItem.getData().getCompoundOrDefault("FlagData", null);

		if (flagData == null) {
			return;
		}

		byte[] colorIndexes = unpackFlagColors(flagData.getByteArray("Colors"));
		int[] colorData = {-1, -1, -1};

		ListTag list = flagData.getList("Items");
		for(int i = 0; i < list.tagCount(); ++i) {
			CompoundTag compound = (CompoundTag)list.tagAt(i);
			ItemStack stack = ItemStack.readItemStackFromNbt(compound);
			if (stack != null && stack.getItem().equals(Items.DYE)) {
				colorData[i] = Colors.allFlagColors[TextFormatting.get(DyeColor.MASK_COLOR - stack.getMetadata()).id].getARGB();
			}
		}

		this.drawGuiTexture(this.mc.textureManager, x, y, FLAG_WIDTH * (AdditionalTooltipOptions.FLAG_ART_SCALE.value + 1), FLAG_HEIGHT * (AdditionalTooltipOptions.FLAG_ART_SCALE.value + 1), "/assets/minecraft/textures/entity/flag_ui.png");

		GLRenderer.pushFrame();
		GLRenderer.enableState(State.BLEND);
		GLRenderer.setBlendFunc(BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFactor.SRC_COLOR);
		GLRenderer.setShader(Shaders.COLOR);

		TessellatorGeneral tessellator = GLRenderer.getTessellator();

		for (int color = 0; color < 3; color++) {
			if (colorData[color] == -1) {
				continue;
			}

			GLRenderer.setColor1i(colorData[color]);

			tessellator.startDrawingQuads();

			for (int dx = 0; dx < FLAG_WIDTH; dx++) {
				for (int dy = 0; dy < FLAG_HEIGHT; dy++) {
					if (colorIndexes[dx + FLAG_WIDTH * dy] - 1 != color) {
						continue;
					}

					int minX = x + dx * (AdditionalTooltipOptions.FLAG_ART_SCALE.value + 1);
					int minY = y + dy * (AdditionalTooltipOptions.FLAG_ART_SCALE.value + 1);
					int maxX = minX + (AdditionalTooltipOptions.FLAG_ART_SCALE.value + 1);
					int maxY = minY + (AdditionalTooltipOptions.FLAG_ART_SCALE.value + 1);

					tessellator.addVertex(minX, maxY, 0.0F);
					tessellator.addVertex(maxX, maxY, 0.0F);
					tessellator.addVertex(maxX, minY, 0.0F);
					tessellator.addVertex(minX, minY, 0.0F);
				}
			}

			tessellator.draw();
		}

		GLRenderer.disableState(State.BLEND);
		GLRenderer.popFrame();
	}

	@Unique
	private void renderMapTooltip(int x, int y) {
		int[] mapImageData = new int[MapItemRenderer.IMAGE_AREA];
		TextureBuffered mapTexture = Minecraft.getMinecraft().textureManager.loadBufferedTexture(new BufferedImage(MapItemRenderer.IMAGE_WIDTH, MapItemRenderer.IMAGE_HEIGHT, 2));
		TextureManager textureManager = Minecraft.getMinecraft().textureManager;
		TessellatorGeneral tessellator = GLRenderer.getTessellator();

		int size = 24 * (AdditionalTooltipOptions.MAP_ART_SCALE.value + 1);
		this.drawGuiTexture(this.mc.textureManager, x, y, size, size, "/assets/minecraft/textures/misc/mapbg.png");

		byte scale = renderItem.getData().getByteOrDefault("scale", (byte) 3);

		String s = String.format("map_%s_scale_%s", renderItem.getMetadata(), scale);
		ItemMapSavedData mapData = (ItemMapSavedData)this.mc.currentWorld.getSavedData(ItemMapSavedData.class, s);
		if (mapData == null) {
			return;
		}

		for(int i = 0; i < MapItemRenderer.IMAGE_AREA; ++i) {
			int colorIndex = mapData.colors[i];
			if (colorIndex >> 2 == 0) {
				mapImageData[i] = (i + i / 128 & 1) * 8 + 16 << 24;
			} else {
				int col = MaterialColor.getColorFromIndex(colorIndex >> 2);
				int i1 = colorIndex & 3;
				int shade = 220;
				if (i1 == 2) {
					shade = 255;
				}

				if (i1 == 0) {
					shade = 180;
				}

				int red = net.minecraft.core.util.helper.Color.redFromInt(col) * shade / Color.MASK_CHANNEL;
				int green = net.minecraft.core.util.helper.Color.greenFromInt(col) * shade / Color.MASK_CHANNEL;
				int blue = net.minecraft.core.util.helper.Color.blueFromInt(col) * shade / Color.MASK_CHANNEL;
				mapImageData[i] = Color.intToIntARGB(255, red, green, blue);
			}
		}

		textureManager.updateTextureData(mapImageData, MapItemRenderer.IMAGE_WIDTH, MapItemRenderer.IMAGE_HEIGHT, mapTexture.id());
		mapTexture.bind();
		size -= 2 * (AdditionalTooltipOptions.MAP_ART_SCALE.value + 1);
		x += (AdditionalTooltipOptions.MAP_ART_SCALE.value + 1);
		y += (AdditionalTooltipOptions.MAP_ART_SCALE.value + 1);
		GLRenderer.enableState(State.BLEND);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y +size, -0.01, 0, 1);
		tessellator.addVertexWithUV(x + size, y + size, -0.01, 1, 1);
		tessellator.addVertexWithUV(x + size, y, -0.01, 1, 0);
		tessellator.addVertexWithUV(x, y, -0.01, 0, 0);
		tessellator.draw();
		GLRenderer.disableState(State.BLEND);
	}

	@Inject(method = "render(Ljava/lang/CharSequence;IIIIIIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/font/FontRenderer;renderWidthConstrained(Ljava/lang/CharSequence;III)Lnet/minecraft/client/render/font/RenderIntegerConstrainedBase;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void doAdditionalRendering(CharSequence chars, int mouseX, int mouseY, int offsetX, int offsetY, int maxWidth, int maxHeight, boolean canOffset, CallbackInfo callbackInfo,
									   List<String> lines, int rawWidth, int rawHeight, int padding, int screenW, int screenH, int finalX, int finalY) {
		if (renderItem == null) {
			return;
		}

		if (renderFlag) {
			renderFlagTooltip(finalX, finalY + 10);
		} else if (renderMap) {
			renderMapTooltip(finalX, finalY + 10 + AdditionalTooltipOptions.MAP_ART_SCALE.value);
		}
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
		renderItem = itemStack;
		boolean drawnPrompt = (!showDescription && !(slot instanceof SlotResult) && GameSettings.ITEM_DESCRIPTIONS.value != DescriptionPromptEnum.NEVER_PROMPT) && GameSettings.KEY_DESCRIPTION.getKeyCode() == AdditionalTooltipOptions.KEY_SHOW_ADDITIONAL_TOOLTIP.getKeyCode();
		StringBuilder text = new StringBuilder(cir.getReturnValue());
		Item item = itemStack.getItem();

		// Armor Stats
		if (item instanceof ItemArmor<?> armor) {
			if (shouldDisplayTooltip(AdditionalTooltipOptions.SHOW_ARMOR_PROTECTION)) {
				text.append("\n");
				if (armor.getArmorShape() instanceof HumanArmorShape armorShape) {
					String slotName = "?";
					switch (armorShape) {
						case HEAD -> slotName = "Head";
						case CHEST -> slotName = "Chest";
						case LEGS -> slotName = "Legs";
						case BOOTS -> slotName = "Feet";
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
						String protection = new DecimalFormat("#.#").format((100.0F * (material.getProtection(damageType) * armor.getArmorPieceProtectionPercentage())));
						text.append(String.format("\n+%s%% %s", protection, damageTypeName));
					}
				}
			}
			else {
				drawnPrompt = AttemptDrawPrompt(drawnPrompt, text, AdditionalTooltipOptions.SHOW_ARMOR_PROTECTION);
			}
		}

		// Food Stats
		if (item instanceof ItemFood a || (item instanceof ItemPlaceable placeable && placeable.block.getLogic() instanceof BlockLogicEdible)) {
			if (shouldDisplayTooltip(AdditionalTooltipOptions.SHOW_FOOD)) {
				if (item instanceof ItemFood food) {
					int healAmount = food.getHealAmount(itemStack);
					if (healAmount != 0.0F) {
						text.append('\n').append(TextFormatting.RED).append("♥").append(TextFormatting.LIGHT_GRAY).append(" x ").append(new DecimalFormat("#.#").format((healAmount / 2.0F)));
						int ticksPerHeal = food.getTicksPerHeal(itemStack);
						if (ticksPerHeal != 0.0F && AdditionalTooltipOptions.SHOW_FOOD_REGEN_TIME.value) {
							text.append(" over ").append(new DecimalFormat("#.#").format((healAmount*(ticksPerHeal/20.0F)))).append("s");
						}
					}
				}
				else if (item instanceof ItemPlaceable placeable && placeable.block.getLogic() instanceof BlockLogicEdible edibleLogic) {
					int healAmount = edibleLogic.getHealAmount(null, null);
					text.append('\n').append(TextFormatting.RED).append("♥").append(TextFormatting.LIGHT_GRAY).append(String.format(" x %s per slice (%s total)", new DecimalFormat("#.#").format((healAmount / 2.0F)), edibleLogic.maxBites));
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

		// Flag Art
		if (item instanceof ItemFlag flag && flag.hasFlagBeenDrawnOn(itemStack)) {
			if (shouldDisplayTooltip(AdditionalTooltipOptions.SHOW_FLAG_ART)) {
				renderFlag = true;
				text.insert(text.indexOf("\n"), StringUtils.repeat("\n\n", (AdditionalTooltipOptions.FLAG_ART_SCALE.value + 1)));
			}
			else {
				renderFlag = false;
				drawnPrompt = AttemptDrawPrompt(drawnPrompt, text, AdditionalTooltipOptions.SHOW_FLAG_ART);
			}
		}
		else {
			renderFlag = false;
		}

		// Map Art
		if (item instanceof ItemMap && ItemMap.hasInitialized(itemStack) && !(slot instanceof SlotGuidebook)) {
			if (shouldDisplayTooltip(AdditionalTooltipOptions.SHOW_MAP_ART)) {
				renderMap = true;
				text.insert(text.indexOf("\n"), StringUtils.repeat("\n\n\n", (AdditionalTooltipOptions.MAP_ART_SCALE.value + 1)));
			}
			else {
				renderMap = false;
				drawnPrompt = AttemptDrawPrompt(drawnPrompt, text, AdditionalTooltipOptions.SHOW_MAP_ART);
			}
		}
		else {
			renderMap = false;
		}

		cir.setReturnValue(text.toString());
	}
}
