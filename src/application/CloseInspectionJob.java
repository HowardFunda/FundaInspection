package application;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javafx.application.Platform;

public class CloseInspectionJob implements Job{

//	private Logger logger = Logger.getLogger(this.getClass());
	
	private static CloseRunner INSTENCE = new CloseRunner();
	
	public static CloseRunner getCloseRunner() {
		return INSTENCE;
	}
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
//		logger.debug("CloseInspectionJob - " + new Date());
		Platform.runLater(getCloseRunner());
	}
	
	static class CloseRunner implements Runnable{
		@Override
		public void run() {
			InspectionMaker.getInspectionMaker().hide();
		}
	}

}
