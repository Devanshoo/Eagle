package org.ekstep.jobs.samza.task;

import java.util.Map;

import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.apache.samza.task.WindowableTask;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.TestSamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JobLogger;

public class TestSamzaTask implements StreamTask, InitableTask, WindowableTask {
	
	static JobLogger LOGGER = new JobLogger(TestSamzaTask.class);

	private JobMetrics metrics;
	ISamzaService testSamzaService = new TestSamzaService();

	@Override
	public void init(Config config, TaskContext context) throws Exception {

		try {
			metrics = new JobMetrics(context);
			testSamzaService.initialize(config);
			LOGGER.info("Task initialized");
		} catch(Exception ex) {
			LOGGER.error("Task initialization failed", ex);
			throw ex;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		Map<String, Object> outgoingMap = (Map<String, Object>) envelope.getMessage();
		try {
			testSamzaService.processMessage(outgoingMap, metrics, collector);
		} catch (Exception e) {
			metrics.incFailedCounter();
			LOGGER.error("Message processing failed", outgoingMap, e);
		}
	}

	@Override
	public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		metrics.clear();
	}

}
