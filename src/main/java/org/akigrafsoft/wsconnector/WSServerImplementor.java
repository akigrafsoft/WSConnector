package org.akigrafsoft.wsconnector;

public abstract class WSServerImplementor {

	private WSServerKonnector m_serverKonnector;

	public WSServerImplementor(WSServerKonnector konnector) {
		m_serverKonnector = konnector;
	}

	protected WSServerKonnector getServerKonnector() {
		return m_serverKonnector;
	}
}
