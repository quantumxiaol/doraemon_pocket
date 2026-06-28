package com.doraemon.pocket.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;

public record UndergroundSwimControl(int vertical, float forward, float sideways, boolean drill) {
	public static final UndergroundSwimControl IDLE = new UndergroundSwimControl(0, 0.0F, 0.0F, false);

	public static UndergroundSwimControl fromInput(boolean jumpPressed, boolean sneakPressed, boolean sprintPressed, float forward, float sideways) {
		int vertical = 0;
		if (jumpPressed) {
			vertical = 1;
		} else if (sneakPressed) {
			vertical = -1;
		}

		return new UndergroundSwimControl(
				vertical,
				sanitizeAxis(forward),
				sanitizeAxis(sideways),
				sprintPressed || jumpPressed
		);
	}

	public static UndergroundSwimControl read(PacketByteBuf buf) {
		int vertical = MathHelper.clamp(buf.readByte(), -1, 1);
		return new UndergroundSwimControl(vertical, sanitizeAxis(buf.readFloat()), sanitizeAxis(buf.readFloat()), buf.readBoolean());
	}

	public void write(PacketByteBuf buf) {
		buf.writeByte(vertical);
		buf.writeFloat(forward);
		buf.writeFloat(sideways);
		buf.writeBoolean(drill);
	}

	public boolean hasActiveInput() {
		return drill || vertical != 0 || Math.abs(forward) > 0.01F || Math.abs(sideways) > 0.01F;
	}

	public boolean isIdle() {
		return !hasActiveInput();
	}

	private static float sanitizeAxis(float value) {
		if (!Float.isFinite(value)) {
			return 0.0F;
		}
		return MathHelper.clamp(value, -1.0F, 1.0F);
	}
}
