package com.madibasoft.envisadroid.api.tpi.event;



public interface TPIListener {

	public void panelModeEvent(PanelModeEvent panelEvent);

	public void zoneEvent(ZoneEvent ge);    

	public void ledEvent(LEDEvent ledEvent);    

	public void partitionEvent(PartitionEvent partitionEvent);

	public void loginEvent(LoginEvent loginEvent);

	public void errorEvent(ErrorEvent errorEvent);    

	public void infoEvent(InfoEvent infoEvent);
	
	public void closeEvent(CloseEvent closeEvent);

	public void openEvent(OpenEvent openEvent);

	public void chimeEvent(ChimeEvent chimeEvent);

	public void panelEvent(PanelEvent panelEvent);

	public void smokeEvent(SmokeEvent smokeEvent);


}
