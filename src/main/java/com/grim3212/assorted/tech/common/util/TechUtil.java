package com.grim3212.assorted.tech.common.util;

import net.minecraft.util.math.vector.Vector3i;

public class TechUtil {
	public static Vector3i multiply(Vector3i vec, int mul) {
		if (mul == 1) {
			return vec;
		} else {
			return mul == 0 ? Vector3i.ZERO : new Vector3i(vec.getX() * mul, vec.getY() * mul, vec.getZ() * mul);
		}
	}
}
