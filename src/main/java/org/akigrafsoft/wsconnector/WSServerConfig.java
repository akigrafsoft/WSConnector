package org.akigrafsoft.wsconnector;

import java.net.MalformedURLException;
import java.net.URL;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;

/**
 * Configuration class for {@link WSServerKonnector}
 * <p>
 * <b>This MUST be a Java bean</b>
 * </p>
 * 
 * @author kmoyse
 * 
 */
public class WSServerConfig extends KonnectorConfiguration {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8067551870595317417L;

	private String url = "http://localhost:8080/";
	private long maxProcessingTimeSeconds = 10;
	private String wsServerImplementorClassName = null;

	@SuppressWarnings("rawtypes")
	Class serverImplementorClass;

	// ------------------------------------------------------------------------
	// Java Bean

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getMaxProcessingTimeSeconds() {
		return maxProcessingTimeSeconds;
	}

	public void setMaxProcessingTimeSeconds(long maxProcessingTimeSeconds) {
		this.maxProcessingTimeSeconds = maxProcessingTimeSeconds;
	}

	public String getWsServerImplementorClassName() {
		return wsServerImplementorClassName;
	}

	public void setWsServerImplementorClassName(
			String wsServerImplementorClassName) {
		this.wsServerImplementorClassName = wsServerImplementorClassName;
	}

	// ------------------------------------------------------------------------
	// Fluent API
	/**
	 * Set URL to listen on
	 * 
	 * @param value
	 * @return
	 */
	public WSServerConfig url(String value) {
		this.url = value;
		return this;
	}

	public WSServerConfig maxProcessingTimeSeconds(long value) {
		this.maxProcessingTimeSeconds = value;
		return this;
	}

	public WSServerConfig wsServerImplementorClassName(String value) {
		this.wsServerImplementorClassName = value;
		return this;
	}

	// ------------------------------------------------------------------------
	// Configuration

	@SuppressWarnings("unchecked")
	@Override
	public void audit() throws ExceptionAuditFailed {
		super.audit();
		try {
			new URL(this.url);
		} catch (MalformedURLException e) {
			throw new ExceptionAuditFailed("MalformedURLException:"
					+ e.getMessage());
		}

		try {
			serverImplementorClass = Class
					.forName(wsServerImplementorClassName);
			serverImplementorClass.getConstructor(WSServerKonnector.class);
		} catch (ClassNotFoundException | IllegalArgumentException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new ExceptionAuditFailed("bad wsServerImplementorClassName <"
					+ wsServerImplementorClassName + "> " + e.getMessage());
		}
	}
}
