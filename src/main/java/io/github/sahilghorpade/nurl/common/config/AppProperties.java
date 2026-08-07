package io.github.sahilghorpade.nurl.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private String baseUrl;
	private Set<String> reservedAliases;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public Set<String> getReservedAliases() {
		return reservedAliases;
	}

	public void setReservedAliases(Set<String> reservedAliases) {
		this.reservedAliases = reservedAliases;
	}

}
