package org.akigrafsoft.wsconnector;

import java.net.MalformedURLException;
import java.net.URL;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;

public class WSServerConfig extends KonnectorConfiguration {

	public String url = "http://localhost:8080/";

	public long maxProcessingTimeSeconds = 10;

	public String wsServerImplementorClassName = null;

	@SuppressWarnings("rawtypes")
	Class serverImplementorClass;

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
