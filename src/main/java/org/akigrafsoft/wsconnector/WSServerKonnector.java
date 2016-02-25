/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.wsconnector;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.ws.Endpoint;

import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.konnector.Konnector;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

/**
 * 
 * @author kmoyse
 * 
 */
public class WSServerKonnector extends Konnector {

	private Endpoint m_endpoint;

	private URL m_url;

	private Object m_serverImplementor;

	protected WSServerKonnector(String name) throws ExceptionDuplicate {
		super(name);
	}

	@Override
	public Class<? extends KonnectorConfiguration> getConfigurationClass() {
		return WSServerConfig.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doLoadConfig(KonnectorConfiguration config) {
		super.doLoadConfig(config);

		WSServerConfig l_config = (WSServerConfig) config;

		try {
			m_url = new URL(l_config.getUrl());
		} catch (MalformedURLException e) {
			// should not happen as it was audited
		}

		try {
			m_serverImplementor = (Object) l_config.serverImplementorClass.getConstructor(WSServerKonnector.class)
					.newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected CommandResult doStart() {

		m_endpoint = Endpoint.publish(m_url.toString(), m_serverImplementor);

		// TODO : this is different and more complex if https is required

		this.setStarted();
		this.setAvailable();
		return CommandResult.Success;
	}

	@Override
	public void doHandle(KonnectorDataobject dataobject) {
		WSDataobject l_do = (WSDataobject) dataobject;

		// Nothing to do here, except notify the thread handling the original
		// request it can resume execution. The WSDL wrapping layer handles the
		// actual response
		l_do.done();

		resumeWithExecutionComplete(dataobject);
	}

	@Override
	protected CommandResult doStop() {
		m_endpoint.stop();
		this.setUnavailable();
		this.setStopped();
		return CommandResult.Success;
	}

	/**
	 * To be called from Endpoint implementing the WS. the call is blocking
	 * while message is being processed (max configured).
	 * 
	 * @param message
	 * @param dataobject
	 */
	void handleReceive(Message message, WSDataobject dataobject) {

		injectMessageInApplication(message, dataobject);

		boolean response = false;
		try {
			response = dataobject.waitForReponse(((WSServerConfig) getConfiguration()).getMaxProcessingTimeSeconds());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (response) {
			if (ActivityLogger.isInfoEnabled())
				ActivityLogger.info(buildActivityLog(message, "responding<" + dataobject.operationName + ">"));
		} else {
			ActivityLogger.warn(buildActivityLog(message, "handleReceive no response within maxProcessingTimeSeconds<"
					+ ((WSServerConfig) getConfiguration()).getMaxProcessingTimeSeconds() + ">"));
		}
	}

}
