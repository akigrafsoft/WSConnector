package org.akigrafsoft.wsconnector;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.SessionBasedClientKonnectorConfiguration;

public class WSClientConfig extends SessionBasedClientKonnectorConfiguration {

	public String url = null;
	public String localWSDL = null;

	public String wsServiceClassName = null;
	public String wsPortClassName = null;
	public String wsClientImplementorClassName = null;
	public String namespaceURI;
	public String localServicePart;
	public String localPortPart;

	URL serviceUrl = null;

	@SuppressWarnings("rawtypes")
	Class serviceClass;
	@SuppressWarnings("rawtypes")
	Class portClass;
	@SuppressWarnings("rawtypes")
	Class clientImplementorClass;

	// ------------------------------------------------------------------------
	// Fluent API
	/**
	 * Set URL to listen on
	 * 
	 * @param value
	 * @return
	 */
	public WSClientConfig url(String value) {
		this.url = value;
		return this;
	}

	public WSClientConfig localWSDL(String value) {
		this.localWSDL = value;
		return this;
	}

	public WSClientConfig wsServiceClassName(String value) {
		this.wsServiceClassName = value;
		return this;
	}

	public WSClientConfig wsPortClassName(String value) {
		this.wsPortClassName = value;
		return this;
	}

	public WSClientConfig wsClientImplementorClassName(String value) {
		this.wsClientImplementorClassName = value;
		return this;
	}

	public WSClientConfig namespaceURI(String value) {
		this.namespaceURI = value;
		return this;
	}

	public WSClientConfig localServicePart(String value) {
		this.localServicePart = value;
		return this;
	}

	public WSClientConfig localPortPart(String value) {
		this.localPortPart = value;
		return this;
	}

	// ------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Override
	public void audit() throws ExceptionAuditFailed {
		super.audit();

		if (this.url != null) {
			try {
				serviceUrl = new URL(this.url);
			} catch (MalformedURLException e) {
				throw new ExceptionAuditFailed("url|MalformedURLException:"
						+ e.getMessage());
			}
		} else if (this.localWSDL != null) {
//			ClassLoader classloader = Thread.currentThread()
//					.getContextClassLoader();
//			serviceUrl = classloader.getResource(this.localWSDL);
			try {
				serviceUrl = new URL(this.localWSDL);
			} catch (MalformedURLException e) {
				throw new ExceptionAuditFailed(
						"localWSDL|MalformedURLException:" + e.getMessage());
			}
		} else {
			throw new ExceptionAuditFailed(
					"url or localWSDL must be configured");
		}

		try {
			serviceClass = Class.forName(wsServiceClassName);
			serviceClass.newInstance();
			serviceClass.getDeclaredConstructor(URL.class, QName.class);
			// .newInstance(new URL(url),
			// new QName(namespaceURI, localPart));
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| NoSuchMethodException | SecurityException e) {
			throw new ExceptionAuditFailed("bad wsServiceClassName <"
					+ wsServiceClassName + "> " + e.getMessage() + e.getClass());
		}

		try {
			portClass = Class.forName(wsPortClassName);
		} catch (ClassNotFoundException e) {
			throw new ExceptionAuditFailed("bad wsPortClassName <"
					+ wsPortClassName + "> " + e.getMessage());
		}

		try {
			clientImplementorClass = Class
					.forName(wsClientImplementorClassName);
		} catch (ClassNotFoundException e) {
			throw new ExceptionAuditFailed("bad wsClientImplementorClassName <"
					+ wsClientImplementorClassName + "> " + e.getMessage());
		}
	}
}