package com.repository.document.executor;

import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.repository.document.helper.HttpRequestHelper;

import org.slf4j.Logger;

public class ExecutorStarter implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ExecutorStarter.class);
	
	private List<Runnable> executions;
	
	private TaskExecutor executor = new SyncTaskExecutor();

	public ExecutorStarter(List<Runnable> executions) {
		super();
		this.executions = executions;
	}

	@Override
	public void run() {
		logger.info("Number of executions="+executions.size());
		executions.forEach(e -> {
			logger.info("Executing "+e);
			executor.execute(e);
		});
		logger.info("All executions completed");
	}

}
