package listener;

/**
 * Created by varuna on 12/18/2017.
 */

public interface ActiveSyncListener {
    public boolean isSyncHappening(Object context);
    public void setSyncHappening(boolean happening, Object context);
}
