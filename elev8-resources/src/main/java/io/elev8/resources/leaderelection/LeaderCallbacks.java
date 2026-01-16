package io.elev8.resources.leaderelection;

/**
 * Callbacks for leader election events.
 * Implement this interface to handle leadership transitions.
 */
public interface LeaderCallbacks {

    /**
     * Called when this instance becomes the leader.
     * This is called in a separate thread and should contain the main work loop.
     * When this method returns, the leader election will attempt to release leadership.
     */
    void onStartLeading();

    /**
     * Called when this instance stops being the leader.
     * This is called when leadership is lost, either due to lease expiration,
     * explicit release, or shutdown. It may be called even if onStartLeading
     * has not returned yet.
     */
    void onStopLeading();

    /**
     * Called when a new leader is elected (including this instance).
     *
     * @param identity the identity of the new leader
     */
    void onNewLeader(String identity);
}
