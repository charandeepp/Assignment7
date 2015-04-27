import java.io.Serializable;
import java.math.BigInteger;

/**
 * Class which denotes the typical response type of the find_node query made.
 * 
 * @author rkandur
 *
 */
public class FindNodeResponsePair implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// true node on which wither lookup or insert operation should be done 
	BigInteger nodeId_;
	Integer nodeNum_;
	String nodeUrl_;
	// response of find_node if applicable
	String response_;
	
	public FindNodeResponsePair(BigInteger nodeId, Integer nodeNum,
			String nodeUrl, String resposne) {
		nodeId_ = nodeId;
		nodeNum_ = nodeNum;
		nodeUrl_ = nodeUrl;
		response_ = resposne;
	}
	
}
