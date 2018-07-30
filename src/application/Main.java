package application;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {

	private static Logger logger = Logger.getLogger(Main.class);
	@Override
	public void start(Stage primaryStage) {
		Parameters parameters = getParameters();
		List<String> params = parameters.getRaw();
		String deviceNum = "";
		if(params.size()>0) {
			deviceNum = params.get(0);
		}
		Platform.setImplicitExit(false);
		InspectionMaker inspectionMaker = InspectionMaker.getInspectionMaker();
		inspectionMaker.setStage(primaryStage);
		logger.debug("````` deviceNum ````` : "+deviceNum);
	
		inspectionMaker.startInspection(NumberUtils.toInt(deviceNum,0));
	}

	public static void main(String[] args) {
		launch(args);
	}

}
