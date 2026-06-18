package horiuchi.additionaltooltips;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.*;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.TextFormatting;
import org.lwjgl.input.Keyboard;

@Environment(EnvType.CLIENT)
public class AdditionalTooltipOptions {
	public static final KeyBinding KEY_SHOW_ADDITIONAL_TOOLTIP = new KeyBinding("key.additional.tooltip").setDefault(InputDevice.keyboard, Keyboard.KEY_LSHIFT);
	public static final OptionBoolean DRAW_BELOW_DESCRIPTION = new OptionBoolean("drawBelowDescription", true);
	public static final OptionBoolean DISABLE_FUNCTIONALITY = new OptionBoolean("disableFunctionality", false);

	public static final OptionEnum<TooltipColor> TOOLTIP_COLOR = new OptionEnum<>("tooltipColor", TooltipColor.class, TooltipColor.LIGHT_BLUE).withDisplayStringProvider(AdditionalTooltipOptions::getTooltipColorString);
	public static final OptionEnum<TooltipColor> TOOLTIP_DURABILITY_COLOR = new OptionEnum<>("tooltipDurabilityColor", TooltipColor.class, TooltipColor.WHITE).withDisplayStringProvider(AdditionalTooltipOptions::getTooltipColorString);

	public static final OptionEnum<ShowTooltip> SHOW_FOOD = new OptionEnum<>("showFood", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION).withDisplayStringProvider(AdditionalTooltipOptions::getShowTooltipString);
	public static final OptionBoolean SHOW_FOOD_REGEN_TIME = new OptionBoolean("showFoodRegenTime", true);
	public static final OptionEnum<ShowTooltip> SHOW_ARMOR_PROTECTION = new OptionEnum<>("showArmorProtection", ShowTooltip.class, ShowTooltip.PROMPT).withDisplayStringProvider(AdditionalTooltipOptions::getShowTooltipString);
	public static final OptionEnum<ShowTooltip> SHOW_TOOL_MINING_EFFICIENCY = new OptionEnum<>("showToolMiningEfficiency", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION).withDisplayStringProvider(AdditionalTooltipOptions::getShowTooltipString);
	public static final OptionEnum<ShowTooltip> SHOW_TOOL_DAMAGE = new OptionEnum<>("showToolDamage", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION).withDisplayStringProvider(AdditionalTooltipOptions::getShowTooltipString);
	public static final OptionBoolean SHOW_NON_SWORD_DAMAGE = new OptionBoolean("showNonSwordDamage", true);
	public static final OptionEnum<ShowTooltip> SHOW_ARROW_DAMAGE = new OptionEnum<>("showArrowDamage", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION).withDisplayStringProvider(AdditionalTooltipOptions::getShowTooltipString);
	public static final OptionEnum<ShowTooltip> SHOW_DURABILITY = new OptionEnum<>("showDurability", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION).withDisplayStringProvider(AdditionalTooltipOptions::getShowTooltipString);

	public static final OptionEnum<ShowTooltip> SHOW_FLAG_ART = new OptionEnum<>("showFlagArt", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION).withDisplayStringProvider(AdditionalTooltipOptions::getShowTooltipString);
	public static final OptionRange FLAG_ART_SCALE = new OptionRange("flagArtScale", 1, 3).withDisplayStringProvider((mc, i18n, option) -> (option.value + 1) + "x");
	public static final OptionEnum<ShowTooltip> SHOW_MAP_ART = new OptionEnum<>("showMapArt", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION).withDisplayStringProvider(AdditionalTooltipOptions::getShowTooltipString);
	public static final OptionRange MAP_ART_SCALE = new OptionRange("mapArtScale", 1, 3).withDisplayStringProvider((mc, i18n, option) -> (option.value + 1) + "x");

	public static String getShowTooltipString(Minecraft mc, I18n i18n, Option<ShowTooltip> option) {
		return i18n.translateKey("options." + option.value.name().toLowerCase());
	}

	@Environment(EnvType.CLIENT)
	public enum ShowTooltip {
		DONT_SHOW,
		ON_SHOW_DESCRIPTION,
		PROMPT,
		ALWAYS_SHOW
	}

	public static String getTooltipColorString(Minecraft mc, I18n i18n, Option<TooltipColor> option) {
		return getColorOption(option) + i18n.translateKey("options.tooltipcolor." + option.value.name().toLowerCase());
	}

	public static TextFormatting getColorOption(Option<TooltipColor> option) {
		return TextFormatting.getColorFormatting(option.value.name().toLowerCase().replace("_", ""));
	}

	@Environment(EnvType.CLIENT)
	public enum TooltipColor {
		WHITE,
		ORANGE,
		MAGENTA,
		LIGHT_BLUE,
		YELLOW,
		LIME,
		PINK,
		GRAY,
		LIGHT_GRAY,
		CYAN,
		PURPLE,
		BLUE,
		BROWN,
		GREEN,
		RED,
		BLACK
	}
}
