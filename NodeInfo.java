import java.io.Serializable;
import java.math.BigInteger;

public class NodeInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	public String nodeURL_;
    public BigInteger nodeId_;
    public Integer nodeNum_;

    public NodeInfo(String url,BigInteger id,Integer num){
        nodeURL_ = url;
        nodeId_ = id;
        nodeNum_ = num;
    }

}