package application;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.listeners.JobListenerSupport;

public class CloseJobListener extends JobListenerSupport{
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public String getName() {
		return "CloseJobListener";
	}

	@Override
	public void jobWasExecuted(JobExecutionContext arg0, JobExecutionException arg1) {
		logger.debug("jobWasExecuted :"+arg0.getFireTime());
		try {
			ScheduleMaker.makeCloseSchadule();
		} catch (SchedulerException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
}
