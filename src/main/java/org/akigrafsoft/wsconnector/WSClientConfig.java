/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.wsconnector;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.SessionBasedClientKonnectorConfiguration;

/**
 * Configuration class for {@link WSClientKonnector}
 * <p>
 * <b>This MUST be a Java bean</b>
 * </p>
 * 
 * @author kmoyse
 * 
 */
public class WSClientConfig extends SessionBasedClientKonnectorConfiguration {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6292914295934898576L;

	private String url = null;
	private String localWSDL = null;

	private String wsServiceClassName = null;
	private String wsPortClassName = null;
	private String wsClientImplementorClassName = null;
	private String namespaceURI;
	private String localServicePart;
	private String localPortPart;

	URL serviceUrl = null;

	@SuppressWarnings("rawtypes")
	Class serviceClass;
	@SuppressWarnings("rawtypes")
	Class portClass;
	@SuppressWarnings("rawtypes")
	Class clientImplementorClass;

	// ------------------------------------------------------------------------
	// Java Bean

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLocalWSDL() {
		return localWSDL;
	}

	public void setLocalWSDL(String localWSDL) {
		this.localWSDL = localWSDL;
	}

	public String getWsServiceClassName() {
		return wsServiceClassName;
	}

	public void setWsServiceClassName(String wsServiceClassName) {
		this.wsServiceClassName = wsServiceClassName;
	}

	public String getWsPortClassName() {
		return wsPortClassName;
	}

	public void setWsPortClassName(String wsPortClassName) {
		this.wsPortClassName = wsPortClassName;
	}

	public String getWsClientImplementorClassName() {
		return wsClientImplementorClassName;
	}

	public void setWsClientImplementorClassName(String wsClientImplementorClassName) {
		this.wsClientImplementorClassName = wsClientImplementorClassName;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	public String getLocalServicePart() {
		return localServicePart;
	}

	public void setLocalServicePart(String localServicePart) {
		this.localServicePart = localServicePart;
	}

	public String getLocalPortPart() {
		return localPortPart;
	}

	public void setLocalPortPart(String localPortPart) {
		this.localPortPart = localPortPart;
	}

	// ------------------------------------------------------------------------
	// Fluent API
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
				throw new ExceptionAuditFailed("url|MalformedURLException:" + e.getMessage());
			}
		} else if (this.localWSDL != null) {
			// ClassLoader classloader = Thread.currentThread()
			// .getContextClassLoader();
			// serviceUrl = classloader.getResource(this.localWSDL);
			try {
				serviceUrl = new URL(this.localWSDL);
			} catch (MalformedURLException e) {
				throw new ExceptionAuditFailed("localWSDL|MalformedURLException:" + e.getMessage());
			}
		} else {
			throw new ExceptionAuditFailed("url or localWSDL must be configured");
		}

		try {
			serviceClass = Class.forName(wsServiceClassName);
			serviceClass.newInstance();
			serviceClass.getDeclaredConstructor(URL.class, QName.class);
			// .newInstance(new URL(url),
			// new QName(namespaceURI, localPart));
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| NoSuchMethodException | SecurityException e) {
			throw new ExceptionAuditFailed(
					"bad wsServiceClassName <" + wsServiceClassName + "> " + e.getMessage() + e.getClass());
		}

		try {
			portClass = Class.forName(wsPortClassName);
		} catch (ClassNotFoundException e) {
			throw new ExceptionAuditFailed("bad wsPortClassName <" + wsPortClassName + "> " + e.getMessage());
		}

		try {
			clientImplementorClass = Class.forName(wsClientImplementorClassName);
		} catch (ClassNotFoundException e) {
			throw new ExceptionAuditFailed(
					"bad wsClientImplementorClassName <" + wsClientImplementorClassName + "> " + e.getMessage());
		}
	}
}