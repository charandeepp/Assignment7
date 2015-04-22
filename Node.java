

/**
 * Created by Charandeep on 4/21/15.
 */

import java.io.Serializable;
import java.math.BigInteger;
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
        public BigInteger nodeId;
        public Integer nodeNum;

        public NodeInfo(String url,BigInteger id,Integer num){
            nodeURL = url;
            nodeId = id;
            nodeNum = num;
        }

    }

    static String[] nodeURLs ={"node1","node2","node3","node4"};

    private NodeInfo myInfo;
    private NodeInfo mySuccessor = null;
    private NodeInfo myPredecessor = null;
    private Integer globalNodeCount = 0;

    private Hashtable<String,String> dictionary_;
    private String[] fingerTable_;

    private boolean joinLock = false;
    public Node(String url){

        dictionary_ = new Hashtable<String,String>();
        fingerTable_ = new String[160];

        try {
            if (url.compareTo(nodeURLs[0]) == 0) {
                myInfo = new NodeInfo(url, sha1BigInt(url), 0);
            }
            else{
                Registry registry = LocateRegistry.getRegistry();
                ChordInterface mainNode = (ChordInterface) registry.lookup(nodeURLs[0]);
                JoinResponse joinResponse = mainNode.join(url);
                if(joinResponse.status == JoinResponse.Status.BUSY){
                    System.out.println("Node-0 is busy! Kill and reconnect after sometime.");
                }
                else{
                    myInfo = joinResponse.newNodeInfo;
                    myPredecessor = joinResponse.predecessor;
                    mySuccessor = joinResponse.successor;
                    fingerTable_ = joinResponse.fingerTable;
                }
            }
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        catch(RemoteException e){
            e.printStackTrace();
        }
        catch (NotBoundException e){
            e.printStackTrace();
        }


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
            BigInteger hashKey = null;
            try {
                hashKey = sha1BigInt(url);
            }
            catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }

            successor = successor(hashKey);
            Registry registry;
            ChordInterface successorNode = null;

            try {
                registry = LocateRegistry.getRegistry();
                successorNode = (ChordInterface) registry.lookup(successor.nodeURL);
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
            result.fingerTable = fixFingers(hashKey);
            successorNode.notify(result.newNodeInfo);
        }
        return result;
    }

    @Override
    public String lookup(String word){
        String meaning = dictionary_.get(word);
        return meaning;
    }

    @Override
    public NodeInfo successor(BigInteger id) {
        for(int i=0;i<160;i++){
            int compare_value = id.subtract(myInfo.nodeId).compareTo(power(2,i));

            if(compare_value >0) {
                continue;
            }
            else if(compare_value < 0){
                Registry registry;
                ChordInterface successorNode = null;

                try {
                    registry = LocateRegistry.getRegistry();
                    successorNode = (ChordInterface) registry.lookup(fingerTable_[i-1]);
                }
                catch(RemoteException e){
                    System.out.println(e);
                }
                catch (NotBoundException e){
                    System.out.println(e);
                }
                return successorNode.successor(id);
            }
            else{
                Registry registry;
                ChordInterface successorNode = null;

                try {
                    registry = LocateRegistry.getRegistry();
                    successorNode = (ChordInterface) registry.lookup(fingerTable_[i]);
                }
                catch(RemoteException e){
                    System.out.println(e);
                }
                catch (NotBoundException e){
                    System.out.println(e);
                }
                return successorNode.getMyInfo();
            }

        }
        return null;
    }

    @Override
    public void join_done(NodeInfo newNode) {
        joinLock = true;
    }

    @Override
    public void insertKey(String word, String meaning) {
        dictionary_.put(word,meaning);
    }

    @Override
    public void notify(NodeInfo predecessor) {
        if(myPredecessor == null || ((predecessor.nodeId.compareTo(myPredecessor.nodeId) <0) && (predecessor.nodeId.compareTo(myInfo.nodeId)>0 || myInfo.nodeNum==0)))
            myPredecessor = predecessor;
    }

    @Override
    public NodeInfo predecessor(BigInteger id) {
        return null;
    }

    @Override
    public NodeInfo getMyInfo(){
        return myInfo;
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

    public String[] getFingerTable_(){

        return fingerTable_;

    }

    public String sha1String(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public BigInteger sha1BigInt(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        BigInteger bi = new BigInteger(1, result);
        return bi;
    }

    public String[] fixFingers(BigInteger id){

        String[] fingerTable = new String[160];

        for(int i=0;i<160;i++){
            fingerTable[i] = successor(id.add(power(2,i))).nodeURL;
        }

        return fingerTable;
    }

    public BigInteger power(Integer base, Integer exponent){

        BigInteger result = BigInteger.ONE;

        for(int i=1;i<=exponent;i++){
            result = result.multiply(BigInteger.valueOf(base));
        }

        return result;

    }
}
