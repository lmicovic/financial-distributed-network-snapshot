package app.snapshot_bitcake;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import app.AppConfig;

/**
 * Main snapshot collector class. Has support for Naive, Chandy-Lamport
 * and Lai-Yang snapshot algorithms.
 * 
 * @author bmilojkovic
 *
 */
public class SnapshotCollectorWorker implements SnapshotCollector {

	private Map<Integer, Map<Integer, Map<Integer, LYSnapshotResult>>> collectedLYValues = new ConcurrentHashMap<>();
	
	private volatile boolean working = true;
	
	private AtomicBoolean collecting = new AtomicBoolean(false);
	
	private BitcakeManager bitcakeManager;

	
	public SnapshotCollectorWorker() {
		bitcakeManager = new LaiYangBitcakeManager();
	}
	
	
	
	@Override
	public void run() {
		while(working) {
			
			/*
			 * Not collecting yet - just sleep until we start actual work, or finish
			 */
			while (collecting.get() == false) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (working == false) {
					return;
				}
			}
			
			/*
			 * Collecting is done in three stages:
			 * 1. Send messages asking for values
			 * 2. Wait for all the responses
			 * 3. Print result
			 */
			
			//1 send asks
			int mySnapshotId = AppConfig.initiatorsVersion.get(AppConfig.myServentInfo.getId());
			AppConfig.timestampedStandardPrint("Started collecting snapshot: " + mySnapshotId);
			AppConfig.timestampedStandardPrint("Lista inicijatora: " + AppConfig.initiatorsVersion.toString());
			
			((LaiYangBitcakeManager)bitcakeManager).markerEvent(AppConfig.myServentInfo.getId(), this);
			
			AppConfig.timestampedStandardPrint("Waitng for tell response...");
			//2 wait for responses or finish
			boolean waiting = true;
			while (waiting) {
				if (collectedLYValues.size() == AppConfig.getServentCount()) {
					waiting = false;
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (working == false) {
					return;
				}
			}
			
			AppConfig.timestampedStandardPrint("Got all tell responses.");
			
			
			
			Integer collectorId = AppConfig.myServentInfo.getId();
			Integer version = AppConfig.initiatorsVersion.get(collectorId);	// current snapshotID for serventCollector

			AppConfig.timestampedStandardPrint("Servent Snapshot initiator ID: " + collectorId);
			AppConfig.timestampedStandardPrint("Snapshot ID: " + version);

			AppConfig.timestampedStandardPrint("Collected values " + collectedLYValues.toString());

			int sum = 0;
			
			// Prolazimo kroz pristigle rezultate
			for(Entry<Integer, Map<Integer, Map<Integer, LYSnapshotResult>>> result: collectedLYValues.entrySet()) {
				
				int serventID = result.getKey();
				
				// Uzimamo rezultat za trenutnog serventa rezultat za collectorID i trenutnu vreziju Snapshot-a koja se radi
				int serventResult = result.getValue().get(collectorId).get(version).getRecordedAmount();
				sum += serventResult;
				
				AppConfig.timestampedStandardPrint(
						"Recorded bitcake amount for " + serventID + " = " + serventResult);
			}
			
			// Ispisujemo rezultate koji su u kanalima 
			for(int i = 0; i < AppConfig.getServentCount(); i++) {
				for (int j = 0; j < AppConfig.getServentCount(); j++) {
					if (i != j) {
						if (AppConfig.getInfoById(i).getNeighbors().contains(j) &&
							AppConfig.getInfoById(j).getNeighbors().contains(i)) {
							int ijAmount = collectedLYValues.get(i).get(collectorId).get(version).getGiveHistory().get(j);
							int jiAmount = collectedLYValues.get(j).get(collectorId).get(version).getGetHistory().get(i);
							
							if (ijAmount != jiAmount) {
								String outputString = String.format(
										"Unreceived bitcake amount: %d from servent %d to servent %d",
										ijAmount - jiAmount, i, j);
								AppConfig.timestampedStandardPrint(outputString);
								sum += ijAmount - jiAmount;
							}
						}
					}
				}
			}
			

			AppConfig.timestampedStandardPrint("System bitcake count: " + sum);
			
			collectedLYValues.clear(); //reset for next invocation
			collecting.set(false);
			

		}

	}
	
	// Dodajemo rezulatat za serventa za odredjeni verziju snapshota
	public void addLYSnapshotInfo(int serventId, Map<Integer, Map<Integer, LYSnapshotResult>> givenHistoryForInitiatorForVersion) {
		// Map<Key: serventID, Value: Map<serventID, Value: Map<Key: snapshotID, Value: result>>>
		collectedLYValues.put(serventId, givenHistoryForInitiatorForVersion);
	}
	
	@Override
	public void startCollecting() {
		boolean oldValue = this.collecting.getAndSet(true);
		
		if (oldValue == true) {
			AppConfig.timestampedErrorPrint("Tried to start collecting before finished with previous.");
		}
	}
	
	@Override
	public BitcakeManager getBitcakeManager() {
		return bitcakeManager;
	}
	
	@Override
	public void stop() {
		working = false;
	}

}
