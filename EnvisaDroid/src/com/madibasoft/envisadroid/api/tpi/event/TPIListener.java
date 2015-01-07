package com.madibasoft.envisadroid.api.tpi.event;



public interface TPIListener {

	public void panelEvent(PanelEvent panelEvent);

	public void zoneEvent(ZoneEvent ge);    

	public void ledEvent(LEDEvent ledEvent);    

	public void partitionEvent(PartitionEvent partitionEvent);

	public void loginEvent(LoginEvent loginEvent);

	public void errorEvent(ErrorEvent errorEvent);    

	public void infoEvent(InfoEvent infoEvent);

	public void chimeEvent(ChimeEvent chimeEvent);

	public void smokeEvent(SmokeEvent smokeEvent);


}
