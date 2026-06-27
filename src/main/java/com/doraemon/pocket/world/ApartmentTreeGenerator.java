package com.doraemon.pocket.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;

public final class ApartmentTreeGenerator {
	private static final int MIN_SHAFT_DEPTH = 11;
	private static final int MAX_SHAFT_DEPTH = 17;
	private static final int ROOM_HEIGHT = 3;
	private static final int MAX_BRANCH_DEPTH = 4;
	private static final int MAX_ROOMS = 13;
	private static final BlockState AIR = Blocks.AIR.getDefaultState();
	private static final BlockState FLOOR = Blocks.BIRCH_PLANKS.getDefaultState();
	private static final BlockState WALL = Blocks.OAK_PLANKS.getDefaultState();
	private static final BlockState ROOT = Blocks.STRIPPED_OAK_WOOD.getDefaultState();
	private static final BlockState LANTERN = Blocks.LANTERN.getDefaultState();

	private ApartmentTreeGenerator() {
	}

	public static boolean canGenerate(WorldView world, BlockPos saplingPos) {
		return saplingPos.getY() - MAX_SHAFT_DEPTH - ROOM_HEIGHT - 3 > world.getBottomY();
	}

	public static boolean generate(ServerWorld world, BlockPos saplingPos, Random random) {
		if (!canGenerate(world, saplingPos)) {
			return false;
		}

		int shaftSize = random.nextBoolean() ? 3 : 4;
		int shaftDepth = MIN_SHAFT_DEPTH + random.nextInt(MAX_SHAFT_DEPTH - MIN_SHAFT_DEPTH + 1);
		int hubFloorY = saplingPos.getY() - shaftDepth;

		set(world, saplingPos, AIR);

		Room hub = new Room(new BlockPos(saplingPos.getX(), hubFloorY, saplingPos.getZ()), 6, 6);
		generateRoom(world, hub);
		decorateRoomRoots(world, hub, random);
		GrowthState growth = new GrowthState(1);
		branchFrom(world, hub, 0, random, null, growth);
		reopenPassages(world, growth);
		generateShaft(world, saplingPos, hubFloorY, shaftSize);
		openShaftDoorway(world, saplingPos, hubFloorY, shaftSize);

		world.playSound(null, saplingPos, SoundEvents.BLOCK_ROOTED_DIRT_BREAK, SoundCategory.BLOCKS, 1.2F, 0.55F);
		world.playSound(null, saplingPos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
		return true;
	}

	private static void generateShaft(ServerWorld world, BlockPos center, int floorY, int size) {
		int minX = center.getX() - size / 2;
		int minZ = center.getZ() - size / 2;
		int maxX = minX + size - 1;
		int maxZ = minZ + size - 1;
		int ladderX = center.getX();
		int ladderZ = minZ;

		for (int y = floorY + 1; y <= center.getY(); y++) {
			for (int x = minX - 1; x <= maxX + 1; x++) {
				for (int z = minZ - 1; z <= maxZ + 1; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					boolean wall = x == minX - 1 || x == maxX + 1 || z == minZ - 1 || z == maxZ + 1;
					set(world, pos, wall ? ROOT : AIR);
				}
			}
			set(world, new BlockPos(ladderX, y, ladderZ), Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH));
		}
	}

	private static void openShaftDoorway(ServerWorld world, BlockPos center, int floorY, int size) {
		int minX = center.getX() - size / 2;
		int minZ = center.getZ() - size / 2;
		int maxX = minX + size - 1;
		int maxZ = minZ + size - 1;
		for (int y = floorY + 1; y <= floorY + 2; y++) {
			for (int x = minX; x <= maxX; x++) {
				set(world, new BlockPos(x, y, maxZ + 1), AIR);
			}
			for (int z = minZ; z <= maxZ; z++) {
				set(world, new BlockPos(center.getX(), y, z), AIR);
			}
		}
	}

