package servent.handler.snapshot;

import java.util.Map;

import app.AppConfig;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.LYMarkerMessage;
import servent.message.snapshot.LYTellMessage;

public class LYMarkerHandler implements MessageHandler {
	@Override
	public void run() {
		//Don't actually need this.
		//Everything is done in SimpleServentListener.
	}

}
