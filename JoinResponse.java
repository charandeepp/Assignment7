import java.io.Serializable;

/**
 * Created by Charandeep on 4/21/15.
 */
public class JoinResponse implements Serializable{

	private static final long serialVersionUID = 1L;

	public enum Status{
        BUSY,DONE
    }

    public Status status;
    public Node.NodeInfo newNodeInfo;
    public Node.NodeInfo successor;
    public Node.NodeInfo predecessor;
    public String[] fingerTable;
}
