import java.io.Serializable;

/**
 * Created by Charandeep on 4/21/15.
 * 
 * Class which denotes the typical response received from server 
 * when a Join request is made
 */
public class JoinResponse implements Serializable{

	private static final long serialVersionUID = 1L;

	public enum Status{
        BUSY,
        DONE, 
        ERROR,
        NONE;
    }

    public Status status;
    public String response;
    public Node.NodeInfo newNodeInfo;
    public Node.NodeInfo successor;
    public Node.NodeInfo predecessor;
    public String[] fingerTable;

    public JoinResponse() {
    	status = Status.NONE;
    	response = new String();
    }
    
    public JoinResponse(Status s, String r) {
    	status = s;
    	response = r;
    }
    
}
