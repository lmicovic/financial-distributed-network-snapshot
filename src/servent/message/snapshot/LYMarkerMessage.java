package servent.message.snapshot;

import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class LYMarkerMessage extends BasicMessage {

	private static final long serialVersionUID = -5814632211689247846L;
	
	// SnapshotID svih servenata koji mogu biti inicijatori snapshota na sistemu
	private Map<Integer, Integer> initiatorsVersion;

	public LYMarkerMessage(ServentInfo sender, ServentInfo receiver, int collectorId, Map<Integer, Integer> initiatorsVersion) {
		super(MessageType.LY_MARKER, sender, receiver, String.valueOf(collectorId), initiatorsVersion);
		this.initiatorsVersion = initiatorsVersion;
	}

	public Map<Integer, Integer> getInitiatorsVersion() {
		return initiatorsVersion;
	}

	public void setInitiatorsVersion(Map<Integer, Integer> initiatorsVersion) {
		this.initiatorsVersion = initiatorsVersion;
	}
	
}
