import java.io.Serializable;
import java.util.Hashtable;

/**
 * Created by Charandeep on 4/21/15.
 */
public class JoinResponse implements Serializable{

    public enum Status{
        BUSY,DONE
    }

    public Status status;
    public Node.NodeInfo newNodeInfo;
    public Node.NodeInfo successor;
    public Node.NodeInfo predecessor;
    public String[] fingerTable;
}
