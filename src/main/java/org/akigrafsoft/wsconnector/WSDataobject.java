/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.wsconnector;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

public class WSDataobject extends KonnectorDataobject {

	/**
	 * The WS Operation name
	 */
	public String operationName;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1266905422996155939L;

	transient private final CountDownLatch m_latch = new CountDownLatch(1);

	public WSDataobject(Message message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	boolean waitForReponse(long timeoutSeconds) throws InterruptedException {
		return m_latch.await(timeoutSeconds, TimeUnit.SECONDS);
	}

	/**
	 * Call this method when a response is set on the message. It will notify
	 * ServerKonnector that response can be sent
	 */
	void done() {
		if (m_latch != null)
			m_latch.countDown();
	}
}
