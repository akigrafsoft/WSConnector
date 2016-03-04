package org.akigrafsoft.wsconnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.akigrafsoft.knetthreads.Dispatcher;
import com.akigrafsoft.knetthreads.Endpoint;
import com.akigrafsoft.knetthreads.EndpointController;
import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.FlowProcessContext;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.RequestEnum;
import com.akigrafsoft.knetthreads.konnector.Konnector;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;
import com.akigrafsoft.knetthreads.routing.EndpointRouter;
import com.akigrafsoft.knetthreads.routing.KonnectorRouter;

@Ignore
public class ClientServerASGTest {
	static String SERVER_NAME = "Server01";
	static String SERVER_NAME2 = "Server02";
	static String CLIENT_NAME = "Client01";

	// private final static XStream xstream = new XStream();

	static WSServerKonnector m_serverKonnector;
	static WSServerKonnector m_serverKonnector2;
	static WSClientKonnector m_clientKonnector;

	static private Endpoint m_ep;

	static int serverKonnectorPort = 8096;
	static int serverKonnector2Port = 8098;
	static int asgPort = 9222;

	static class Received {
		Message message;
		KonnectorDataobject dataobject;

		public Received(Message message, KonnectorDataobject dataobject) {
			super();
			this.message = message;
			this.dataobject = dataobject;
		}
	}

	static List<Received> received = Collections.synchronizedList(new ArrayList<Received>());

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
			m_serverKonnector2 = new WSServerKonnector(SERVER_NAME2);
		} catch (ExceptionDuplicate e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		try {
			m_ep = new Endpoint("test") {
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

					received.add(new Received(message, dataobject));

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
			m_ep.setDispatcher(new Dispatcher<RequestEnum>("foo") {
				@Override
				public FlowProcessContext getContext(Message message, KonnectorDataobject dataobject,
						RequestEnum request) {
					return null;
				}
			});
			m_serverKonnector.setEndpointRouter(new EndpointRouter() {
				@Override
				public Endpoint resolveKonnector(Message message, KonnectorDataobject dataobject) {
					return m_ep;
				}
			});
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

		EndpointController.INSTANCE.removeEndpoint(m_ep);
	}

	@Test
	public void test() {

		try {
			m_serverKonnector.configure(new WSServerConfig().url("http://localhost:" + serverKonnectorPort + "/hello")
					.maxProcessingTimeSeconds(15)
					.wsServerImplementorClassName("org.akigrafsoft.wsconnector.HelloPortTypeServer"));
		} catch (ExceptionAuditFailed e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		try {
			m_clientKonnector
					.configure(
							new WSClientConfig()
									// .url("http://localhost:" + asgPort +
									// "/hello")
									.localWSDL("file:ws/HelloService_client.wsdl")
									.wsServiceClassName("org.akigrafsoft.wsdl.helloservice.HelloService")
									.wsPortClassName("org.akigrafsoft.wsdl.helloservice.HelloPortType")
									.wsClientImplementorClassName(
											"org.akigrafsoft.wsconnector.HelloWSClientImplementor")
					// .namespaceURI("http://wsconnector.akigrafsoft.org/")
					.namespaceURI("http://www.examples.com/wsdl/HelloService.wsdl")
					.localServicePart("HelloPortTypeServerService")
					// .localServicePart("Hello_Service")
					.localPortPart("HelloWebServicePort"));
			// .localPortPart("Hello_Port"));
		} catch (ExceptionAuditFailed e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		System.out.println("Start Connectors");

		assertEquals(Konnector.CommandResult.Success, m_serverKonnector.start());
		assertEquals(Konnector.CommandResult.Success, m_clientKonnector.start());

		System.out.println("Connectors started");

		Utils.sleep(5);

		System.out.println("Sending WS request");

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
			Utils.sleep(2);

			assertEquals("Thanks " + "Kevin" + ", Hello!", dataobject.inboundBuffer);
		}

		Utils.sleep(2);

		assertEquals(Konnector.CommandResult.Success, m_clientKonnector.stop());
		assertEquals(Konnector.CommandResult.Success, m_serverKonnector.stop());

		// fail("Not yet implemented");
	}
}
