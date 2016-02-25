/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.wsconnector;

import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

public abstract class WSClientImplementor {
	/**
	 * Must be implemented to perform client side execution of WS, using port.
	 * 
	 * @param dataobject
	 * @param port
	 */
	abstract void execute(KonnectorDataobject dataobject, Object port);
}