	private static void branchFrom(ServerWorld world, Room source, int depth, Random random, Direction incoming, GrowthState growth) {
		if (depth >= MAX_BRANCH_DEPTH || growth.rooms() >= MAX_ROOMS) {
			return;
		}

		List<Direction> directions = new ArrayList<>(Direction.Type.HORIZONTAL.stream().toList());
		Collections.shuffle(directions, new java.util.Random(random.nextLong()));

		int branches = switch (depth) {
			case 0 -> 4;
			case 1 -> 2 + random.nextInt(2);
			case 2 -> 1 + random.nextInt(2);
			default -> random.nextFloat() < 0.55F ? 1 : 0;
		};
		int made = 0;
		for (Direction direction : directions) {
			if (incoming != null && direction == incoming.getOpposite()) {
				continue;
			}
			if (made >= branches || growth.rooms() >= MAX_ROOMS) {
				return;
			}

			int roomWidth = 3 + random.nextInt(4);
			int roomLength = 3 + random.nextInt(4);
			RootPath path = createRootPath(source, direction, depth, random);
			Room room = createRoomAtPathEnd(source, path, roomWidth, roomLength, random);

			generateRoom(world, room);
			decorateRoomRoots(world, room, random);
			generateRootCorridor(world, path, random, growth);
			if (random.nextFloat() < 0.75F) {
				generateRootTendril(world, path, random, growth);
			}
			growth.addRoom();
			branchFrom(world, room, depth + 1, random, path.exitDirection(), growth);
			made++;
		}
	}

	private static RootPath createRootPath(Room source, Direction direction, int depth, Random random) {
		List<Segment> segments = new ArrayList<>();
		BlockPos cursor = source.wallCenter(direction);
		int firstLength = 4 + random.nextInt(5);
		segments.add(new Segment(cursor, direction, firstLength, source.floorY()));
		cursor = cursor.offset(direction, firstLength);

		Direction exit = direction;
		boolean shouldTurn = depth > 0 ? random.nextFloat() < 0.7F : random.nextFloat() < 0.45F;
		if (shouldTurn) {
			Direction turn = turn(direction, random.nextBoolean());
			int turnLength = 3 + random.nextInt(5);
			BlockPos turnStart = cursor.offset(turn);
			segments.add(new Segment(turnStart, turn, turnLength, source.floorY()));
			cursor = turnStart.offset(turn, turnLength);
			exit = turn;
		}

		int finalLength = 2 + random.nextInt(4);
		BlockPos finalStart = cursor.offset(exit);
		segments.add(new Segment(finalStart, exit, finalLength, source.floorY()));
		cursor = finalStart.offset(exit, finalLength);
		return new RootPath(segments, exit, cursor);
	}

	private static Room createRoomAtPathEnd(Room source, RootPath path, int roomWidth, int roomLength, Random random) {
		Direction exit = path.exitDirection();
		int along = exit.getAxis() == Direction.Axis.X ? roomWidth : roomLength;
		BlockPos center = path.end().offset(exit, 1 + along / 2);
		int floorY = source.floorY();
		if (random.nextFloat() < 0.35F) {
			floorY -= 1;
		}
		return new Room(new BlockPos(center.getX(), floorY, center.getZ()), roomWidth, roomLength);
	}

	private static void generateRoom(ServerWorld world, Room room) {
		for (int y = room.floorY(); y <= room.ceilingY(); y++) {
			for (int x = room.minX() - 1; x <= room.maxX() + 1; x++) {
				for (int z = room.minZ() - 1; z <= room.maxZ() + 1; z++) {
					boolean floor = y == room.floorY();
					boolean ceiling = y == room.ceilingY();
					boolean wall = x == room.minX() - 1 || x == room.maxX() + 1 || z == room.minZ() - 1 || z == room.maxZ() + 1;
					BlockState state = floor ? FLOOR : (ceiling || wall ? WALL : AIR);
					set(world, new BlockPos(x, y, z), state);
				}
			}
		}

		set(world, room.center().up(1), LANTERN);
		set(world, room.center().add(room.width() / 2, 1, room.length() / 2), Blocks.TORCH.getDefaultState());
	}

	private static void decorateRoomRoots(ServerWorld world, Room room, Random random) {
		for (int y = room.floorY() + 1; y <= room.ceilingY(); y++) {
			for (int x = room.minX() - 1; x <= room.maxX() + 1; x++) {
				placeRootPatch(world, random, new BlockPos(x, y, room.minZ() - 1));
				placeRootPatch(world, random, new BlockPos(x, y, room.maxZ() + 1));
			}
			for (int z = room.minZ() - 1; z <= room.maxZ() + 1; z++) {
				placeRootPatch(world, random, new BlockPos(room.minX() - 1, y, z));
				placeRootPatch(world, random, new BlockPos(room.maxX() + 1, y, z));
			}
		}
	}

	private static void placeRootPatch(ServerWorld world, Random random, BlockPos pos) {
		if (random.nextFloat() < 0.18F) {
			set(world, pos, ROOT);
		}
	}

