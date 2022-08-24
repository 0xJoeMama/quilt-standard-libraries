package org.quiltmc.qsl.registry.test;

import com.mojang.serialization.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.builder.QuiltRegistryBuilder;
import org.quiltmc.qsl.registry.api.builder.RegistryBuilderModule;

public class RegistryLibBuilderTest implements ModInitializer {
	public record Test(String name) { }

	public static final Logger LOGGER = LoggerFactory.getLogger(RegistryLibBuilderTest.class);
	public static final Registry<Test> TESTS = QuiltRegistryBuilder.<Test>create()
			.addModule(RegistryBuilderModule.synchronizing())
		   	.addModule(RegistryBuilderModule.onAdded(context -> LOGGER.info("{} was just registered!", context.id())))
			.addModule(RegistryBuilderModule.customFactory((key, lifecycle, holderProvider, modules) ->
				new DefaultedRegistry<>("quilt:test0", key, lifecycle, holderProvider)))
		   	.lifecycle(Lifecycle.stable())
		   	.build(new Identifier("quilt_registry_test", "test_items"));

	@Override
	public void onInitialize(ModContainer mod) {
		Test test0 = new Test("0");
		Test test1 = new Test("1");

		Registry.register(TESTS, new Identifier("quilt", "test0"), test0);

		LOGGER.info("{}", TESTS.getId(test0));

		Identifier id = TESTS.getId(test1);
		LOGGER.info("{}", id);

		Test maybeAcacia = TESTS.get(new Identifier("quilt", "test1"));
		if (maybeAcacia != null && maybeAcacia.equals(test0)) {
			LOGGER.info("Defaulted correctly!");
		}

		Registry.register(TESTS, new Identifier("quilt", "test1"), test1);

		Test maybeAcacia2 = TESTS.get(new Identifier("quilt", "test1"));
		if (maybeAcacia2 != null && maybeAcacia2.equals(test1)) {
			LOGGER.info("Test1 was properly registered!");
		}

		registerRandomItems(20);

		int size = TESTS.size();
		LOGGER.info("The size {} matched!", size == 22 ? "was" : "was not");

		registerRandomItems(10);
		LOGGER.info("{}", TESTS.size());
		LOGGER.info("{}", TESTS);
	}

	private static void registerRandomItems(int amount) {
		for (int i = 0; i < amount; i++) {
			Registry.register(TESTS, new Identifier("quilt", "test_" + i), new Test("test_" + i));
		}
	}
}
