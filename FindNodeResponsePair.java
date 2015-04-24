/**
 * Class which denotes the typical response type of the find_node query made.
 * 
 * @author rkandur
 *
 */
public class FindNodeResponsePair {

	// true node on which wither lookup or insert operation should be done 
	Node.NodeInfo node_;
	// response of find_node if applicable
	String response_;
	
	public FindNodeResponsePair(Node.NodeInfo n, String resp) {
		node_ = n;
		response_ = resp;
	}
	
}