package logicBox;

import java.util.Map;

/**
 * Model class for representing lock state in Firebase Realtime Database
 * This model ensures proper synchronization across multiple clients
 */
public class LockState {
    private boolean isLocked;
    private String lastUpdatedBy;
    private long lastUpdatedTimestamp;
    private String status; // "locked", "unlocked", "operating"
    private String operationInProgress; // userId of user currently operating the lock

    public LockState() {
        // Default constructor for Firebase deserialization
    }

    public LockState(boolean isLocked, String lastUpdatedBy, long lastUpdatedTimestamp, String status) {
        this.isLocked = isLocked;
        this.lastUpdatedBy = lastUpdatedBy;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.status = status;
    }

    // Getters and Setters
    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOperationInProgress() {
        return operationInProgress;
    }

    public void setOperationInProgress(String operationInProgress) {
        this.operationInProgress = operationInProgress;
    }

    /**
     * Check if this lock state is newer than another state
     * Used for conflict resolution
     */
    public boolean isNewerThan(LockState other) {
        return this.lastUpdatedTimestamp > other.lastUpdatedTimestamp;
    }

    /**
     * Check if lock is currently being operated by another user
     */
    public boolean isBeingOperatedByOther(String currentUserId) {
        return operationInProgress != null && !operationInProgress.isEmpty() && 
               !operationInProgress.equals(currentUserId);
    }
}
