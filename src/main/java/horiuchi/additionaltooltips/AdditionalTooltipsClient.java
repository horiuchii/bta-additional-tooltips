package horiuchi.additionaltooltips;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.options.components.*;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Option;
import net.minecraft.core.item.Items;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.util.ClientStartEntrypoint;
import turniplabs.halplibe.util.OptionsInitEntrypoint;

import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
public class AdditionalTooltipsClient implements ClientStartEntrypoint, OptionsInitEntrypoint {
	public static OptionsPage additionalTooltipOptions;
	public static final String MOD_ID = HalpLibe.registerMod("additionaltooltips", false);

	@Override
	public void beforeClientStart() {}

	@Override
	public void afterClientStart() {
		additionalTooltipOptions = new OptionsPage("additionaltooltips.options.title", Items.LABEL.getDefaultStack())
			.withComponent(new OptionsCategory("additionaltooltips.options.category.additionaltooltips")
				.withComponent(new KeyBindingComponent(AdditionalTooltipOptions.KEY_SHOW_ADDITIONAL_TOOLTIP))
				.withComponent(new ToggleableOptionComponent<>(AdditionalTooltipOptions.SHOW_FOOD))
				.withComponent(new BooleanOptionComponent(AdditionalTooltipOptions.SHOW_FOOD_REGEN_TIME))
				.withComponent(new ToggleableOptionComponent<>(AdditionalTooltipOptions.SHOW_ARMOR_PROTECTION))
				.withComponent(new ToggleableOptionComponent<>(AdditionalTooltipOptions.SHOW_DURABILITY))
				.withComponent(new ToggleableOptionComponent<>(AdditionalTooltipOptions.SHOW_FLAG_ART))
				.withComponent(new ToggleableOptionComponent<>(AdditionalTooltipOptions.FLAG_ART_SCALE))
				.withComponent(new ToggleableOptionComponent<>(AdditionalTooltipOptions.SHOW_MAP_ART))
				.withComponent(new ToggleableOptionComponent<>(AdditionalTooltipOptions.MAP_ART_SCALE))
			);
		OptionsPages.register(additionalTooltipOptions);
	}

	@Override
	public void initOptions() {
		for (Field field : AdditionalTooltipOptions.class.getDeclaredFields()) {
			try {
				Object o = field.get(null);
				if(o instanceof KeyBinding key){
					GameSettings.register(key);
				} else {
					GameSettings.register((Option<?>) o);
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
