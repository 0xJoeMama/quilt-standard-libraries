/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.component.test;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.level.LevelProperties;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.components.FloatComponent;
import org.quiltmc.qsl.component.api.components.IntegerComponent;
import org.quiltmc.qsl.component.api.components.InventoryComponent;

public class ComponentTestMod implements ModInitializer {
	public static final String MODID = "quilt_component_test";

	// Registration Code
	public static final ComponentType<InventoryComponent> COW_INVENTORY = InventoryComponent.of(
			() -> DefaultedList.ofSize(1, new ItemStack(Items.COBBLESTONE, 64)),
			new Identifier(MODID, "cow_inventory")
	);
	public static final ComponentType<IntegerComponent> CREEPER_EXPLODE_TIME =
			IntegerComponent.create(200, new Identifier(MODID, "creeper_explode_time"));
	public static final ComponentType<IntegerComponent> HOSTILE_EXPLODE_TIME =
			IntegerComponent.create(new Identifier(MODID, "hostile_explode_time"));
	public static final ComponentType<IntegerComponent> CHEST_NUMBER =
			IntegerComponent.create(200, new Identifier(MODID, "chest_number"));
	public static final ComponentType<InventoryComponent> CHUNK_INVENTORY = InventoryComponent.ofSize(1,
			new Identifier(MODID, "chunk_inventory")
	);
	public static final ComponentType<FloatComponent> SAVE_FLOAT =
			FloatComponent.create(new Identifier(MODID, "save_float"));

	// Attention do NOT place this block in any world because registry sync issues will make the game hung upon rejoining.
	public static final Block TEST_BLOCK = new TestBlock(AbstractBlock.Settings.copy(Blocks.STONE));
	public static final ComponentType<IntegerComponent> ITEMSTACK_INT =
			IntegerComponent.create(new Identifier(MODID, "itemstack_int"));

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.BLOCK, new Identifier(MODID, "test_block"), TEST_BLOCK);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MODID, "block_entity"), TEST_BE_TYPE);
		Block.STATE_IDS.add(TEST_BLOCK.getDefaultState());

		// Application Code
		Components.inject(CreeperEntity.class, CREEPER_EXPLODE_TIME);
		Components.injectInheritage(CowEntity.class, COW_INVENTORY);
		Components.injectInheritanceExcept(HostileEntity.class, HOSTILE_EXPLODE_TIME, CreeperEntity.class);
		Components.inject(ChestBlockEntity.class, CHEST_NUMBER);
		Components.injectInheritage(Chunk.class, CHUNK_INVENTORY);
		Components.inject(LevelProperties.class, SAVE_FLOAT);
		Components.inject(ItemStack.class, ITEMSTACK_INT);
//		Components.inject(new ItemStackInjectionPredicate(Items.BOOKSHELF), ITEMSTACK_INT);
	}

	public static final BlockEntityType<TestBlockEntity> TEST_BE_TYPE =
			BlockEntityType.Builder.create(TestBlockEntity::new, TEST_BLOCK, Blocks.NOTE_BLOCK).build(null);

}
