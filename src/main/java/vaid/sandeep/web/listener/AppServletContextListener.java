

package vaid.sandeep.web.listener;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import vaid.sandeep.logging.handler.AsyncFileHandler;

public class AppServletContextListener implements ServletContextListener {
	

	
	private static final String LOGGER_NAME = "vaid.sandeep.logging.handler.AsyncFileHandler";
	private static final Logger log = Logger.getLogger(AsyncFileHandler.class.getName());
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void contextInitialized(ServletContextEvent context) {
		// TODO Auto-generated method stub
		
		 java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
	        rootLogger.setUseParentHandlers(false);
	        Handler[] handlers = rootLogger.getHandlers();

	        Filter filter = new Filter() {
	            public boolean isLoggable(LogRecord record) {
	                if ((record.getMessage() != null)
	                        && (record.getLoggerName().matches(LOGGER_NAME))) {
	                    return false;
	                }
	                return true;
	            }
	        };
	        for (Handler handler : handlers) {
	            
	            handler.setFilter(filter);
	        }
	        


	        startAsyncOrSyncLogger(context, rootLogger);
	       
	              
	        
	        log.warning(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ");
	        

	}
	private boolean startAsyncOrSyncLogger(ServletContextEvent context, java.util.logging.Logger rootLogger) {
        if (Boolean.parseBoolean(System.getProperty("sync.file.logger"))) {
        	System.out.println(" &&&& contextInitialized &&&& Async");
            startAsyncLogger(context, rootLogger);
        } else {
        	System.out.println(" &&&& contextInitialized &&&& Sync ");
            startSyncLogger(context, rootLogger);
        }
        return true;


    }

    private boolean startSyncLogger(ServletContextEvent context, java.util.logging.Logger rootLogger) {
        try {
            String serverName = context.getServletContext().getServerInfo();
            
            String dirPath =  context.getServletContext().getInitParameter("logging-path") + File.separator +serverName
                    + File.separator ;
            File dir = new File(dirPath);
            if (!dir.exists()){
            	dir.mkdirs();
            }
            
            String logFileName = context.getServletContext().getInitParameter("log-file-name");
            FileHandler fileHandler =
                new FileHandler(dirPath+ logFileName,
                        10000000, 10, true) {
                @Override
                public synchronized void publish(LogRecord record) {                	
                    super.publish(record);
                }
            };
            Filter filter = new Filter() {
                public boolean isLoggable(LogRecord record) {
                	
                    if (record.getLevel().intValue() <= Level.INFO.intValue()) {
                    	
                    	System.out.println("LOGGINGG****************************** Sync " + record.getLevel().intValue() +" Level.INFO.intValue() "+ Level.WARNING.intValue());
                        if ((record.getMessage() != null)
                                && (record.getLoggerName().matches(LOGGER_NAME))) {
                            return true;
                        }
                    }

                    return false;
                }
            };
            fileHandler.setFilter(filter);

            Formatter formatter = new Formatter() {
                @Override
                public String format(LogRecord record) {
                    StringBuffer sb = new StringBuffer();

                    java.sql.Timestamp ts = new java.sql.Timestamp(record.getMillis());
                    sb.append("[");
                    sb.append(ts.toString());
                    sb.append("]");
                    sb.append(" ");

                    sb.append(record.getThreadID());
                    sb.append(" ");

                    sb.append(record.getLoggerName());
                    sb.append(" ");

                    // Get the level name and add it to the buffer
                    sb.append(record.getLevel().getName());
                    sb.append(" ");

                    // Get the formatted message (includes localization
                    // and substitution of paramters) and add it to the buffer
                    sb.append(formatMessage(record));
                    sb.append("\n");

                    return sb.toString();
                }
            };
            fileHandler.setFormatter(formatter);
            rootLogger.addHandler(fileHandler);
            //CHECKSTYLE:OFF
        } catch (Exception e) {//CHECKSTYLE:ON
            // Ignore
            return true;
        }

        return true;
    }




    private boolean startAsyncLogger(ServletContextEvent context, java.util.logging.Logger rootLogger) {
        try {
            String serverName = context.getServletContext().getServerInfo();
            
            String maxThread = System.getProperty("async.logger.max.thread");
            int maxThreadCount = getSystemPropertyValue(maxThread, 10);
            String queueSize = System.getProperty("async.logger.queue.size");
            int queueSizeLength = getSystemPropertyValue(queueSize, 10);
            String keepAlive = System.getProperty("async.logger.thread.keepalive.time");
            int keepAliveTime = getSystemPropertyValue(keepAlive, 60);
            
            
            String dirPath =  context.getServletContext().getInitParameter("logging-path") + File.separator +serverName
                    + File.separator ;
            File dir = new File(dirPath);
            if (!dir.exists()){
            	dir.mkdirs();
            }
            
            String logFileName = context.getServletContext().getInitParameter("log-file-name");
            AsyncFileHandler fileHandler =
                    new AsyncFileHandler(dirPath+logFileName, 10000000, 10, true, 2, maxThreadCount, queueSizeLength,
                            keepAliveTime);
            Filter filter = new Filter() {

                public boolean isLoggable(LogRecord record) {
                    if (record.getLevel().intValue() <= Level.INFO.intValue()) {
                    	System.out.println("LOGGINGG****************************** ASync");
                        if ((record.getMessage() != null)
                                && (record.getLoggerName()
                                        .matches(LOGGER_NAME))) {
                            return true;
                        }
                    }

                    return false;
                }
            };
            fileHandler.setFilter(filter);
            rootLogger.addHandler(fileHandler);
            //CHECKSTYLE:OFF
        } catch (Exception e) {//CHECKSTYLE:ON
            // Ignore
            return true;
        }
        return true;
    }
    //CHECKSTYLE:OFF
    private int getSystemPropertyValue(String maxThread, int maxThreadCount) {
        try {
            if(maxThread!=null){
                maxThreadCount = Integer.valueOf(maxThread);
            }

        } catch (Exception e) {
        }
        return maxThreadCount;
    }

   

    /**
     * 
     * startAppLogger
     */
    protected boolean startAppNotificationLogger(ServletContextEvent contextEvent, java.util.logging.Logger rootLogger) {

        try {
            String serverName = contextEvent.getServletContext().getServerInfo();
            FileHandler fileHandler =
                new FileHandler(System.getProperty("server.root") + "/logs/" + serverName + "/PSHNotification.log",
                        10000000, 50, true) {
                @Override
                public synchronized void publish(LogRecord record) {
                    super.publish(record);
                }
            };
            Filter filter = new Filter() {
                public boolean isLoggable(LogRecord record) {
                    if (record.getLevel().intValue() >= Level.INFO.intValue()) {
                        if (record.getMessage() != null
                                && !(record.getLoggerName()
                                        .matches("com\\.clear2pay(.*).logger.async.AsyncFileHandler"))
                                        && record.getMessage().matches("(.*)\\[ARBPSHNotification\\](.*)")) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            fileHandler.setFilter(filter);

            Formatter formatter = new Formatter() {
                @Override
                public String format(LogRecord record) {
                    StringBuffer sb = new StringBuffer();

                    java.sql.Timestamp ts = new java.sql.Timestamp(record.getMillis());
                    sb.append("[");
                    sb.append(ts.toString());
                    sb.append("]");
                    sb.append(" ");

                    sb.append(record.getThreadID());
                    sb.append(" ");

                    sb.append(record.getLoggerName());
                    sb.append(" ");

                    // Get the level name and add it to the buffer
                    sb.append(record.getLevel().getName());
                    sb.append(" ");

                    // Get the formatted message (includes localization
                    // and substitution of paramters) and add it to the buffer
                    sb.append(formatMessage(record));
                    sb.append("\n");

                    return sb.toString();
                }
            };
            fileHandler.setFormatter(formatter);
            rootLogger.addHandler(fileHandler);
            //CHECKSTYLE:OFF
        } catch (Exception e) {//CHECKSTYLE:ON
            // Ignore
            return true;
        }

        return true;
    }

}
