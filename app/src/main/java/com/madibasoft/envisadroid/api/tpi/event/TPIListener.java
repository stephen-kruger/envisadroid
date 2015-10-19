package com.madibasoft.envisadroid.api.tpi.event;



public interface TPIListener {

	void panelModeEvent(PanelModeEvent panelEvent);

	void zoneEvent(ZoneEvent ge);

	void ledEvent(LEDEvent ledEvent);

	void partitionEvent(PartitionEvent partitionEvent);

	void loginEvent(LoginEvent loginEvent);

	void errorEvent(ErrorEvent errorEvent);

	void infoEvent(InfoEvent infoEvent);
	
	void closeEvent(CloseEvent closeEvent);

	void openEvent(OpenEvent openEvent);

	void chimeEvent(ChimeEvent chimeEvent);

	void panelEvent(PanelEvent panelEvent);

	void smokeEvent(SmokeEvent smokeEvent);


}
