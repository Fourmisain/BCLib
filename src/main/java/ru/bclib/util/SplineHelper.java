package ru.bclib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import ru.bclib.sdf.SDF;
import ru.bclib.sdf.operator.SDFUnion;
import ru.bclib.sdf.primitive.SDFLine;

public class SplineHelper {
	public static List<Vector3f> makeSpline(float x1, float y1, float z1, float x2, float y2, float z2, int points) {
		List<Vector3f> spline = Lists.newArrayList();
		spline.add(new Vector3f(x1, y1, z1));
		int count = points - 1;
		for (int i = 1; i < count; i++) {
			float delta = (float) i / (float) count;
			float x = Mth.lerp(delta, x1, x2);
			float y = Mth.lerp(delta, y1, y2);
			float z = Mth.lerp(delta, z1, z2);
			spline.add(new Vector3f(x, y, z));
		}
		spline.add(new Vector3f(x2, y2, z2));
		return spline;
	}
	
	public static List<Vector3f> smoothSpline(List<Vector3f> spline, int segmentPoints) {
		List<Vector3f> result = Lists.newArrayList();
		Vector3f start = spline.get(0);
		for (int i = 1; i < spline.size(); i++) {
			Vector3f end = spline.get(i);
			for (int j = 0; j < segmentPoints; j++) {
				float delta = (float) j / segmentPoints;
				delta = 0.5F - 0.5F * Mth.cos(delta * 3.14159F);
				result.add(lerp(start, end, delta));
			}
			start = end;
		}
		result.add(start);
		return result;
	}
	
	private static Vector3f lerp(Vector3f start, Vector3f end, float delta) {
		float x = Mth.lerp(delta, start.x(), end.x());
		float y = Mth.lerp(delta, start.y(), end.y());
		float z = Mth.lerp(delta, start.z(), end.z());
		return new Vector3f(x, y, z);
	}
	
	public static void offsetParts(List<Vector3f> spline, Random random, float dx, float dy, float dz) {
		int count = spline.size();
		for (int i = 1; i < count; i++) {
			Vector3f pos = spline.get(i);
			float x = pos.x() + (float) random.nextGaussian() * dx;
			float y = pos.y() + (float) random.nextGaussian() * dy;
			float z = pos.z() + (float) random.nextGaussian() * dz;
			pos.set(x, y, z);
		}
	}
	
	public static void powerOffset(List<Vector3f> spline, float distance, float power) {
		int count = spline.size();
		float max = count + 1;
		for (int i = 1; i < count; i++) {
			Vector3f pos = spline.get(i);
			float x = (float) i / max;
			float y = pos.y() + (float) Math.pow(x, power) * distance;
			pos.set(pos.x(), y, pos.z());
		}
	}
	
	public static SDF buildSDF(List<Vector3f> spline, float radius1, float radius2, Function<BlockPos, BlockState> placerFunction) {
		int count = spline.size();
		float max = count - 2;
		SDF result = null;
		Vector3f start = spline.get(0);
		for (int i = 1; i < count; i++) {
			Vector3f pos = spline.get(i);
			float delta = (float) (i - 1) / max;
			SDF line = new SDFLine()
					.setRadius(Mth.lerp(delta, radius1, radius2))
					.setStart(start.x(), start.y(), start.z())
					.setEnd(pos.x(), pos.y(), pos.z())
					.setBlock(placerFunction);
			result = result == null ? line : new SDFUnion().setSourceA(result).setSourceB(line);
			start = pos;
		}
		return result;
	}
	
	public static SDF buildSDF(List<Vector3f> spline, Function<Float, Float> radiusFunction, Function<BlockPos, BlockState> placerFunction) {
		int count = spline.size();
		float max = count - 2;
		SDF result = null;
		Vector3f start = spline.get(0);
		for (int i = 1; i < count; i++) {
			Vector3f pos = spline.get(i);
			float delta = (float) (i - 1) / max;
			SDF line = new SDFLine()
					.setRadius(radiusFunction.apply(delta))
					.setStart(start.x(), start.y(), start.z())
					.setEnd(pos.x(), pos.y(), pos.z())
					.setBlock(placerFunction);
			result = result == null ? line : new SDFUnion().setSourceA(result).setSourceB(line);
			start = pos;
		}
		return result;
	}
	
