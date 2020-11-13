package de.timeout.libs.config;

import javax.annotation.NotNull;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.jetbrains.annotations.NotNull;

public class JsonConfigOptions extends FileConfigurationOptions {

	protected JsonConfigOptions(@NotNull MemoryConfiguration configuration) {
		super(configuration);
	}

	@Override
	public @NotNull JsonConfig configuration() {
		return (JsonConfig) super.configuration();
	}

	@Override
	public @NotNull JsonConfigOptions copyDefaults(boolean value) {
		super.copyDefaults(value);
		return this;
	}

	@Override
	public @NotNull JsonConfigOptions copyHeader(boolean value) {
		super.copyHeader(value);
		return this;
	}

	@Override
	public @NotNull JsonConfigOptions header(String value) {
		super.header(value);
		return this;
	}

	@Override
	public @NotNull JsonConfigOptions pathSeparator(char value) {
		super.pathSeparator(value);
		return this;
	}

	
}
