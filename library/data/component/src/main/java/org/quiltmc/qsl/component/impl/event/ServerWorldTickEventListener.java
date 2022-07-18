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

package org.quiltmc.qsl.component.impl.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.quiltmc.qsl.base.api.event.ListenerPhase;
import org.quiltmc.qsl.component.impl.CommonInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

@SuppressWarnings("unused")
@ListenerPhase(
		callbackTarget = ServerWorldTickEvents.End.class,
		namespace = CommonInitializer.MOD_ID, path = "tick_world_container"
)
public class ServerWorldTickEventListener implements ServerWorldTickEvents.End {
	@Override
	public void endWorldTick(MinecraftServer server, ServerWorld world) {
		world.getComponentContainer().tick(world);
	}
}