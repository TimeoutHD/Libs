package de.timeout.libs.config;

import javax.annotation.Nonnull;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;

public class JsonConfigOptions extends FileConfigurationOptions {

	protected JsonConfigOptions(@Nonnull MemoryConfiguration configuration) {
		super(configuration);
	}

	@Override
	public JsonConfig configuration() {
		return (JsonConfig) super.configuration();
	}

	@Override
	public JsonConfigOptions copyDefaults(boolean value) {
		super.copyDefaults(value);
		return this;
	}

	@Override
	public JsonConfigOptions copyHeader(boolean value) {
		super.copyHeader(value);
		return this;
	}

	@Override
	public JsonConfigOptions header(String value) {
		super.header(value);
		return this;
	}

	@Override
	public JsonConfigOptions pathSeparator(char value) {
		super.pathSeparator(value);
		return this;
	}

	
}
