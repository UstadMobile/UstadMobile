package listener;

/**
 * Created by varuna on 7/31/2017.
 */

public interface ActiveUserListener {
    public void userChanged(String username, Object context);
    public void credChanged(String cred, Object context);
}
