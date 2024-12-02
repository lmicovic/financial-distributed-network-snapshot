package app.snapshot_bitcake;

import java.util.Map;

/**
 * This class is used if the user hasn't specified a snapshot type in config.
 * 
 * @author bmilojkovic
 *
 */
public class NullSnapshotCollector implements SnapshotCollector {

	@Override
	public void run() {}

	@Override
	public void stop() {}

	@Override
	public BitcakeManager getBitcakeManager() {
		return null;
	}


	@Override
	public void startCollecting() {}

	@Override
	public void addLYSnapshotInfo(int id, Map<Integer, Map<Integer, LYSnapshotResult>> versionsSomethingSomething) {
		// TODO Auto-generated method stub
		
	}

}
