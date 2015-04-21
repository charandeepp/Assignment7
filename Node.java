

/**
 * Created by Charandeep on 4/21/15.
 */


import org.omg.CORBA.INTERNAL;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

public class Node implements ChordInterface{


    public class NodeInfo implements Serializable{

        public String nodeURL;
        public String nodeId;
        public Integer nodeNum;

        public NodeInfo(String url,String id,Integer num){
            nodeURL = url;
            nodeId = id;
            nodeNum = num;
        }

    }

    private NodeInfo myInfo;
    private NodeInfo mySuccessor = null;
    private NodeInfo myPredecessor = null;
    private Integer globalNodeCount = 0;

    private Hashtable<String,String> dictionary_;
    private Hashtable<Integer,NodeInfo> fingerTable_;

    private boolean joinLock = false;
    public Node(String url){

        dictionary_ = new Hashtable<String,String>();
        fingerTable_ = new Hashtable<Integer,NodeInfo>();


    }
    @Override
    public JoinResponse join(String url){
        JoinResponse result = null;
        NodeInfo successor;
        if(joinLock==false){
            result = new JoinResponse();
            result.status = JoinResponse.Status.BUSY;
        }
        else{
            joinLock = false;
            String hashKey = null;
            try {
                hashKey = sha1(url);
            }
            catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }

            successor = successor(hashKey);
            Registry registry;
            ChordInterface node = null;
            ChordInterface successorNode = null;

            try {
                registry = LocateRegistry.getRegistry();
                successorNode = (ChordInterface) registry.lookup(successor.nodeURL);
                node = (ChordInterface) registry.lookup(url);
            }
            catch(RemoteException e){
                System.out.println(e);
            }
            catch (NotBoundException e){
                System.out.println(e);
            }
            result = new JoinResponse();
            result.status = JoinResponse.Status.DONE;
            result.newNodeInfo.nodeId = hashKey;
            result.newNodeInfo.nodeNum = globalNodeCount;
            globalNodeCount++;
            result.successor = successor;
            result.predecessor = predecessor(hashKey);



            successorNode.notify(result.newNodeInfo);



        }
        return result;
    }

    @Override
    public String lookup(String word){
        String meaning = null;

        return meaning;
    }

    @Override
    public NodeInfo successor(String id) {
        //if()
        return null;
    }

    @Override
    public void join_done(NodeInfo newNode) {

        joinLock = true;
    }

    @Override
    public void insertKey() {

    }

    @Override
    public void notify(NodeInfo predecessor) {
        if(myPredecessor == null || ((predecessor.nodeId.compareTo(myPredecessor.nodeId) <0) && (predecessor.nodeId.compareTo(myInfo.nodeId)>0 || myInfo.nodeNum==0)))
            myPredecessor = predecessor;
    }

    /*
    @Override
    public void updateSuccessor(NodeInfo successor) {
        mySuccessor = successor;
    }*/

    public void setDictionary_(Hashtable dictionary_) {
        this.dictionary_ = dictionary_;
    }

    public Hashtable getDictionary_() {
        return dictionary_;
    }

    public void setFingerTable_(){

    }

    public Hashtable getFingerTable_(){

        return fingerTable_;

    }

    static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

}
