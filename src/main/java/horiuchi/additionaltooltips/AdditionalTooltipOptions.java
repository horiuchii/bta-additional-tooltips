package horiuchi.additionaltooltips;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.*;
import net.minecraft.core.util.helper.ITranslatable;
import org.lwjgl.input.Keyboard;

@Environment(EnvType.CLIENT)
public class AdditionalTooltipOptions {
	public static final KeyBinding KEY_SHOW_ADDITIONAL_TOOLTIP = new KeyBinding("key.additional.tooltip").setDefault(InputDevice.keyboard, Keyboard.KEY_LSHIFT);
	public static final OptionEnum<ShowTooltip> SHOW_FOOD = new OptionEnum<>("showFood", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION);
	public static final OptionBoolean SHOW_FOOD_REGEN_TIME = new OptionBoolean("showFoodRegenTime", true);
	public static final OptionEnum<ShowTooltip> SHOW_ARMOR_PROTECTION = new OptionEnum<>("showArmorProtection", ShowTooltip.class, ShowTooltip.PROMPT);
	public static final OptionEnum<ShowTooltip> SHOW_DURABILITY = new OptionEnum<>("showDurability", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION);
	public static final OptionEnum<ShowTooltip> SHOW_FLAG_ART = new OptionEnum<>("showFlagArt", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION);
	public static final OptionRange FLAG_ART_SCALE = new OptionRange("flagArtScale", 1, 3).withDisplayStringProvider((mc, i18n, option) -> (option.value + 1) + "x");
	public static final OptionEnum<ShowTooltip> SHOW_MAP_ART = new OptionEnum<>("showMapArt", ShowTooltip.class, ShowTooltip.ON_SHOW_DESCRIPTION);
	public static final OptionRange MAP_ART_SCALE = new OptionRange("mapArtScale", 1, 3).withDisplayStringProvider((mc, i18n, option) -> (option.value + 1) + "x");

	@Environment(EnvType.CLIENT)
	public enum ShowTooltip implements ITranslatable {
		DONT_SHOW,
		ON_SHOW_DESCRIPTION,
		PROMPT,
		ALWAYS_SHOW;

		public String getTranslationKey() {
			return this.name().toLowerCase();
		}
	}
}
