package com.doraemon.pocket.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;

public record BambooCopterControl(int vertical, float forward, float sideways) {
	public static final BambooCopterControl IDLE = new BambooCopterControl(0, 0.0F, 0.0F);

	public static BambooCopterControl fromInput(boolean jumpPressed, boolean sneakPressed, float forward, float sideways) {
		int vertical = 0;

		if (jumpPressed) {
			vertical = 1;
		} else if (sneakPressed) {
			vertical = -1;
		}

		return new BambooCopterControl(
				vertical,
				sanitizeAxis(forward),
				sanitizeAxis(sideways)
		);
	}

	public static BambooCopterControl read(PacketByteBuf buf) {
		int vertical = MathHelper.clamp(buf.readByte(), -1, 1);
		return new BambooCopterControl(vertical, sanitizeAxis(buf.readFloat()), sanitizeAxis(buf.readFloat()));
	}

	public void write(PacketByteBuf buf) {
		buf.writeByte(vertical);
		buf.writeFloat(forward);
		buf.writeFloat(sideways);
	}

	public boolean hasActiveInput() {
		return vertical != 0 || Math.abs(forward) > 0.01F || Math.abs(sideways) > 0.01F;
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
