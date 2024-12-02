package servent.handler.snapshot;

import java.util.Map;

import app.AppConfig;
import app.snapshot_bitcake.LYSnapshotResult;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.LYTellMessage;

public class LYTellHandler implements MessageHandler {

	private Message clientMessage;
	private SnapshotCollector snapshotCollector;
	
	public LYTellHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
		this.clientMessage = clientMessage;
		this.snapshotCollector = snapshotCollector;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.LY_TELL) {
			LYTellMessage lyTellMessage = (LYTellMessage)clientMessage;
			
			int senderID = lyTellMessage.getOriginalSenderInfo().getId();
			Map<Integer, Map<Integer, LYSnapshotResult>> giveHistoryForInitiotorForVersion = lyTellMessage.getGivenHistoryForInitiatorForVersion();
			
			snapshotCollector.addLYSnapshotInfo(
					senderID,
					giveHistoryForInitiotorForVersion);	
		} else {
			AppConfig.timestampedErrorPrint("Tell amount handler got: " + clientMessage);
		}

	}

}
