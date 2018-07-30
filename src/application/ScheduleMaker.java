package application;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

public class ScheduleMaker {
	 private static Logger logger = Logger.getLogger(ScheduleMaker.class);
	
	private static SchedulerFactory sf;
	private static Scheduler sched;
	private static Boolean isExecuted = Boolean.TRUE;
	private static JobDetail closeInspectionJob;
	
	static {
		sf = new StdSchedulerFactory();
		try {
			sched = sf.getScheduler();
			closeInspectionJob = JobBuilder.newJob(CloseInspectionJob.class)
					.withIdentity("closeInspectionJob", "group2").build();
		} catch (SchedulerException e) {
			isExecuted = Boolean.FALSE;
			e.printStackTrace();
			logger.error(e);
		}
	}

	public static void main(String[] args) throws Exception {
		logger.debug("isExecuted:"+isExecuted);
		if(isExecuted) {
//			 testCron(sched);
			setCronJobsAndTriggers(sched);
			sched.start();
			Main.main(args);
		}

	}

	@SuppressWarnings(value = { "unused" })
	private static void testCron(Scheduler sched) throws SchedulerException {
		// define the job and tie it to our HelloJob class
		JobDetail startInspectionJob = JobBuilder.newJob(StartInspectionJob.class)
				.withIdentity("startInspectionJob", "group1").build();	
		CronScheduleBuilder testSBs = CronScheduleBuilder.cronSchedule("0/20 * * ? * 2-6");
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1").withSchedule(testSBs).build();
		sched.scheduleJob(startInspectionJob, trigger);
		
//		JobDetail closeInspectionJob = JobBuilder.newJob(CloseInspectionJob.class)
//		.withIdentity("closeInspectionJob", "group1").build();
//		CronScheduleBuilder testSBe = CronScheduleBuilder.cronSchedule("10/20 * * ? * 2-6");
//		Trigger trigger2 = TriggerBuilder.newTrigger().withIdentity("trigger2", "group1").withSchedule(testSBe).build();
//		sched.scheduleJob(closeInspectionJob, trigger2);
		
		sched.getListenerManager().addJobListener(
	    		new CloseJobListener(), GroupMatcher.groupEquals("group1")
	    	);
	}

	private static void setCronJobsAndTriggers(Scheduler sched) throws Exception {

		List<String> cronStarts = Arrays.asList("0 0 9 ? * 2-6", 
				                                "0 0 10 ? * 2-6", 
				                                "0 0 11 ? * 2-6", 
				                                "0 50 11 ? * 2-6", 
				                                "0 0 14 ? * 2-6",
			                                	"0 0 15 ? * 2-6", 
			                                	"0 0 16 ? * 2-6", 
			                                	"0 50 16 ? * 2-6");

		for (int i = 0; i < cronStarts.size(); i++) {

			sched.scheduleJob(
					JobBuilder.newJob(StartInspectionJob.class).withIdentity("startInspectionJob" + i, "group1")
							.build(),
					TriggerBuilder.newTrigger().withIdentity("triggerStart" + i, "group1")
							.withSchedule(CronScheduleBuilder.cronSchedule(cronStarts.get(i))).build());
		}
		sched.getListenerManager().addJobListener(
	    		new CloseJobListener(), GroupMatcher.groupEquals("group1")
	    	);

	}

	public static Boolean makeCloseSchadule() throws SchedulerException {
		
		if(isExecuted) {
			
			Trigger triggerStop = TriggerBuilder
					.newTrigger()
					.withIdentity("triggerStop", "group2")
//					.withSchedule(SimpleScheduleBuilder.repeatSecondlyForTotalCount(1, 10)).startAt(DateUtils.addSeconds(new Date(), 10))
					.withSchedule(SimpleScheduleBuilder.repeatMinutelyForTotalCount(3, 1)).startAt(DateUtils.addMinutes(new Date(), 1))
					.build();
			if(!sched.checkExists(JobKey.jobKey("closeInspectionJob", "group2"))) {
				sched.scheduleJob(closeInspectionJob, triggerStop);
			}
			
		}
		return isExecuted;
	}

}
