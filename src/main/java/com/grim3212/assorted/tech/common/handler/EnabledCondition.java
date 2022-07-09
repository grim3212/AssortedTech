package com.grim3212.assorted.tech.common.handler;

import com.google.gson.JsonObject;
import com.grim3212.assorted.tech.AssortedTech;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class EnabledCondition implements ICondition {

	private static final ResourceLocation NAME = new ResourceLocation(AssortedTech.MODID, "part_enabled");
	private final String part;

	public static final String TORCHES_CONDITION = "torches";
	public static final String SPIKES_CONDITION = "spikes";
	public static final String SENSORS_CONDITION = "sensors";
	public static final String FAN_CONDITION = "fan";
	public static final String ALARM_CONDITION = "alarm";
	public static final String BRIDGES_CONDITION = "bridges";
	public static final String GRAVITY_CONDITION = "gravity";

	public EnabledCondition(String part) {
		this.part = part;
	}

	@Override
	public ResourceLocation getID() {
		return NAME;
	}

	@Override
	public boolean test(IContext context) {
		switch (part) {
		case TORCHES_CONDITION:
			return TechConfig.COMMON.torchesEnabled.get();
		case SPIKES_CONDITION:
			return TechConfig.COMMON.spikesEnabled.get();
		case SENSORS_CONDITION:
			return TechConfig.COMMON.sensorsEnabled.get();
		case FAN_CONDITION:
			return TechConfig.COMMON.fanEnabled.get();
		case ALARM_CONDITION:
			return TechConfig.COMMON.alarmEnabled.get();
		case BRIDGES_CONDITION:
			return TechConfig.COMMON.bridgesEnabled.get();
		case GRAVITY_CONDITION:
			return TechConfig.COMMON.gravityEnabled.get();
		default:
			return false;
		}
	}

	public static class Serializer implements IConditionSerializer<EnabledCondition> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public void write(JsonObject json, EnabledCondition value) {
			json.addProperty("part", value.part);
		}

		@Override
		public EnabledCondition read(JsonObject json) {
			return new EnabledCondition(GsonHelper.getAsString(json, "part"));
		}

		@Override
		public ResourceLocation getID() {
			return EnabledCondition.NAME;
		}
	}
}
