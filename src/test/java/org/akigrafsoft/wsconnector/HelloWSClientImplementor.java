package org.akigrafsoft.wsconnector;

import org.akigrafsoft.wsconnector.WSClientImplementor;
import org.akigrafsoft.wsconnector.WSDataobject;
import org.akigrafsoft.wsdl.helloservice.HelloPortType;

import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

public class HelloWSClientImplementor extends WSClientImplementor {

	@Override
	void execute(KonnectorDataobject dataobject, Object port) {
		WSDataobject l_do = (WSDataobject) dataobject;
		HelloPortType l_port = (HelloPortType) port;
		if ("sayHello".equals(l_do.operationName)) {
			l_do.inboundBuffer = l_port.sayHello(l_do.outboundBuffer);
		}
	}
}
