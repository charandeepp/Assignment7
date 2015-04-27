
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Logger class which is used for logging {@link IBankServer} related logs
 * 
 * @author rkandur
 *
 */
public class ServerLogger {

	private static String FILE_NAME = "serverLogfile";
	
	private static Logger logger_ = null;
	private static FileHandler fh_;
	
	private ServerLogger() {
		
	}
	
	public static Logger logger(int pId) {
	
		/**
		 * Configure the logger used for logging server information
		 */
		if(logger_ == null) {
			
			logger_ = Logger.getLogger(ServerLogger.class.getSimpleName());
		    try {
		        fh_ = new FileHandler(new StringBuilder().append(FILE_NAME).append(pId).toString());
		        logger_.addHandler(fh_);
		        SimpleFormatter formatter = new SimpleFormatter();
		        fh_.setFormatter(formatter);
		    } catch (SecurityException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		
		return logger_;
	}
	
	public static void closeLogFile() {
		fh_.close();
	}
	
}
