import java.io.Serializable;
import java.net.URL;

/**
 * Created by Charandeep on 4/21/15.
 */
public class JoinResponse implements Serializable{

    public Integer key;
    public URL successor;
    public URL predecessor;

}
