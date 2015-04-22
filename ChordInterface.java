import java.math.BigInteger;
import java.rmi.Remote;

/**
 * Created by Charandeep on 4/21/15.
 */
public interface ChordInterface extends Remote{

    public Node.NodeInfo getMyInfo();
    public JoinResponse join(String url);
    public void insertKey(String word, String meaning);
    public Node.NodeInfo successor(BigInteger id);
    public String lookup(String word);
    public void join_done(Node.NodeInfo newNode);
    public void notify(Node.NodeInfo predecessor);
    //public void updateSuccessor(Node.NodeInfo successor);
    public Node.NodeInfo predecessor(BigInteger id);
}
