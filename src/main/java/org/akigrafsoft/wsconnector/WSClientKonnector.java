/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.wsconnector;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.konnector.ExceptionCreateSessionFailed;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;
import com.akigrafsoft.knetthreads.konnector.SessionBasedClientKonnector;

public class WSClientKonnector extends SessionBasedClientKonnector {

	// private WSClientConfig m_config;
	private Service m_service = null;
	private WSClientImplementor m_clientImplementor = null;

	protected WSClientKonnector(String name) throws ExceptionDuplicate {
		super(name);
	}

	@Override
	public Class<? extends KonnectorConfiguration> getConfigurationClass() {
		return WSClientConfig.class;
	}

	@Override
	protected void doLoadConfig(KonnectorConfiguration config) {
		super.doLoadConfig(config);

		WSClientConfig l_config = (WSClientConfig) config;

		try {
			m_clientImplementor = (WSClientImplementor) l_config.clientImplementorClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void execute(KonnectorDataobject dataobject, Session session) {
		// simply pass the execution to implementor class
		m_clientImplementor.execute(dataobject, session.getUserObject());
	}

	@Override
	protected void createSession(Session session) throws ExceptionCreateSessionFailed {
		// Nothing to do here
		// The creation of WS objects is making connection so let
		// async_startSession do this
	}

	@SuppressWarnings("unchecked")
	@Override
	public void async_startSession(Session session) {
		if (m_service == null) {
			try {
				m_service = (Service) ((WSClientConfig) getConfiguration()).serviceClass
						.getDeclaredConstructor(URL.class, QName.class)
						.newInstance(((WSClientConfig) getConfiguration()).serviceUrl,
								new QName(((WSClientConfig) getConfiguration()).getNamespaceURI(),
										((WSClientConfig) getConfiguration()).getLocalServicePart()));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// SHOULD NOT HAPPEN AS IT SHOsULD HAVE BEEN AUDITED
				e.printStackTrace();
				this.sessionDied(session);
				return;
			}
		}

		// ClassLoader classloader =
		// Thread.currentThread().getContextClassLoader();
		// URL wsdlLocation = classloader.getResource("MyHelloService.wsdl");
		// QName serviceName= new QName("http://test.com/", "MyHelloService");
		//
		// MyHelloService service = new MyHelloService(wsdlLocation,
		// serviceName);

		session.setUserObject(m_service.getPort(
				new QName(((WSClientConfig) getConfiguration()).getNamespaceURI(),
						((WSClientConfig) getConfiguration()).getLocalPortPart()),
				((WSClientConfig) getConfiguration()).portClass));
		this.sessionStarted(session);
	}

	@Override
	protected void async_stopSession(Session session) {
		m_service = null;
		this.sessionStopped(session);
	}

}
