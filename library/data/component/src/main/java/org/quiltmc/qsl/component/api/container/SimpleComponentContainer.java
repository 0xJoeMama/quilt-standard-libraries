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

package org.quiltmc.qsl.component.api.container;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.components.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.util.StringConstants;

import java.util.*;
import java.util.stream.Stream;

public class SimpleComponentContainer implements ComponentContainer {
	private final Map<Identifier, Component> components;
	private final List<Identifier> nbtComponents;

	protected SimpleComponentContainer(@Nullable Runnable saveOperation, Stream<Identifier> componentIds) {
		this.components = new HashMap<>();
		this.nbtComponents = new ArrayList<>();

		componentIds.forEach(id -> {
			Component component = ComponentsImpl.getEntry(id).create();
			this.components.put(id, component);

			if (component instanceof NbtComponent<?> nbtComponent) {
				this.nbtComponents.add(id);
				nbtComponent.setSaveOperation(saveOperation);
			}
		});

		componentIds.close();
	}

	public static @NotNull SimpleComponentContainer create(Runnable saveOperation, ComponentIdentifier<?>... ids) {
		return new SimpleComponentContainer(saveOperation, Stream.of(ids).map(ComponentIdentifier::id));
	}

	@Override
	public Optional<Component> expose(Identifier id) {
		return Optional.ofNullable(this.components.get(id));
	}

	@Override
	public Map<Identifier, Component> exposeAll() {
		return this.components;
	}

	@Override
	public void moveComponents(ComponentContainer other) {
		throw new IllegalStateException("Cannot move components into a SimpleComponentContainer");
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = new NbtCompound();
		this.nbtComponents.forEach(id -> this.expose(id).ifPresent(component -> NbtComponent.writeTo(rootQslNbt, (NbtComponent<?>) component, id)));
		if (!rootQslNbt.isEmpty()) {
			providerRootNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		}
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = providerRootNbt.getCompound(StringConstants.COMPONENT_ROOT);

		if (rootQslNbt.isEmpty()) {
			return;
		}

		rootQslNbt.getKeys().stream()
				.map(Identifier::tryParse)
				.filter(Objects::nonNull)
				.forEach(id -> this.expose(id).ifPresent(component -> NbtComponent.readFrom((NbtComponent<?>) component, id, rootQslNbt)));
	}

	@Override
	public void setSaveOperation(@NotNull Runnable runnable) {
		throw new IllegalStateException("Cannot change the save operation of a SimpleComponentContainer since it needs to be passed into the constructor");
	}
}