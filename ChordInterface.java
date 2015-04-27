import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

/**
 * Created by Charandeep on 4/21/15.
 */
public interface ChordInterface extends Remote{

    public NodeInfo getMyInfo() throws RemoteException;
   
    public JoinResponse join(String url) throws RemoteException;
    public void join_done(NodeInfo newNode) throws RemoteException;
    public FindNodeResponsePair find_node(String key, boolean withTrace) throws RemoteException;
    
    public String lookup(String word) throws RemoteException;
    public NodeInfo predecessor(BigInteger id) throws RemoteException;
    public NodeInfo getThisPredecessor() throws RemoteException;
    public NodeInfo getThisSuccessor() throws RemoteException;
    public NodeInfo successor(BigInteger id) throws RemoteException;
    
    public void insertKey(String word, String meaning) throws RemoteException;
    public void removeKey(String word) throws RemoteException;
    public void updateSuccessor(NodeInfo successor) throws RemoteException;
    public void notify(NodeInfo predecessor) throws RemoteException;
    
    public void fixFingers() throws RemoteException;
    public Hashtable<String, String> getKeyStore()  throws RemoteException;
    public String printRingStructure() throws RemoteException;
	public String getFormattedNodeDetails() throws RemoteException;
    
}
