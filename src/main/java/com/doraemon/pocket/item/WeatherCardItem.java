package com.doraemon.pocket.item;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WeatherCardItem extends Item {
	private static final int WEATHER_DURATION_TICKS = 24000;

	private final WeatherMode mode;

	public WeatherCardItem(WeatherMode mode, Settings settings) {
		super(settings);
		this.mode = mode;
	}

	public void applyWeather(ServerWorld world) {
		switch (mode) {
			case CLEAR -> world.setWeather(WEATHER_DURATION_TICKS, 0, false, false);
			case RAIN -> world.setWeather(0, WEATHER_DURATION_TICKS, true, false);
			case THUNDER -> world.setWeather(0, WEATHER_DURATION_TICKS, true, true);
			case SNOW -> world.setWeather(0, WEATHER_DURATION_TICKS, true, false);
		}
	}

	public Text getAppliedText() {
		return Text.translatable("message.doraemon_pocket.weather_card." + mode.id());
	}

	public float getPitch() {
		return mode.pitch();
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.weather_card.tooltip").formatted(Formatting.GRAY));
		if (mode == WeatherMode.SNOW) {
			tooltip.add(Text.translatable("item.doraemon_pocket.weather_card.snow.tooltip").formatted(Formatting.DARK_AQUA));
		}
	}

	public enum WeatherMode {
		CLEAR("clear", 1.55F),
		RAIN("rain", 0.95F),
		THUNDER("thunder", 0.65F),
		SNOW("snow", 1.2F);

		private final String id;
		private final float pitch;

		WeatherMode(String id, float pitch) {
			this.id = id;
			this.pitch = pitch;
		}

		public String id() {
			return id;
		}

		public float pitch() {
			return pitch;
		}
	}
}
