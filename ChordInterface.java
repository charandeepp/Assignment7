import java.math.BigInteger;
import java.rmi.Remote;
import java.util.Hashtable;

/**
 * Created by Charandeep on 4/21/15.
 */
public interface ChordInterface extends Remote{

    public Node.NodeInfo getMyInfo();
   
    public JoinResponse join(String url);
    public void join_done(Node.NodeInfo newNode);
    public FindNodeResponsePair find_node(String key, boolean withTrace);
    
    public String lookup(String word);
    public Node.NodeInfo predecessor(BigInteger id);
    public Node.NodeInfo getThisPredecessor();
    public Node.NodeInfo getThisSuccessor();
    public Node.NodeInfo successor(BigInteger id);
    
    public void insertKey(String word, String meaning);
    public void removeKey(String word);
    public void updateSuccessor(Node.NodeInfo successor);
    public void notify(Node.NodeInfo predecessor);
    
    public void fixFingers();
    public Hashtable<String, String> getKeyStore();
    public void printRingStructure();
	public String getFormattedNodeDetails();
    
}
