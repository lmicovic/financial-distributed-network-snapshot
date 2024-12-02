package app.snapshot_bitcake;

import java.util.Map;

import app.Cancellable;

/**
 * Describes a snapshot collector. Made not-so-flexibly for readability.
 * 
 * @author bmilojkovic
 *
 */
public interface SnapshotCollector extends Runnable, Cancellable {

	BitcakeManager getBitcakeManager();

	void addLYSnapshotInfo(int id, 	Map<Integer, Map<Integer, LYSnapshotResult>> versionsSomethingSomething);

	void startCollecting();

}