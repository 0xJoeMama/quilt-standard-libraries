package org.quiltmc.qsl.component.impl.defaults;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.components.FloatComponent;

public class DefaultFloatComponent implements FloatComponent {
	private float value;
	@Nullable
	private Runnable saveOperation;

	public DefaultFloatComponent() {
		this(0);
	}

	public DefaultFloatComponent(float initialValue) {
		this.value = initialValue;
	}

	@Override
	public float get() {
		return this.value;
	}

	@Override
	public void set(float value) {
		this.value = value;
		this.saveNeeded();
	}

	@Override
	public void saveNeeded() {
		if (this.saveOperation != null) {
			this.saveOperation.run();
		}
	}

	@Override
	public void setSaveOperation(@Nullable Runnable runnable) {
		this.saveOperation = runnable;
	}
}
