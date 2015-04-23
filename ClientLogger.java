
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Logger class which is used for logging Client related logs
 * 
 * @author rkandur
 *
 */
public class ClientLogger {

	private static Logger logger_ = null;
	private static String FILE_NAME = "clientLogfile";
	
	private ClientLogger() {
		
	}
	
	public static Logger logger() {
	
		/**
		 * Configure the logger used for logging client information
		 */
		if(logger_ == null) {
			
			logger_ = Logger.getLogger(ClientLogger.class.getSimpleName());
			FileHandler fh;
		    try {
		        fh = new FileHandler(FILE_NAME);
		        logger_.addHandler(fh);
		        SimpleFormatter formatter = new SimpleFormatter();
		        fh.setFormatter(formatter);
		    } catch (SecurityException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		
		return logger_;
	}
	
}