	private static void generateRootCorridor(ServerWorld world, RootPath path, Random random, GrowthState growth) {
		int width = random.nextFloat() < 0.35F ? 2 : 1;
		int height = random.nextFloat() < 0.45F ? 3 : 2;
		for (Segment segment : path.segments()) {
			for (int i = 0; i <= segment.length(); i++) {
				BlockPos center = segment.start().offset(segment.direction(), i).withY(segment.floorY());
				carveCorridorCell(world, center, segment.direction(), width, height, random, growth);
			}
		}
	}

	private static void carveCorridorCell(ServerWorld world, BlockPos center, Direction direction, int width, int height, Random random, GrowthState growth) {
		Direction lateral = direction.rotateYClockwise();
		int minSide = width == 1 ? 0 : -1;
		int maxSide = 0;
		for (int side = minSide - 1; side <= maxSide + 1; side++) {
			for (int y = center.getY(); y <= center.getY() + height + 1; y++) {
				BlockPos pos = center.offset(lateral, side).withY(y);
				boolean interiorSide = side >= minSide && side <= maxSide;
				boolean floor = y == center.getY();
				boolean ceiling = y == center.getY() + height + 1;
				boolean wall = !interiorSide;
				BlockState state = floor && interiorSide ? FLOOR : (ceiling || wall ? rootOrWall(random) : AIR);
				set(world, pos, state);
				if (interiorSide && floor) {
					growth.addPassage(pos);
				}
			}
		}
	}

	private static void generateRootTendril(ServerWorld world, RootPath path, Random random, GrowthState growth) {
		List<Segment> segments = path.segments();
		Segment base = segments.get(random.nextInt(segments.size()));
		Direction direction = turn(base.direction(), random.nextBoolean());
		BlockPos start = base.start().offset(base.direction(), Math.max(1, random.nextInt(Math.max(2, base.length()))));
		int length = 2 + random.nextInt(5);
		for (int i = 0; i <= length; i++) {
			BlockPos center = start.offset(direction, i).withY(base.floorY());
			carveCorridorCell(world, center, direction, 1, 2, random, growth);
		}
		BlockPos cap = start.offset(direction, length + 1).withY(base.floorY() + 1);
		set(world, cap, ROOT);
	}

	private static void reopenPassages(ServerWorld world, GrowthState growth) {
		for (BlockPos floor : growth.passages()) {
			set(world, floor, FLOOR);
			set(world, floor.up(), AIR);
			set(world, floor.up(2), AIR);
			set(world, floor.up(3), AIR);
		}
	}

	private static BlockState rootOrWall(Random random) {
		return random.nextFloat() < 0.68F ? ROOT : WALL;
	}

	private static Direction turn(Direction direction, boolean clockwise) {
		return clockwise ? direction.rotateYClockwise() : direction.rotateYClockwise().getOpposite();
	}

	private static void set(ServerWorld world, BlockPos pos, BlockState state) {
		if (pos.getY() <= world.getBottomY() || pos.getY() >= world.getTopY()) {
			return;
		}
		world.setBlockState(pos, state, Block.NOTIFY_ALL);
	}

	private record Room(BlockPos center, int width, int length) {
		int floorY() {
			return center.getY();
		}

		int ceilingY() {
			return floorY() + ROOM_HEIGHT + 1;
		}

		int minX() {
			return center.getX() - width / 2;
		}

		int maxX() {
			return minX() + width - 1;
		}

		int minZ() {
			return center.getZ() - length / 2;
		}

		int maxZ() {
			return minZ() + length - 1;
		}

		BlockPos wallCenter(Direction direction) {
			return switch (direction) {
				case NORTH -> new BlockPos(center.getX(), floorY(), minZ() - 1);
				case SOUTH -> new BlockPos(center.getX(), floorY(), maxZ() + 1);
				case WEST -> new BlockPos(minX() - 1, floorY(), center.getZ());
				case EAST -> new BlockPos(maxX() + 1, floorY(), center.getZ());
				default -> center;
			};
		}
	}

	private record Segment(BlockPos start, Direction direction, int length, int floorY) {
	}

	private record RootPath(List<Segment> segments, Direction exitDirection, BlockPos end) {
	}

	private static final class GrowthState {
		private int rooms;
		private final List<BlockPos> passages = new ArrayList<>();

		private GrowthState(int rooms) {
			this.rooms = rooms;
		}

		private int rooms() {
			return rooms;
		}

		private void addRoom() {
			rooms++;
		}

		private void addPassage(BlockPos pos) {
			passages.add(pos.toImmutable());
		}

		private List<BlockPos> passages() {
			return passages;
		}
	}
}
