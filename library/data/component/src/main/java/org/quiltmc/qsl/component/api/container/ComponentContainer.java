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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;

import org.quiltmc.qsl.component.api.ComponentFactory;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.ComponentsImpl;

public interface ComponentContainer {
	ComponentContainer EMPTY = EmptyComponentContainer.INSTANCE;
	ComponentContainer.Factory<EmptyComponentContainer> EMPTY_FACTORY = EmptyComponentContainer.FACTORY;
	ComponentContainer.Factory<SimpleComponentContainer> SIMPLE_FACTORY = SimpleComponentContainer.FACTORY;
	ComponentContainer.Factory<LazyComponentContainer> LAZY_FACTORY = LazyComponentContainer.FACTORY;

	static <C> ComponentContainer.Factory<SingleComponentContainer<C>> createSingleFactory(ComponentType<C> type) {
		return SingleComponentContainer.createFactory(new ComponentEntry<>(type));
	}

	static <C> ComponentContainer.Factory<SingleComponentContainer<C>> createSingleFactory(ComponentType<C> type,
			ComponentFactory<C> factory) {
		return SingleComponentContainer.createFactory(new ComponentEntry<>(type, factory));
	}

	static ComponentContainer createComposite(ComponentContainer main, ComponentContainer fallback) {
		return new CompositeComponentContainer(main, fallback);
	}

	static Builder builder(Object obj) {
		// TODO: Is there a way to avoid this instanceof check?
		if (!(obj instanceof ComponentProvider provider)) {
			throw new UnsupportedOperationException("Cannot create a container for a non-provider object");
		}

		return new Builder(provider);
	}

	@Nullable <C> C expose(ComponentType<C> type);

	ComponentProvider getProvider();

	void writeNbt(NbtCompound providerRootNbt);

	void readNbt(NbtCompound providerRootNbt);

	void tick();

	void sync();

	void forEach(BiConsumer<ComponentType<?>, ? super Object> action);

	@FunctionalInterface
	interface Factory<T extends ComponentContainer> {
		T generate(ComponentProvider provider,
				List<ComponentEntry<?>> entries,
				@Nullable Runnable saveOperation,
				boolean ticking,
				@Nullable SyncChannel<?, ?> syncChannel
		);
	}

	class Builder {
		private final ComponentProvider provider;
		private final List<ComponentEntry<?>> entries;
		private boolean ticking;
		@Nullable
		private Runnable saveOperation;
		@Nullable
		private SyncChannel<?, ?> syncChannel;
		private boolean acceptsInjections;

		private Builder(ComponentProvider provider) {
			this.provider = provider;
			this.entries = new ArrayList<>();
			this.acceptsInjections = false;
			this.saveOperation = null;
			this.syncChannel = null;
		}

		public Builder saving(Runnable saveOperation) {
			this.saveOperation = saveOperation;
			return this;
		}

		public Builder ticking() {
			this.ticking = true;
			return this;
		}

		public Builder syncing(SyncChannel<?, ?> syncChannel) {
			this.syncChannel = syncChannel;
			return this;
		}

		public <C> Builder add(ComponentEntry<C> componentEntry) {
			this.entries.add(componentEntry);
			return this;
		}

		public <C> Builder add(ComponentType<C> type) {
			this.add(new ComponentEntry<>(type));
			return this;
		}

		public <C> Builder add(ComponentType<C> type, ComponentFactory<C> factory) {
			this.add(new ComponentEntry<>(type, factory));
			return this;
		}

		public Builder add(ComponentType<?>... types) {
			for (var type : types) {
				this.add(type);
			}

			return this;
		}

		public Builder acceptsInjections() {
			this.acceptsInjections = true;
			return this;
		}

		public <T extends ComponentContainer> T build(ComponentContainer.Factory<T> factory) {
			// TODO: See if we can cache the builder at some stage to reduce object creation.
			if (this.acceptsInjections) {
				this.entries.addAll(ComponentsImpl.getInjections(this.provider));
			}

			return factory.generate(this.provider, this.entries, this.saveOperation, this.ticking, this.syncChannel);
		}
	}
}
