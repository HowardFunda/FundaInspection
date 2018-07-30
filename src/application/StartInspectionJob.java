package application;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javafx.application.Platform;

public class StartInspectionJob implements Job{

//	private Logger logger = Logger.getLogger(this.getClass());
	
	private static StartRunner INSTENCE = new StartRunner();
	
	public static StartRunner getStartRunner() {
		return INSTENCE;
	}
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
//		logger.debug("StartInspectionJob - " + new Date());
		Platform.runLater(getStartRunner());
		
	}
	static class StartRunner implements Runnable{
		@Override
		public void run() {
			InspectionMaker.getInspectionMaker().show();
		}
	}

	

}
