package com.doraemon.pocket.block.entity;

import com.doraemon.pocket.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class LinkedPortalBlockEntity extends BlockEntity {
	public static final int ANYWHERE_DOOR_HEIGHT = 2;
	public static final int PASS_LOOP_HEIGHT = 1;
	public static final String KIND_ANYWHERE_DOOR = "anywhere_door";
	public static final String KIND_PASS_LOOP = "pass_loop";

	private String kind = KIND_ANYWHERE_DOOR;
	private BlockPos rootPos = BlockPos.ORIGIN;
	private String partnerWorld = "";
	private BlockPos partnerRoot = BlockPos.ORIGIN;
	private Direction facing = Direction.NORTH;
	private int height = PASS_LOOP_HEIGHT;

	public LinkedPortalBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.LINKED_PORTAL, pos, state);
		this.rootPos = pos;
	}

	public void configure(String kind, BlockPos rootPos, RegistryKey<World> partnerWorld, BlockPos partnerRoot, Direction facing, int height) {
		this.kind = kind;
		this.rootPos = rootPos.toImmutable();
		this.partnerWorld = partnerWorld.getValue().toString();
		this.partnerRoot = partnerRoot.toImmutable();
		this.facing = facing;
		this.height = height;
		markDirty();
	}

	public String getKind() {
		return kind;
	}

	public BlockPos getRootPos() {
		return rootPos;
	}

	public String getPartnerWorldId() {
		return partnerWorld;
	}

	public RegistryKey<World> getPartnerWorldKey() {
		Identifier id = Identifier.tryParse(partnerWorld);
		if (id == null) {
			return null;
		}
		return RegistryKey.of(RegistryKeys.WORLD, id);
	}

	public BlockPos getPartnerRoot() {
		return partnerRoot;
	}

	public Direction getFacing() {
		return facing;
	}

	public int getHeight() {
		return Math.max(PASS_LOOP_HEIGHT, Math.min(ANYWHERE_DOOR_HEIGHT, height));
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		kind = nbt.contains("Kind") ? nbt.getString("Kind") : KIND_ANYWHERE_DOOR;
		rootPos = nbt.contains("Root") ? BlockPos.fromLong(nbt.getLong("Root")) : pos;
		partnerWorld = nbt.contains("PartnerWorld") ? nbt.getString("PartnerWorld") : "";
		partnerRoot = nbt.contains("PartnerRoot") ? BlockPos.fromLong(nbt.getLong("PartnerRoot")) : BlockPos.ORIGIN;
		Direction savedFacing = nbt.contains("Facing") ? Direction.byName(nbt.getString("Facing")) : null;
		facing = savedFacing == null ? Direction.NORTH : savedFacing;
		height = nbt.contains("Height") ? nbt.getInt("Height") : PASS_LOOP_HEIGHT;
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putString("Kind", kind);
		nbt.putLong("Root", rootPos.asLong());
		nbt.putString("PartnerWorld", partnerWorld);
		nbt.putLong("PartnerRoot", partnerRoot.asLong());
		nbt.putString("Facing", facing.asString());
		nbt.putInt("Height", height);
	}
}