	public static boolean fillSpline(List<Vector3f> spline, WorldGenLevel world, BlockState state, BlockPos pos, Function<BlockState, Boolean> replace) {
		Vector3f startPos = spline.get(0);
		for (int i = 1; i < spline.size(); i++) {
			Vector3f endPos = spline.get(i);
			if (!(fillLine(startPos, endPos, world, state, pos, replace))) {
				return false;
			}
			startPos = endPos;
		}
		
		return true;
	}
	
	public static void fillSplineForce(List<Vector3f> spline, WorldGenLevel world, BlockState state, BlockPos pos, Function<BlockState, Boolean> replace) {
		Vector3f startPos = spline.get(0);
		for (int i = 1; i < spline.size(); i++) {
			Vector3f endPos = spline.get(i);
			fillLineForce(startPos, endPos, world, state, pos, replace);
			startPos = endPos;
		}
	}
	
	public static boolean fillLine(Vector3f start, Vector3f end, WorldGenLevel world, BlockState state, BlockPos pos, Function<BlockState, Boolean> replace) {
		float dx = end.x() - start.x();
		float dy = end.y() - start.y();
		float dz = end.z() - start.z();
		float max = MHelper.max(Math.abs(dx), Math.abs(dy), Math.abs(dz));
		int count = MHelper.floor(max + 1);
		dx /= max;
		dy /= max;
		dz /= max;
		float x = start.x();
		float y = start.y();
		float z = start.z();
		boolean down = Math.abs(dy) > 0.2;
		
		BlockState bState;
		MutableBlockPos bPos = new MutableBlockPos();
		for (int i = 0; i < count; i++) {
			bPos.set(x + pos.getX(), y + pos.getY(), z + pos.getZ());
			bState = world.getBlockState(bPos);
			if (bState.equals(state) || replace.apply(bState)) {
				BlocksHelper.setWithoutUpdate(world, bPos, state);
				bPos.setY(bPos.getY() - 1);
				bState = world.getBlockState(bPos);
				if (down && bState.equals(state) || replace.apply(bState)) {
					BlocksHelper.setWithoutUpdate(world, bPos, state);
				}
			}
			else {
				return false;
			}
			x += dx;
			y += dy;
			z += dz;
		}
		bPos.set(end.x() + pos.getX(), end.y() + pos.getY(), end.z() + pos.getZ());
		bState = world.getBlockState(bPos);
		if (bState.equals(state) || replace.apply(bState)) {
			BlocksHelper.setWithoutUpdate(world, bPos, state);
			bPos.setY(bPos.getY() - 1);
			bState = world.getBlockState(bPos);
			if (down && bState.equals(state) || replace.apply(bState)) {
				BlocksHelper.setWithoutUpdate(world, bPos, state);
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void fillLineForce(Vector3f start, Vector3f end, WorldGenLevel world, BlockState state, BlockPos pos, Function<BlockState, Boolean> replace) {
		float dx = end.x() - start.x();
		float dy = end.y() - start.y();
		float dz = end.z() - start.z();
		float max = MHelper.max(Math.abs(dx), Math.abs(dy), Math.abs(dz));
		int count = MHelper.floor(max + 1);
		dx /= max;
		dy /= max;
		dz /= max;
		float x = start.x();
		float y = start.y();
		float z = start.z();
		boolean down = Math.abs(dy) > 0.2;
		
		BlockState bState;
		MutableBlockPos bPos = new MutableBlockPos();
		for (int i = 0; i < count; i++) {
			bPos.set(x + pos.getX(), y + pos.getY(), z + pos.getZ());
			bState = world.getBlockState(bPos);
			if (replace.apply(bState)) {
				BlocksHelper.setWithoutUpdate(world, bPos, state);
				bPos.setY(bPos.getY() - 1);
				bState = world.getBlockState(bPos);
				if (down && replace.apply(bState)) {
					BlocksHelper.setWithoutUpdate(world, bPos, state);
				}
			}
			x += dx;
			y += dy;
			z += dz;
		}
		bPos.set(end.x() + pos.getX(), end.y() + pos.getY(), end.z() + pos.getZ());
		bState = world.getBlockState(bPos);
		if (replace.apply(bState)) {
			BlocksHelper.setWithoutUpdate(world, bPos, state);
			bPos.setY(bPos.getY() - 1);
			bState = world.getBlockState(bPos);
			if (down && replace.apply(bState)) {
				BlocksHelper.setWithoutUpdate(world, bPos, state);
			}
		}
	}
	
	public static boolean canGenerate(List<Vector3f> spline, float scale, BlockPos start, WorldGenLevel world, Function<BlockState, Boolean> canReplace) {
		int count = spline.size();
		Vector3f vec = spline.get(0);
		MutableBlockPos mut = new MutableBlockPos();
		float x1 = start.getX() + vec.x() * scale;
		float y1 = start.getY() + vec.y() * scale;
		float z1 = start.getZ() + vec.z() * scale;
		for (int i = 1; i < count; i++) {
			vec = spline.get(i);
			float x2 = start.getX() + vec.x() * scale;
			float y2 = start.getY() + vec.y() * scale;
			float z2 = start.getZ() + vec.z() * scale;
			
			for (float py = y1; py < y2; py += 3) {
				if (py - start.getY() < 10) continue;
				float lerp = (py - y1) / (y2 - y1);
				float x = Mth.lerp(lerp, x1, x2);
				float z = Mth.lerp(lerp, z1, z2);
				mut.set(x, py, z);
				if (!canReplace.apply(world.getBlockState(mut))) {
					return false;
				}
			}
			
			x1 = x2;
			y1 = y2;
			z1 = z2;
		}
		return true;
	}
	
	public static boolean canGenerate(List<Vector3f> spline, BlockPos start, WorldGenLevel world, Function<BlockState, Boolean> canReplace) {
		int count = spline.size();
		Vector3f vec = spline.get(0);
		MutableBlockPos mut = new MutableBlockPos();
		float x1 = start.getX() + vec.x();
		float y1 = start.getY() + vec.y();
		float z1 = start.getZ() + vec.z();
		for (int i = 1; i < count; i++) {
			vec = spline.get(i);
			float x2 = start.getX() + vec.x();
			float y2 = start.getY() + vec.y();
			float z2 = start.getZ() + vec.z();
			
			for (float py = y1; py < y2; py += 3) {
				if (py - start.getY() < 10) continue;
				float lerp = (py - y1) / (y2 - y1);
				float x = Mth.lerp(lerp, x1, x2);
				float z = Mth.lerp(lerp, z1, z2);
				mut.set(x, py, z);
				if (!canReplace.apply(world.getBlockState(mut))) {
					return false;
				}
			}
			
			x1 = x2;
			y1 = y2;
			z1 = z2;
		}
		return true;
	}
	
	public static Vector3f getPos(List<Vector3f> spline, float index) {
		int i = (int) index;
		int last = spline.size() - 1;
		if (i >= last) {
			return spline.get(last);
		}
		float delta = index - i;
		Vector3f p1 = spline.get(i);
		Vector3f p2 = spline.get(i + 1);
		float x = Mth.lerp(delta, p1.x(), p2.x());
		float y = Mth.lerp(delta, p1.y(), p2.y());
		float z = Mth.lerp(delta, p1.z(), p2.z());
		return new Vector3f(x, y, z);
	}
	
	public static void rotateSpline(List<Vector3f> spline, float angle) {
		for (Vector3f v: spline) {
			float sin = (float) Math.sin(angle);
			float cos = (float) Math.cos(angle);
			float x = v.x() * cos + v.z() * sin;
			float z = v.x() * sin + v.z() * cos;
			v.set(x, v.y(), z);
		}
	}
	
	public static List<Vector3f> copySpline(List<Vector3f> spline) {
		List<Vector3f> result = new ArrayList<Vector3f>(spline.size());
		for (Vector3f v: spline) {
			result.add(new Vector3f(v.x(), v.y(), v.z()));
		}
		return result;
	}
	
	public static void scale(List<Vector3f> spline, float scale) {
		scale(spline, scale, scale, scale);
	}
	
	public static void scale(List<Vector3f> spline, float x, float y, float z) {
		for (Vector3f v: spline) {
			v.set(v.x() * x, v.y() * y, v.z() * z);
		}
	}
	
	public static void offset(List<Vector3f> spline, Vector3f offset) {
		for (Vector3f v: spline) {
			v.set(offset.x() + v.x(), offset.y() + v.y(), offset.z() + v.z());
		}
	}
}
