package servent.message.snapshot;

import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.LYSnapshotResult;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

// Ovome sam jebao kevu kolko sam ga izmenio...
public class LYTellMessage extends BasicMessage {

	private static final long serialVersionUID = 3116394054726162318L;

	private Map<Integer, Map<Integer, LYSnapshotResult>> givenHistoryForInitiatorForVersion;
	
	// TODO
	public LYTellMessage(ServentInfo sender, ServentInfo receiver, Map<Integer, Map<Integer, LYSnapshotResult>> givenHistoryForInitiatorForVersion) {
		super(MessageType.LY_TELL, sender, receiver, AppConfig.initiatorsVersion);
		
		this.givenHistoryForInitiatorForVersion = givenHistoryForInitiatorForVersion;
	}
	
	private LYTellMessage(MessageType messageType, ServentInfo sender, ServentInfo receiver, 
			boolean white, List<ServentInfo> routeList, String messageText, int messageId,
			Map<Integer, Map<Integer, LYSnapshotResult>> givenHistoryForInitiatorForVersion) {
		super(messageType, sender, receiver, white, routeList, messageText, messageId, AppConfig.initiatorsVersion);
		this.givenHistoryForInitiatorForVersion = givenHistoryForInitiatorForVersion;
	}

	
	public Map<Integer, Map<Integer, LYSnapshotResult>> getGivenHistoryForInitiatorForVersion() {
		return givenHistoryForInitiatorForVersion;
	}

	public void setGivenHistoryForInitiatorForVersion(
			Map<Integer, Map<Integer, LYSnapshotResult>> givenHistoryForInitiatorForVersion) {
		this.givenHistoryForInitiatorForVersion = givenHistoryForInitiatorForVersion;
	}

	@Override
	public Message setRedColor() {
		Message toReturn = new LYTellMessage(getMessageType(), getOriginalSenderInfo(), getReceiverInfo(),
				false, getRoute(), getMessageText(), getMessageId(), getGivenHistoryForInitiatorForVersion());
		return toReturn;
	}
}
