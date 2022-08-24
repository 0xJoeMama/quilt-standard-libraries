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

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.injection.predicate.InjectionPredicate;
import org.quiltmc.qsl.component.impl.ComponentsImpl;

@ApiStatus.Experimental
public final class Components {
	// begin registry
	public static final RegistryKey<Registry<ComponentType<?>>> REGISTRY_KEY = ComponentsImpl.REGISTRY_KEY;
	public static final Registry<ComponentType<?>> REGISTRY = ComponentsImpl.REGISTRY;
	// end registry

	// begin injection methods
	public static void inject(InjectionPredicate predicate, ComponentEntry<?>... entries) {
		ComponentsImpl.inject(predicate, entries);
	}
	// end injection methods

	@Nullable
	public static <C, S> C expose(ComponentType<C> type, S obj) {
		if (obj instanceof ComponentProvider provider) {
			return provider.getComponentContainer().expose(type);
		}

		return null;
	}

	@Nullable
	public static <C, S> C ifPresent(S obj, ComponentType<C> type, Consumer<? super C> action) {
		if (obj instanceof ComponentProvider provider) {
			return provider.ifPresent(type, action);
		}

		return null;
	}

	// begin registration methods
	public static <C> ComponentType<C> register(Identifier id, ComponentType<C> type) {
		return ComponentsImpl.register(id, type);
	}
	// end registration method
}
