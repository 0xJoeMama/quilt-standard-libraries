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

package org.quiltmc.qsl.component.api;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public interface ComponentContainer {
	Optional<Component> expose(Identifier id);

	Map<Identifier, Component> exposeAll();

	void moveComponents(ComponentContainer other);

	void writeNbt(NbtCompound providerRootNbt);

	void readNbt(NbtCompound providerRootNbt);

	void setSaveOperation(@NotNull Runnable runnable);
}