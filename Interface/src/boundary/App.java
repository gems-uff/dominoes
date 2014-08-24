package boundary;

import dao.DominoesSQLDao;
import domain.Configuration;
import domain.Dominoes;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import arch.Matrix2D;
import arch.MatrixDescriptor;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private static ListViewDominoes list;
    private static AreaMove area;
    private static DominoesMenuBar menu;
    private static TimePane time;
    private static Visual visual;

    private static Scene scene;
    private static Stage stage;

    private static SplitPane splitPane;
            
    private static double width = Configuration.width;
    private static double height = Configuration.height;

    /**
     *
     */
    public static void start() {
        launch((String[]) null);
    }
    
    static void drawGraph(Dominoes domino) {
        visual.addTabGraph(domino);
    }

    static void drawMatrix(Dominoes domino) {
        visual.addTabMatrix(domino);
    }
    
    static void drawChart(Dominoes domino) {
        visual.addTabChart(domino);
    }
    
    static void drawTree(Dominoes domino) {
        visual.addTabTree(domino);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            App.stage = primaryStage;
            App.stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            App.stage.centerOnScreen();

            App.menu = new DominoesMenuBar();

            App.set();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * This function is used to exit this program
     *
     * @param status status required by the operating system
     */
    private static void exit(int status) {
        System.exit(status);
    }

    /**
     * This function is called to change the parts color
     */
    static void changeColor() {
        App.list.changeColor();
        App.area.changeColor();
    }

    /**
     * This function remove all parts in this area move
     */
    public static void clear() {
        App.list.clear();
        App.area.clear();
    }

    /**
     * This function adds in List a matrix specified
     *
     * @param dominoes the matrix to be added
     */
    public static void CopyToList(Dominoes dominoes) {
        App.list.add(dominoes);
    }

    /**
     * This function adds in Area a matrix specified
     *
     * @param dominoes the matrix to be added
     */
    public static void copyToArea(Dominoes dominoes) {
        App.area.add(dominoes);
    }

    /**
     * This function remove the element of the list and of the move area
     *
     * @param dominoes Element to remove
     * @param group Element to remove
     * @return true, in affirmative case
     * @throws IOException
     */
    public static boolean removeMatrix(Dominoes dominoes, Group group) throws IOException {
        boolean result = control.Controller.removeMatrix(dominoes);

        // if not removed both, then we have which resultMultiplication
        if (App.area.remove(group)) {
            if (App.list.remove(group)) {
                if (result) {
                    return true;
                } else {
                    App.area.add(dominoes);
                    App.list.add(dominoes);
                    return false;
                }
            } else {
                App.area.add(dominoes);
                return false;
            }
        }
        return false;

    }

    /**
     * This functions is called when user want save each alteration
     *
     * @throws IOException
     */
    public static void saveAll() throws IOException {
        App.area.saveAllAndSendToList();
    }

    /**
     * Set the basic configuration of this Application
     */
    public static void set() {
        App.stage.setTitle("Dominoes Interface");
        App.stage.setResizable(Configuration.resizable);

        // Open database
        try {
			DominoesSQLDao.openDatabase();
			
			// Set begin and end date
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DominoesSQLDao.setBeginDate(sdf.parse("2014-01-01 00:00:00"));
			DominoesSQLDao.setEndDate(sdf.parse("2014-12-31 00:00:00"));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        App.list = new ListViewDominoes();
        App.visual = new Visual();
        App.area = new AreaMove();
        App.time = new TimePane();
        App.time.setVisible(Configuration.visibilityTimePane);
        
        //App.list.add(new Dominoes("R","C", new Matrix2D(new MatrixDescriptor("R", "C"))));
        
        App.scene = new Scene(new Group());
        VBox back = new VBox();
        splitPane = new SplitPane();
        
        App.scene.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                App.scene.setCursor(Cursor.DEFAULT);
            }
        });
        
        splitPane.getItems().add(App.list);
        splitPane.getItems().add(App.area);
        splitPane.getItems().add(App.visual);
        
        back.getChildren().addAll(menu);
        
        back.getChildren().addAll(splitPane);
        if(time.isVisible())back.getChildren().add(time);
        time.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    back.getChildren().add(time);
                } else {
                    back.getChildren().remove(time);
                }
            }
        });
        
        
        App.scene.setRoot(back);
        App.stage.setScene(App.scene);
        App.stage.show();
        App.setFullscreen(Configuration.fullscreen);
    }

    /**
     * This function is used to define the visibility of historic
     *
     * @param visibility True to define visible the historic
     */
    public static void setVisibleHistoric() {
        area.setVisibleHistoric();
        list.setVisibleHistoric();
    }
    
    /**
     * This function is used to define the visibility of type
     *
     * @param visibility True to define visible the type
     */
    public static void setVisibleType() {
        area.setVisibleType();
        list.setVisibleType();
    }

    /**
     * This function is used to make full screen in this Application
     *
     * @param fullscreen
     */
    static void setFullscreen(boolean fullscreen) {
        double padding = menu.getHeight();
        App.stage.setFullScreen(fullscreen);
        if (!fullscreen) {
            padding += 30;
            
            App.stage.setWidth(Configuration.width);
            App.stage.setHeight(Configuration.height);
            App.stage.centerOnScreen();
        }

        App.width = App.stage.getWidth();
        App.height = App.stage.getHeight();
        
        App.list.setSize(Configuration.listWidth , App.height - padding);
        App.visual.setSize(App.width, App.height - padding);
        App.area.setSize(App.width, App.height - padding);

    }
    
    static void setVisibleTimePane(){
	 	Configuration.visibilityTimePane = !Configuration.visibilityTimePane;
    	time.setVisible(Configuration.visibilityTimePane);
    }
    
    @Override
    public void stop(){
    	// Destroy all remaining dominoes
    	
    }
    
}
