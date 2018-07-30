package application;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class InspectionMaker {

	private static Logger logger = Logger.getLogger(InspectionMaker.class);
	private Stage mainStage;
	private ScheduledExecutorService scheduler;
	private Future<?> future;
	private double xOffset = 0;
	private double yOffset = 0;
	private MusicRunner musicRunner;

	private static InspectionMaker INSTANCE = new InspectionMaker();

	private InspectionMaker() {
		musicRunner = new MusicRunner();
		scheduler = Executors.newScheduledThreadPool(1);
	};

	public static InspectionMaker getInspectionMaker() {
		return INSTANCE;
	}

	public Stage getStage() {
		return this.mainStage;
	}

	public void setStage(Stage mainStage) {
		this.mainStage = mainStage;
	}

	public void show() {
		runMusic();
		mainStage.show();

	}

	public void hide() {
		logger.debug("mainStage.isShowing():"+mainStage.isShowing());
		if (mainStage.isShowing()) {
			mainStage.hide();
		}
		if(null != future) {
			logger.debug("future.isCancelled():"+future.isCancelled());
			logger.debug("future.isDone():"+future.isDone());
			if(!(future.isCancelled() || future.isDone())) {
				future.cancel(true);
			}
		}

	}

	public void startInspection(int deviceNum) {
		try {

			Text text = new Text();
			text.setText(readLineByLineJava8("./sceneFiles/revealWord.txt"));
			text.setId("fancytext");
			BorderPane root = new BorderPane();
			root.setStyle("-fx-background-color: rgba(255,255,255,0.5);");
			root.setCenter(text);

			Scene scene = new Scene(root, 800, 800);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			scene.setFill(null);
			scene.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					xOffset = event.getSceneX();
					yOffset = event.getSceneY();
				}
			});
			scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					mainStage.setX(event.getScreenX() - xOffset);
					mainStage.setY(event.getScreenY() - yOffset);
				}
			});
			scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton().equals(MouseButton.PRIMARY)) {
						if (event.getClickCount() == 2) {
							if (mainStage.isFullScreen()) {
								mainStage.setFullScreen(false);
							} else {
								mainStage.setFullScreen(true);
							}
						}
					}
				}
			});
			mainStage.setX(setOnSecondDevice(deviceNum));
			mainStage.initStyle(StageStyle.TRANSPARENT);
			mainStage.setScene(scene);
			mainStage.setFullScreen(true);
			mainStage.setAlwaysOnTop(true);
			movingScene(text, scene);

			mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent t) {
					// logger.debug("----------123-------------");
					scheduler.shutdown();
					// Platform.exit();
					// System.exit(0);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	private double setOnSecondDevice(int deviceNum) {
		double resultX = 0;

		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			
			logger.debug("GraphicsDevice length : " + gs.length);
			for (int j = 0; j < gs.length; j++) {
				GraphicsDevice gd = gs[j];
				GraphicsConfiguration[] gc = gd.getConfigurations();
				logger.debug("GraphicsDevice["+j+"] GC : " + gc.length);
				for (int i = 0; i < gc.length; i++) {
					logger.debug("dev```` : " + i + ":" + gc[i].getBounds().getX());
					logger.debug("dev```` : " + i + ":" + gc[i].getBounds().getY());
				}
			}
			GraphicsDevice gd = gs[deviceNum];
			GraphicsConfiguration[] gc = gd.getConfigurations();
			resultX = gc[0].getBounds().getX();
			logger.debug("resultX```` : " + resultX);
		} catch (HeadlessException e) {
			e.printStackTrace();
			logger.error(e);
		}
	
		
		return resultX;
	}

	private void runMusic() {
		future = scheduler.scheduleWithFixedDelay(musicRunner, 1L, 2L, TimeUnit.SECONDS);
	}

	class MusicRunner implements Runnable {

		Path dir;
		AudioClip plonkSound;

		public MusicRunner() {
			String musicToPlay = readLineByLineJava8("./sceneFiles/musicPlay.txt").trim();
			dir = Paths.get("./sceneFiles/" + musicToPlay + ".mp3");
			plonkSound = new AudioClip(dir.toUri().toString());
			plonkSound.setCycleCount(AudioClip.INDEFINITE);
		}

		@Override
		public void run() {
			if (!plonkSound.isPlaying()) {
				plonkSound.play();
			}
		}

	}

	private void movingScene(Text text, Scene scene) {
		double sceneWidth = scene.getWidth();
		double msgWidth = text.getLayoutBounds().getWidth();

		KeyValue initKeyValue = new KeyValue(text.translateXProperty(), sceneWidth);
		KeyFrame initFrame = new KeyFrame(Duration.ZERO, initKeyValue);

		KeyValue endKeyValue = new KeyValue(text.translateXProperty(), -1.0 * msgWidth);
		KeyFrame endFrame = new KeyFrame(Duration.seconds(5), endKeyValue);

		Timeline timeline = new Timeline(initFrame, endFrame);

		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
	}

	private static String readLineByLineJava8(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return contentBuilder.toString();
	}
}
