package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import servent.message.BasicMessage;
import servent.message.snapshot.LYMarkerMessage;

/**
 * This class contains all the global application configuration stuff.
 * @author bmilojkovic
 *
 */
public class AppConfig {
	
	public static ServentInfo myServentInfo;
	
	private static List<ServentInfo> serventInfoList = new ArrayList<>();

	public static boolean IS_CLIQUE = false;
	
	public static AtomicBoolean isWhite = new AtomicBoolean(true);
	public static Object colorLock = new Object();
	
	// Key: serventID, Value: snapshotID
	public static Map<Integer, Integer> initiatorsVersion = new ConcurrentHashMap<>();			// Lista servenata koji mogu biti snapshot inicijatori sa svojim trenutnom snapshotId-jevima
	
	
	
	/**
	 * Reads a config file. Should be called once at start of app.
	 * The config file should be of the following format:
	 * <br/>
	 * <code><br/>
	 * servent_count=3 			- number of servents in the system <br/>
	 * clique=false 			- is it a clique or not <br/>
	 * fifo=false				- should sending be fifo
	 * servent0.port=1100 		- listener ports for each servent <br/>
	 * servent1.port=1200 <br/>
	 * servent2.port=1300 <br/>
	 * servent0.neighbors=1,2 	- if not a clique, who are the neighbors <br/>
	 * servent1.neighbors=0 <br/>
	 * servent2.neighbors=0 <br/>
	 * 
	 * </code>
	 * <br/>
	 * So in this case, we would have three servents, listening on ports:
	 * 1100, 1200, and 1300. This is not a clique, and:<br/>
	 * servent 0 sees servent 1 and 2<br/>
	 * servent 1 sees servent 0<br/>
	 * servent 2 sees servent 0<br/>
	 * 
	 * @param configName name of configuration file
	 */
	public static void readConfig(String configName){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));
			
		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}
		
		int serventCount = -1;
		try {
			serventCount = Integer.parseInt(properties.getProperty("servent_count"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading servent_count. Exiting...");
			System.exit(0);
		}
		
		IS_CLIQUE = Boolean.parseBoolean(properties.getProperty("clique", "false"));
		String snapshotType = properties.getProperty("snapshot");
		if (snapshotType == null) {
			snapshotType = "none";
		}
		
		for (int i = 0; i < serventCount; i++) {
			String portProperty = "servent"+i+".port";
			
			int serventPort = -1;
			
			try {
				serventPort = Integer.parseInt(properties.getProperty(portProperty));
			} catch (NumberFormatException e) {
				timestampedErrorPrint("Problem reading " + portProperty + ". Exiting...");
				System.exit(0);
			}
			
			List<Integer> neighborList = new ArrayList<>();
			List<Integer> initiatorsList = new ArrayList<Integer>();
			if (IS_CLIQUE) {
				for(int j = 0; j < serventCount; j++) {
					if (j == i) {
						continue;
					}
					
					neighborList.add(j);
				}
			} else {
				String neighborListProp = properties.getProperty("servent"+i+".neighbors");
				
				if (neighborListProp == null) {
					timestampedErrorPrint("Warning: graph is not clique, and node " + i + " doesnt have neighbors");
				} else {
					String[] neighborListArr = neighborListProp.split(",");
					
					try {
						for (String neighbor : neighborListArr) {
							neighborList.add(Integer.parseInt(neighbor));
						}
					} catch (NumberFormatException e) {
						timestampedErrorPrint("Bad neighbor list for node " + i + ": " + neighborListProp);
					}
				}
			}
			try {
				String initiatorsString = properties.getProperty("initiators");
				
				if (initiatorsString == null) {
					timestampedErrorPrint("You miss initoators property");
				}
			 else {
				 String[] initiator = initiatorsString.split(",");
				 
				try {
					for (String init : initiator) {
						initiatorsList.add(Integer.parseInt(init));
						initiatorsVersion.put(Integer.parseInt(init), 0);
					}
				} catch (NumberFormatException e) {
					timestampedErrorPrint("Bad neighbor list for node " + i + ": " + initiatorsString);
				}
			 }
			
			} catch (NumberFormatException e) {
				timestampedErrorPrint("Problem reading servent_count. Exiting...");
				System.exit(0);
			}
				
			ServentInfo newInfo = new ServentInfo("localhost", i, serventPort, neighborList, initiatorsList);
			serventInfoList.add(newInfo);
		}
	}
	
	/**
	 * Get info for a servent selected by a given id.
	 * @param id id of servent to get info for
	 * @return {@link ServentInfo} object for this id
	 */
	public static ServentInfo getInfoById(int id) {
		if (id >= getServentCount()) {
			throw new IllegalArgumentException(
					"Trying to get info for servent " + id + " when there are " + getServentCount() + " servents.");
		}
		return serventInfoList.get(id);
	}
	
	/**
	 * Get number of servents in this system.
	 */
	public static int getServentCount() {
		return serventInfoList.size();
	}

	public static Map<Integer, Integer> getInitiatorsVersion() {
		return initiatorsVersion;
	}

	public void setInitiatorsVersion(Map<Integer, Integer> initiatorsVersion) {
		this.initiatorsVersion = initiatorsVersion;
	}

	public static AtomicBoolean getIsWhite() {
		return isWhite;
	}

	public static void setIsWhite(AtomicBoolean isWhite) {
		AppConfig.isWhite = isWhite;
	}

	public static int getVersionForInitiator(Integer collectorId) {
		return initiatorsVersion.get(collectorId);
	}
	
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.out.println(timeFormat.format(now) + " - " + message);
	}
	
	
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.err.println(timeFormat.format(now) + " - " + message);
	}
	
	/*
	 * 0 - ja i poruka smo beli
	 * 1 - ja sma beo poruka je crvena
	 * 2 - ja sam crven poruka je bela
	 * */
	// Pronalazimo razliku izmedju SnapshotID-jeva iz poruke i SnapshotID-jeva za servente na lokalnom sistemu
	// Ako pronadjemo razliku, onda to znaci da je serventID za koji smo pronasli razliku izmedju SnapshotID-jevima zapoceo novi Snapshot
	public static int versionRelation(BasicMessage clientMessage) { 
		
		
		// SnapshotIDjevi svih servenata koji mogu biti inicijatori Snapshota u sistemu, pristigli u LY_MARKERU
		Map<Integer, Integer> recivedInitiatorsVersion = clientMessage.initiatorsVersion;
		
		// Prolazimo kroz svoje intiatorsVersion
		for (Map.Entry<Integer, Integer> initiatorVersion: initiatorsVersion.entrySet()) {
			
			Integer initiator = initiatorVersion.getKey();
			Integer localInitiatorVersion = initiatorVersion.getValue();
			
			Integer recivedInitiatorVersionVersion = recivedInitiatorsVersion.get(initiator);
			
			// Ako je snapshotID za pristiglog serventa veca od verzije za tog serventa koju ja imam na sistemu, onda postoji razlika izmedju verzija
			if (recivedInitiatorVersionVersion > localInitiatorVersion) {
//				AppConfig.timestampedErrorPrint("Nasao sam razliku u verzijama na inicijatoru " + initiator.toString());
				
				// Ako ima razlike izmedju pristiglih SnapshotID-jeva i SnapshotID-jeva na lokalnom sistemu, onda vracamo serventID serventa za koji smo pronasli razliku
				return Integer.parseInt(initiator.toString());
			}
		}
		
		// Ako nema razlike izmedju pristiglih verzija snapshotID-jeva i onih na lokalnom sistemu
		return -1;
	}
	
	
}
