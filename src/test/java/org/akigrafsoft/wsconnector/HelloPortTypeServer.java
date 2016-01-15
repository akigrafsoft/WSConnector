package org.akigrafsoft.wsconnector;

import javax.jws.WebService;

import org.akigrafsoft.wsconnector.WSDataobject;
import org.akigrafsoft.wsconnector.WSServerImplementor;
import org.akigrafsoft.wsconnector.WSServerKonnector;
import org.akigrafsoft.wsdl.helloservice.HelloPortType;

import com.akigrafsoft.knetthreads.Message;

@WebService(name = "HelloWebService", endpointInterface = "org.akigrafsoft.wsdl.helloservice.HelloPortType")
public class HelloPortTypeServer extends WSServerImplementor implements
		HelloPortType {

	public HelloPortTypeServer(WSServerKonnector konnector) {
		super(konnector);
	}

	@Override
	public String sayHello(String firstName) {
		Message message = new Message();
		WSDataobject dataobject = new WSDataobject(message);
		dataobject.operationName = "sayHello";
		dataobject.inboundBuffer = firstName;
		getServerKonnector().handleReceive(message, dataobject);
		return dataobject.outboundBuffer;
	}

}
