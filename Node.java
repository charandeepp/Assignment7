

/**
 * Created by Charandeep on 4/21/15.
 * 
 * class which denotes a specific node on the Chord ring
 * 
 */

import java.math.BigInteger;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * 
 * @author rkandur
 * TODO:
 * 1. synchronization among read and write aspects?
 *
 */
public class Node implements ChordInterface{

    private static String MASTER_NODE_URL;
    private NodeInfo myInfo_;
    private NodeInfo mySuccessor_ = null;
    private NodeInfo myPredecessor_ = null;
    private Integer globalNodeCount_ = 0;

    private Hashtable<String,String> dictionary_;
    private String[] fingerTable_;
    private boolean joinLock_ = true;
    
    private ChordInterface masterNode_;
    private static Logger logger;
    private boolean isMasterNode_ = Boolean.FALSE;
    
    public Node(String url){

    	logger = ServerLogger.logger(url.hashCode());
        dictionary_ = new Hashtable<String,String>();
        fingerTable_ = new String[160];
        Arrays.fill(fingerTable_, "");

        try {
            if (url.equals(MASTER_NODE_URL)) {
            	isMasterNode_ = true;
                myInfo_ = new NodeInfo(url, Utils.sha1BigInt(url), 0);
                myPredecessor_ = this.myInfo_;
                mySuccessor_ = this.myInfo_;
                masterNode_ = this;
            }
            else{
            	isMasterNode_ = false;
                Registry registry = LocateRegistry.getRegistry();
                masterNode_ = (ChordInterface) registry.lookup(MASTER_NODE_URL);
                JoinResponse joinResponse = masterNode_.join(url);
                if(joinResponse.status == JoinResponse.Status.BUSY){
                    logger.info("Node-0 is busy! Kill and reconnect after sometime.");
                }
                else{
                    myInfo_ = joinResponse.newNodeInfo;
                    myPredecessor_ = joinResponse.predecessor;
                    mySuccessor_ = joinResponse.successor;
                    fingerTable_ = joinResponse.fingerTable;
                    masterNode_.join_done(myInfo_);
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

    /*
     * used to join a new node in the Chord ring
     */
    @Override
    public JoinResponse join(String url) throws RemoteException {
    	
        if(joinLock_==false){
        	logger.info("Node is Busy, Please try later !");
        	return new JoinResponse(JoinResponse.Status.BUSY, "Node is Busy, Please try later !");
        }
       
        joinLock_ = false;
        StringBuilder response = new StringBuilder();
        
        BigInteger hashKey = null;
        try {
            hashKey = Utils.sha1BigInt(url);
            response.append("Computed hash {" + hashKey + "} for new node with URL {" + url + "}")
            		.append(System.getProperty("line.separator"));
        }
        catch (NoSuchAlgorithmException e){
        	response.append(e.getMessage());
        	logger.severe(response.toString());
        	return new JoinResponse(JoinResponse.Status.ERROR, response.toString());
        }

        NodeInfo successor = successor(hashKey);
        
        response.append("Updating the URL of the new node to {" + url + "}")
				.append(System.getProperty("line.separator"));
        
        response.append("Updating the nodeID of the new node to {" + hashKey + "}")
				.append(System.getProperty("line.separator"));
        
        globalNodeCount_++;
        Integer gc = globalNodeCount_;
        
        response.append("Updating the successor of the new node to {" + successor.nodeURL_ + "}")
        		.append(System.getProperty("line.separator"));
        
        NodeInfo pred = predecessor(hashKey);
        response.append("Updating the predecessor of the new node to {" + pred.nodeURL_ + "}")
				.append(System.getProperty("line.separator"));
        
        String[] ft = computeFingerTableFor(hashKey);
        response.append("Updating the finger table of the new node to ").append(ft.toString())
				.append(System.getProperty("line.separator"));
        
        // we should update the predecessor information in the new node's successor
        ChordInterface successorNode;
        NodeInfo nodeInfo;
        try {
        	Registry registry = LocateRegistry.getRegistry();
            successorNode = (ChordInterface) registry.lookup(successor.nodeURL_);
            nodeInfo = new NodeInfo(url, hashKey, gc);
            successorNode.notify(nodeInfo);
            response.append("Updating the predecessor of the node following the new node {")
					.append(successor.nodeURL_).append("}.")
					.append(System.getProperty("line.separator"));
        }
        catch(Exception e){
        	response.append(e.getMessage());
        	logger.severe(response.toString());
        	return new JoinResponse(JoinResponse.Status.ERROR, response.toString());
        }
        
        // we should also update the successor pointer of the new node's predecessor
        ChordInterface predecessorNode;
        try {
        	Registry registry = LocateRegistry.getRegistry();
            predecessorNode = (ChordInterface) registry.lookup(pred.nodeURL_);
            predecessorNode.updateSuccessor(nodeInfo);
			response.append("Updating the successor of the node preceding the new node {")
					.append(pred.nodeURL_).append("}.")
					.append(System.getProperty("line.separator"));
        }
        catch(Exception e){
        	response.append(e.getMessage());
        	logger.severe(response.toString());
        	return new JoinResponse(JoinResponse.Status.ERROR, response.toString());
        }
		
		// we need to reorganize the finger table and the keys information
		// after the new node joins
        System.out.println(response.toString());
        redistributeFingerTables(response, predecessorNode.getMyInfo(), gc);
        
        response.append("Redistributing the keys after the new node has joined !")
				.append(System.getProperty("line.separator"));
        redistributeKeys(successorNode);
        
        response.append("Updating the status of the response to DONE")
				.append(System.getProperty("line.separator"));
        
        logger.info(response.toString());
        
        return new JoinResponse(JoinResponse.Status.DONE, response.toString(), nodeInfo, successor, pred, ft);
    }

    public void redistributeFingerTables(StringBuilder response, NodeInfo startNodeInfo, int iteration) {
    	
    	if(iteration <= 0) {
    		return;
    	}

    	System.out.println("In redistr of " + startNodeInfo.nodeURL_);
    	// we will just be adjusting the fingers of all the nodes in the ring
		try {
			
			Registry registry = LocateRegistry.getRegistry();
			ChordInterface startNode = (ChordInterface) registry.lookup(startNodeInfo.nodeURL_);
			System.out.println("Found start node !!");
			for(int i=1;i<160;i++){
				ChordInterface succesorNode = (ChordInterface) registry.lookup(startNode.getThisSuccessor().nodeURL_);
				System.out.println("Successor is " + succesorNode.getMyInfo().nodeURL_);
				succesorNode.successor(startNodeInfo.nodeId_.add(Utils.power(2, i)));
			}
			response.append("Updating the finger table of the node {")
					.append(startNodeInfo.nodeURL_).append("}")
					.append(System.getProperty("line.separator"));

			redistributeFingerTables(response, startNode.getThisPredecessor(), --iteration);
				
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
        
    }
    
    public void redistributeKeys(ChordInterface successor) {
    	
		try {

			// after a new node has joined, keys which earlier belonged to this
			// node might belong to the next node, i.e., the successor in the
			// ring. We need to slide these keys to the next node.
			// Since only the keys which are neighbors to the new node might
			// need keys to be redistributed, we can just do this for them
			Registry registry = LocateRegistry.getRegistry();
			ChordInterface newNode = (ChordInterface) registry.lookup(successor.getThisPredecessor().nodeURL_);
			
			Hashtable<String, String> keyStore = successor.getKeyStore();
			HashMap<String, String> keysToBeMoved = new HashMap<String, String>();
			for(String key : keyStore.keySet()) {
				// we should move only if the key value is < newNodeId
				if(Utils.sha1BigInt(key).compareTo(newNode.getMyInfo().nodeId_) < 0) {
					keysToBeMoved.put(key, keyStore.get(key));
				}
			}
			for(String key: keysToBeMoved.keySet()) {
				newNode.insertKey(key, keysToBeMoved.get(key));
				successor.removeKey(key);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        
    }
    
    @Override
    public String lookup(String word) throws RemoteException {
        return dictionary_.get(word);
    }

    @Override
    public NodeInfo successor(BigInteger id) throws RemoteException {
    	
    	System.out.println("Successor for id " + id);
    	
        for(int i=0;i<fingerTable_.length; i++){
        	
        	if(fingerTable_[i].isEmpty()) {
        		System.out.println("Returning self Info");
        		return myInfo_;
        	}
        	
        	if(myInfo_.nodeId_ == getThisSuccessor().nodeId_){
        		System.out.println("Returning successor");
        		return myInfo_;
        	}
        	
        	BigInteger value = (id.subtract(myInfo_.nodeId_).compareTo(BigInteger.ZERO) < 0) ? 
        			Utils.power(2, 160).add(id.subtract(myInfo_.nodeId_)): id.subtract(myInfo_.nodeId_);
            int compare_value = value.compareTo(Utils.power(2,i));
            System.out.println("Compare Value = " + compare_value);

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
                System.out.println("In > 0 and calling successor on" + successorNode.getMyInfo().nodeURL_);
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
                
                System.out.println("In = 0 and calling returning successor " + successorNode.getMyInfo().nodeURL_);
                return successorNode.getMyInfo();
            }

        }
        return null;
    }

    @Override
    public void join_done(NodeInfo newNode) throws RemoteException {
    	if(!isMasterNode_) {
    		logger.severe("I am not a master to serve join_done request !!!");
    		return;
    	}
        joinLock_ = true;
    }

    @Override
    public void insertKey(String word, String meaning) throws RemoteException {
        dictionary_.put(word,meaning);
    }
    
    @Override
    public void removeKey(String word) throws RemoteException {
        dictionary_.remove(word);
    }

    @Override
    public void notify(NodeInfo predecessor) throws RemoteException {
		if (myPredecessor_ == null || 
				((predecessor.nodeId_.compareTo(myPredecessor_.nodeId_) < 0) && 
				(predecessor.nodeId_.compareTo(myInfo_.nodeId_) > 0 || myInfo_.nodeNum_ == 0))) {
            myPredecessor_ = predecessor;
		}
    }

    @Override
    public NodeInfo predecessor(BigInteger id) throws RemoteException {
		try {
			NodeInfo successor = successor(id);
	    	Registry registry = LocateRegistry.getRegistry();
	    	ChordInterface predecessorNode = (ChordInterface) registry.lookup(successor.nodeURL_);
	        return predecessorNode.getThisPredecessor();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
        return null;
    }

    @Override
    public NodeInfo getMyInfo() throws RemoteException {
        return myInfo_;
    }
    
    @Override
    public Hashtable<String, String> getKeyStore() throws RemoteException {
    	return dictionary_;
    }
    
    @Override
    public void updateSuccessor(NodeInfo successor) throws RemoteException {
        mySuccessor_ = successor;
        fingerTable_[0] = successor.nodeURL_;
    }
    
    @Override
    public NodeInfo getThisPredecessor() throws RemoteException {
    	return myPredecessor_;
    }
    
    @Override
    public NodeInfo getThisSuccessor() throws RemoteException {
    	return mySuccessor_;
    }

    @Override
    public void fixFingers() throws RemoteException {
    	for(int i = 0; i < fingerTable_.length; ++i){
            fingerTable_[i] = successor(getMyInfo().nodeId_.add(Utils.power(2,i))).nodeURL_;
        }
    }
    
    @Override
    public FindNodeResponsePair find_node(String key, boolean needTrace) throws RemoteException {
    	
    	if(!isMasterNode_) {
    		logger.severe("I am not a master to serve find_node request !!!");
    		return null;
    	}
    	
    	StringBuilder response = new StringBuilder();
    	NodeInfo successor = null;
    	
    	try {
    		if(needTrace) response.append("Finding the true node which holds data for key {" + key +"}")
    				.append(System.getProperty("line.separator"));
			
    		System.out.println("finfing successor for " + Utils.sha1BigInt(key));
    		successor = masterNode_.successor(Utils.sha1BigInt(key));
    		if(successor == null) {
    			System.out.println("Successor is NULL !!!!!!!!!!!!!!!");
    		}
    		if(needTrace) response.append("True node holding data for key {" + key +"} is {" + successor.nodeURL_ + "}")
					.append(System.getProperty("line.separator"));
    		
    		logger.info("True node holding data for key {" + key +"} is {" + successor.nodeURL_ + "}");
    		
    	} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			if(needTrace) response.append(e.getMessage())
					.append(System.getProperty("line.separator"));
		}
    	
    	return new FindNodeResponsePair(successor.nodeId_, successor.nodeNum_, successor.nodeURL_, response.toString());
    	
    }
    
    @Override
	public String getFormattedNodeDetails() throws RemoteException {
    	
    	// TODO: .append(160 bit hex key).append(","). are we capturing all the
		// information that we are supposed to print ??
    	String keyInHex = new BigInteger(new StringBuilder()
				.append(getMyInfo().nodeId_).toString(), 16).toString();
		
		StringBuilder fd = new StringBuilder();
		try {
			fd.append("Node ID: ").append(getMyInfo().nodeNum_).append(", ")
					.append("URL: ").append(getMyInfo().nodeURL_).append(", ")
					.append("160-bit key: ").append(Utils.sha1String(getMyInfo().nodeURL_)).append(", ")
					.append("Successor: ").append(getThisSuccessor().nodeURL_).append(", ")
					.append("Predecessor: ").append(getThisPredecessor().nodeURL_).append(", ")
					.append("Number of Entries: ").append(getKeyStore().size());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return fd.toString();
	}
   
    public String printRingStructure() throws RemoteException {
    	
    	// we will accumulate information from all nodes and then print the ring structure
    	StringBuilder ringStructure = new StringBuilder();
    	try {
			
			ringStructure.append(masterNode_.getFormattedNodeDetails())
					.append(System.getProperty("line.separator"));
			
			Registry registry = LocateRegistry.getRegistry();			
			NodeInfo currNodeInfo = masterNode_.getThisSuccessor();
			
			while(!currNodeInfo.nodeId_.equals(masterNode_.getMyInfo().nodeId_)) {
				
				ChordInterface currNode = (ChordInterface) registry.lookup(currNodeInfo.nodeURL_);
				ringStructure.append(currNode.getFormattedNodeDetails())
							 .append(System.getProperty("line.separator"));
				currNodeInfo = currNode.getThisSuccessor();
				
			}
			logger.info("Ring structure is -> " + ringStructure.toString());
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
    	return ringStructure.toString();
    }
    
    public String[] computeFingerTableFor(BigInteger id) throws RemoteException {
        String[] fingerTable = new String[160];
        for(int i=0;i<160;i++){
            fingerTable[i] = successor(id.add(Utils.power(2,i))).nodeURL_;
        }
        return fingerTable;
    }

    public static void main(String[] args) {

    	if(args.length != 2) {
    		System.out.println("Incorrect number of arguments. Please provide the following two arguments <currentNodeURL> <masterNodeURL>");
    		return;
    	}
    	
    	String nodeURL = args[0].trim();
    	MASTER_NODE_URL = args[1].trim();
    	
        ChordInterface node = new Node(nodeURL);
        try {
            ChordInterface stub = (ChordInterface) UnicastRemoteObject.exportObject(node, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(nodeURL, stub);

        }
        catch (RemoteException e){
            e.printStackTrace();
        }

    }
}
