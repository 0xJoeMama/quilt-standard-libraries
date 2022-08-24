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

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.component.api.sync.codec.NetworkCodec;

public record ComponentType<T>(Class<T> componentClass, boolean isInstant) {
	// the component type registry is synced, so we need not worry about desyncs
	public static final NetworkCodec<ComponentType<?>> NETWORK_CODEC = NetworkCodec.idIndexed(Components.REGISTRY);

	@SuppressWarnings("unchecked")
	public T cast(Object component) {
		return ((T) component);
	}

	@NotNull
	public Identifier id() {
		return Components.REGISTRY.getId(this);
	}
}
