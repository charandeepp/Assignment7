import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utils method which has some helper methods
 * 
 * @author rkandur
 *
 */
public class Utils {

	//TODO: do we still need this?
	public static String sha1String(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

	/*
	 * method which gives the hashed value of the key using SHA1
	 */
    public static BigInteger sha1BigInt(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        BigInteger bi = new BigInteger(1, result);
        return bi;
    }
    
    /*
     * method to compute base^exponent
     */
    public static BigInteger power(Integer base, Integer exponent){
        BigInteger result = BigInteger.ONE;
        for(int i=1;i<=exponent;i++){
            result = result.multiply(BigInteger.valueOf(base));
        }
        return result;
    }
	
}
