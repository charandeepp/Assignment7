import java.net.URL;
import java.rmi.Remote;

/**
 * Created by Charandeep on 4/21/15.
 */
public interface ChordInterface extends Remote{

    public JoinResponse join(URL newNode);
    public void insertKey();
    public URL successor();
    public String lookup(String word);
    public void join_done(URL newNode);
}
