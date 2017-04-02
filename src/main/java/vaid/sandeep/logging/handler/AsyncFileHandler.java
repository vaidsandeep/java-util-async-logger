package vaid.sandeep.logging.handler;

import java.io.File;

/*
 * Copyright (c) 2005-2013 Clear2Pay nv/sa. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Clear2Pay nv/sa. ("Confidential Information").
 * It may not be copied or reproduced in any manner without the express written permission of Clear2Pay nv/sa.
 *
 */

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.ErrorManager;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * 
 */

// CHECKSTYLE:OFF
public class AsyncFileHandler extends Handler {

	FileHandler fileHandler;

	// creating the ThreadPoolExecutor
	ThreadPoolExecutor executorPool;
	private static final String LOGGER_NAME = "vaid.sandeep.logging.handler.AsyncFileHandler";

	public AsyncFileHandler(String filePath, int limit, int count, boolean append, int corePoolSize,
			int maximumPoolSize, int qDepth, int keepAliveTime) throws IOException {

		super();
		 System.out.println(" &&&& AsyncFileHandler &&&& " + filePath);
		
		
		fileHandler = new FileHandler(filePath, limit, count, append);
		
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl(fileHandler);

		executorPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(qDepth), threadFactory, rejectionHandler);
		Filter filter = new Filter() {
			public boolean isLoggable(LogRecord record) {
				// if (record.getLevel().intValue() >= Level.INFO.intValue()) {
				if ((record.getMessage() != null)
						&& (record.getLoggerName().matches(LOGGER_NAME))) {
					return true;
				}
				// }
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
		
		 

	}

	@Override
	public synchronized void publish(LogRecord record) {
		if (executorPool.isTerminating()) {
			executorPool.shutdown();
		}
		if (executorPool.isTerminated()) {
			executorPool.shutdown();
		}

		try {
			if (this.isLoggable(record)) {

				executorPool.execute(new WorkerThread(record));

			}
		} catch (Exception e) {
			// logging.18=Exception occurred while logging the record.
			e.printStackTrace();
		}

	}

	class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {
		private FileHandler fileHandler;

		public RejectedExecutionHandlerImpl(FileHandler fileHandler) {
			this.fileHandler = fileHandler;
		}

		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			fileHandler.publish(((WorkerThread) r).getCommand());
		}

	}

	class WorkerThread implements Runnable {

		private LogRecord command;

		public WorkerThread(LogRecord s) throws IOException {
			super();
			this.command = s;

		}

		public void run() {

			processCommand();

		}

		private void processCommand() {

			fileHandler.publish(command);

		}

		@Override
		public String toString() {
			return this.command.toString();
		}

		public LogRecord getCommand() {
			return command;
		}

	}

	@Override
	public void close() {
		executorPool.shutdown();
	}

	@Override
	public void flush() {

	}

}
