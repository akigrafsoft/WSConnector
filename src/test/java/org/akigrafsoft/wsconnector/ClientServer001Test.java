package org.akigrafsoft.wsconnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.akigrafsoft.knetthreads.Dispatcher;
import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.FlowProcessContext;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.Endpoint;
import com.akigrafsoft.knetthreads.EndpointController;
import com.akigrafsoft.knetthreads.RequestEnum;
import com.akigrafsoft.knetthreads.konnector.Konnector;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

public class ClientServer001Test {
	static String SERVER_NAME = "Server01";
	static String CLIENT_NAME = "Client01";

	// private final static XStream xstream = new XStream();

	static WSClientKonnector m_clientKonnector;
	static WSServerKonnector m_serverKonnector;

	static Endpoint m_nap;

	static int port = Utils.findFreePort();

	static class Received {
		Message message;
		KonnectorDataobject dataobject;

		public Received(Message message, KonnectorDataobject dataobject) {
			super();
			this.message = message;
			this.dataobject = dataobject;
		}
	}

	static Received received;

	@BeforeClass
	public static void setUpClass() {
		try {
			m_clientKonnector = new WSClientKonnector(CLIENT_NAME);
		} catch (ExceptionDuplicate e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		try {
			m_serverKonnector = new WSServerKonnector(SERVER_NAME);
		} catch (ExceptionDuplicate e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		try {
			m_nap = new Endpoint("test") {
				@Override
				public KonnectorRouter getKonnectorRouter(Message message, KonnectorDataobject dataobject) {
					return new KonnectorRouter() {
						public Konnector resolveKonnector(Message message, KonnectorDataobject dataobject) {
							return m_serverKonnector;
						}
					};
				}

				@Override
				public RequestEnum classifyInboundMessage(Message message, KonnectorDataobject dataobject) {
					received = new Received(message, dataobject);

					System.out.println("classifyInboundMessage");

					// Fake flow by submitting a response directly
					// TODO should do that in a different threads also
					// to check it works!
					dataobject.outboundBuffer = "Thanks " + dataobject.inboundBuffer + ", Hello!";

					m_serverKonnector.handle(dataobject);

					// leave null as this is fake anyway
					return null;
				}
			};
			m_nap.setDispatcher(new Dispatcher<RequestEnum>("foo") {
				@Override
				public FlowProcessContext getContext(Message message, KonnectorDataobject dataobject,
						RequestEnum request) {
					return null;
				}
			});
			m_serverKonnector.setEndpoint(m_nap);
		} catch (ExceptionDuplicate e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		m_clientKonnector.destroy();
		m_serverKonnector.destroy();

		EndpointController.INSTANCE.removeEndpoint(m_nap);
	}

	@Test
	public void test() {

		try {
			m_serverKonnector
					.configure(new WSServerConfig().url("http://localhost:" + port + "/").maxProcessingTimeSeconds(5)
							.wsServerImplementorClassName("org.akigrafsoft.wsconnector.HelloPortTypeServer"));
		} catch (ExceptionAuditFailed e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		try {
			m_clientKonnector
					.configure(
							new WSClientConfig().url("http://localhost:" + port + "/hello")
									.wsServiceClassName("org.akigrafsoft.wsdl.helloservice.HelloService")
									.wsPortClassName("org.akigrafsoft.wsdl.helloservice.HelloPortType")
									.wsClientImplementorClassName(
											"org.akigrafsoft.wsconnector.HelloWSClientImplementor")
					.namespaceURI("http://wsconnector.akigrafsoft.org/").localServicePart("HelloPortTypeServerService")
					.localPortPart("HelloWebServicePort"));
		} catch (ExceptionAuditFailed e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		assertEquals(Konnector.CommandResult.Success, m_serverKonnector.start());
		assertEquals(Konnector.CommandResult.Success, m_clientKonnector.start());

		Utils.sleep(2);

		Message message = new Message();

		System.out.println("TEST ASYNC MODE");
		{
			WSDataobject dataobject = new WSDataobject(message);
			dataobject.operationMode = KonnectorDataobject.OperationMode.TWOWAY;
			dataobject.operationName = "sayHello";
			dataobject.outboundBuffer = "Kevin";
			message.associateDataobject("test", dataobject);

			dataobject.operationSyncMode = KonnectorDataobject.SyncMode.ASYNC;
			m_clientKonnector.handle(dataobject);
			System.out.println(new Date() + "|WAIT...");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}

			assertEquals("Thanks " + "Kevin" + ", Hello!", dataobject.inboundBuffer);
		}

		assertEquals(Konnector.CommandResult.Success, m_clientKonnector.stop());
		assertEquals(Konnector.CommandResult.Success, m_serverKonnector.stop());

		// fail("Not yet implemented");
	}
}
