import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * 
 * @author rkandur
 *
 */
public class Client {

	private static final String WORDS_FILE_PATH = "words.txt";
	private static String NODE0_URL = "node0";
	private static Logger logger = ClientLogger.logger();
	
	private static HashMap<String, String> wordMeaningStore_ = new HashMap<String, String>();

	public static BigInteger sha1BigInt(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        BigInteger bi = new BigInteger(1, result);
        return bi;
    }
	
	public static void loadWordMeaningPairs() {
		try {
			File file = new File(WORDS_FILE_PATH);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] toks = line.trim().split(":");
				if (toks.length != 2) {
					continue;
				}
				wordMeaningStore_.put(toks[0].trim(), toks[1].trim());
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void insertWordsInDHT(ChordInterface node0, Registry registry) {

		for(String word : wordMeaningStore_.keySet()) {
			try {
				Node.NodeInfo successor = node0.successor(sha1BigInt(word));
				try {
					ChordInterface insertNode = (ChordInterface) registry.lookup(successor.nodeURL);
					// TODO: insertKey should have an optional parameter to get a full log trace.
					insertNode.insertKey(word, wordMeaningStore_.get(word));
				} catch (RemoteException | NotBoundException e) {
					logger.severe("Could not insert word {" + word + "} in DHT.");
				}
			} catch (NoSuchAlgorithmException e) {
				logger.severe("Could not find hash of the word {" + word + "}");
			}
		}
        
	}
	
	private static void lookupinDHT(String word, ChordInterface node0, Registry registry) {

		String meaning = new String();
		
		try {
			Node.NodeInfo successor = node0.successor(sha1BigInt(word));
			ChordInterface lookupNode = (ChordInterface) registry.lookup(successor.nodeURL);
			// TODO: lookup should have an optional parameter to get a full log trace.
			meaning = lookupNode.lookup(word);
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		if(meaning == null || meaning.isEmpty()) {
			logger.severe("Could not find any meaning for the word {" + word + "}");
		} else {
			logger.info("Meaning of word {" + word + "} is {" + meaning +"}");
		}
	}

	public static void main(String[] args) {
		
		System.setSecurityManager(new RMISecurityManager());
		Registry registry;
		ChordInterface node0;
		try {
			registry = LocateRegistry.getRegistry();
			node0 = (ChordInterface) registry.lookup(NODE0_URL);
		} catch (RemoteException e1) {
			logger.severe("Could not locate remote RMI registry, Exiting !!!");
			return;
		} catch (NotBoundException e) {
			logger.severe("Could not find any binding with name {" + NODE0_URL + "}, Exiting !!!");
			return;
		}
		
		// load all the dictionary words
		loadWordMeaningPairs();
		
		// insert dictionary words and meanings into DHT
		insertWordsInDHT(node0, registry);
		
		String continueLooping = "y";
		String lookupWord = new String();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		do {
			
			System.out.println("Enter the word to lookup:");
			try {
				lookupWord = br.readLine();
			} catch (IOException e) {
				lookupWord = new String();
			}
			
			lookupinDHT(lookupWord, node0, registry);
			
			System.out.println("Do you want to continue ? (y/n)");
			try {
				continueLooping = br.readLine();
			} catch (IOException e) {
				continueLooping = "n";
			}
			
		} while(continueLooping.equals("y") || continueLooping.equals("Y"));
		
	}

}
