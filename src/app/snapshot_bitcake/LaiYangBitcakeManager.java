package app.snapshot_bitcake;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import app.AppConfig;
import servent.message.Message;
import servent.message.snapshot.LYMarkerMessage;
import servent.message.snapshot.LYTellMessage;
import servent.message.util.MessageUtil;

public class LaiYangBitcakeManager implements BitcakeManager {

	private final AtomicInteger currentAmount = new AtomicInteger(1000);
	
	// Map<Key: serventID, Value: Map<Key: snapshotID, Value: result>> - rezultat za odredjeni snapshotID za odredjenog serventa
	private Map<Integer, Map<Integer, LYSnapshotResult>> givenHistoryForInitiatorForVersion = new ConcurrentHashMap<Integer, Map<Integer, LYSnapshotResult>>();

	// Key: serventID, value: givenAmount
	private Map<Integer, Integer> giveHistory = new ConcurrentHashMap<>();
	private Map<Integer, Integer> getHistory = new ConcurrentHashMap<>();
	
	public LaiYangBitcakeManager() {
		for(Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
			giveHistory.put(neighbor, 0);
			getHistory.put(neighbor, 0);
		}
	}
	
	/*
	 * This value is protected by AppConfig.colorLock.
	 * Access it only if you have the blessing.
	 */
	public int recordedAmount = 0;
	
	public void markerEvent(int collectorId, SnapshotCollector snapshotCollector) {
		synchronized (AppConfig.colorLock) {
			
			// ne treba
			AppConfig.isWhite.set(false);
			
			recordedAmount = getCurrentBitcakeAmount();
			LYSnapshotResult snapshotResult = new LYSnapshotResult(AppConfig.myServentInfo.getId(), recordedAmount, giveHistory, getHistory); 			
			
			// Ako sam ja inicijator Snapshot-a, dodajem sebe u rezultat
			if (collectorId == AppConfig.myServentInfo.getId()) {
				
				// Key: serventID, Value: snapshotID
				Map<Integer, Integer> initiatorsVersion = AppConfig.initiatorsVersion;	// uzimam listu svih mogucih snapshotInitiatorServenata i njihove trenutne snapshotID-jeve
				
				// Povecavamo svoj snapshotID
				initiatorsVersion.put(AppConfig.myServentInfo.getId(), initiatorsVersion.get(AppConfig.myServentInfo.getId()) + 1);
//				AppConfig.timestampedErrorPrint("Zapoceo sam novi snapshot. Lista inicijatora sa verzijama: " + AppConfig.initiatorsVersion.toString());
				
				// Key: myServentID, Value: myResult
				Map<Integer, LYSnapshotResult> tmpMap = new ConcurrentHashMap<Integer, LYSnapshotResult>();
				int mySnapshotID = AppConfig.initiatorsVersion.get(collectorId);
				tmpMap.put(mySnapshotID, snapshotResult);
				
				// put: Map<Key: serventID, Value: Map<Key: snapshotID, Value: result>> - rezultat za odredjeni snapshotID za odredjenog serventa
				givenHistoryForInitiatorForVersion.put(new Integer (collectorId),tmpMap);
				
				snapshotCollector.addLYSnapshotInfo(AppConfig.myServentInfo.getId(), givenHistoryForInitiatorForVersion);
				
			// Ako ja nisam inicijator Snapshot-a, onda saljem TELL_MESSAGE
			} else {
				
				// Key: myServentID, Value: myResult
				Map<Integer, LYSnapshotResult> tmpMap = new ConcurrentHashMap<Integer, LYSnapshotResult>();
				int collectorSnapshotID = AppConfig.initiatorsVersion.get(collectorId); 
				tmpMap.put(collectorSnapshotID, snapshotResult);
				
				// put: Map<Key: serventID, Value: Map<Key: snapshotID, Value: result>> - rezultat za odredjeni snapshotID za odredjenog serventa
				givenHistoryForInitiatorForVersion.put(new Integer(collectorId), tmpMap);	
				
				Message tellMessage = new LYTellMessage(AppConfig.myServentInfo, AppConfig.getInfoById(collectorId), givenHistoryForInitiatorForVersion); 

//				AppConfig.timestampedErrorPrint("Kako ja koji nisam inicijator vidim verzije " + AppConfig.initiatorsVersion.toString());
				MessageUtil.sendMessage(tellMessage);
			}
			
			// Saljemo Markere susedima
			for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
				
				Message clMarker = new LYMarkerMessage(AppConfig.myServentInfo, AppConfig.getInfoById(neighbor), collectorId, AppConfig.initiatorsVersion);
				
				MessageUtil.sendMessage(clMarker);
				
				try {
					// Test	
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void takeSomeBitcakes(int amount) {
		currentAmount.getAndAdd(-amount);
	}
	
	public void addSomeBitcakes(int amount) {
		currentAmount.getAndAdd(amount);
	}
	
	public int getCurrentBitcakeAmount() {
		return currentAmount.get();
	}
	
	private class MapValueUpdater implements BiFunction<Integer, Integer, Integer> {
		
		private int valueToAdd;
		
		public MapValueUpdater(int valueToAdd) {
			this.valueToAdd = valueToAdd;
		}
		
		@Override
		public Integer apply(Integer key, Integer oldValue) {
			return oldValue + valueToAdd;
		}
	}
	
	public void recordGiveTransaction(int neighbor, int amount) {
		giveHistory.compute(neighbor, new MapValueUpdater(amount));
	}
	
	public void recordGetTransaction(int neighbor, int amount) {
		getHistory.compute(neighbor, new MapValueUpdater(amount));
	}
	
	
	
}

