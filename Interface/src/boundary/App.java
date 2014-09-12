package boundary;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import dao.DominoesSQLDao;
import domain.Configuration;
import domain.Dominoes;

@SuppressWarnings("restriction")
public class App extends Application {

    private static ListViewDominoes list;
    private static AreaMove area;
    private static DominoesMenuBar menu;
    private static TimePane time;
    private static Visual visual;

    private static SplitPane vSplitPane;
    private static SplitPane vSP_body_hSplitPane;
    private static BorderPane vSP_head_TimePane;
    
    private static Scene scene;
    private static Stage stage;
            
    private static double width = Configuration.width;
    private static double height = Configuration.height;
    
    private static String beginDateWork = Configuration.beginDate;
    private static String endDateWork = Configuration.endDate;
    
    private static ArrayList<Dominoes> array = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
        	
            App.stage = primaryStage;
            App.stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            App.stage.centerOnScreen();
            App.stage.setTitle("Dominoes Interface");
            App.stage.setResizable(Configuration.resizable);
            
            App.menu = new DominoesMenuBar();
            
            App.beginDateWork = Configuration.beginDate;
            App.endDateWork = Configuration.endDate;
            
            App.checkout(Configuration.beginDate, Configuration.endDate);
        	App.setTimelime();
            App.set();

            time.setButtomOnAction(new EventHandler<ActionEvent>() {
    			
    			@Override
    			public void handle(ActionEvent arg0) {
    				try{
	    			    beginDateWork = time.getValueToolTipMin();
	    			    endDateWork = time.getValueToolTipMax();
	    			    System.out.println("begin: " + beginDateWork);
	    			    System.out.println("end: " + endDateWork);
	    				App.menu.load(beginDateWork, endDateWork);
    				} catch (ParseException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
    		});
            
            if(Configuration.resizableTimeOnFullScreen){
            	App.fillTimeHistoricPointers();
            }
            
            if(!Configuration.automaticCheck){
            	App.clear();
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    private static void fillTimeHistoricPointers() {
    	App.setFullscreen(!stage.isFullScreen());
    	App.setFullscreen(!stage.isFullScreen());
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
     * check out the data
     */
    public static void checkout(String beginDate, String endDate){
    	// Open database
        try {
			
			// Set begin and end date
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			DominoesSQLDao.setBeginDate(sdf.parse(beginDate));
			DominoesSQLDao.setEndDate(sdf.parse(endDate));
//			DominoesSQLDao.setBeginDate(sdf.parse("2013-11-01 00:00:00"));
//			DominoesSQLDao.setEndDate(sdf.parse("2014-01-31 00:00:00"));
			DominoesSQLDao.openDatabase();
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
    }
    
    public static void setTimelime(){
    	beginDateWork = Configuration.beginDate; 
        endDateWork = Configuration.endDate;
    	
    	double min = 0, max = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
        	min = sdf.parse(beginDateWork).getYear() * 12;
        	min += sdf.parse(beginDateWork).getMonth();
        	
        	max = sdf.parse(endDateWork).getYear() * 12;
        	max += sdf.parse(endDateWork).getMonth();
        	
    	} catch (ParseException e) {
    		// TODO Auto-generated catch block
			e.printStackTrace();
    	}
        max = max - min;
        min = 0;

        App.time = new TimePane(min, max, min, min);
        App.time.setVisible(Configuration.visibilityTimePane);
    	
    }
    
    /**
     * Set the basic configuration of this Application
     */
    public static void set() {
        if(Configuration.automaticCheck){
        	array = control.Controller.loadAllMatrices();
        }else{
        	array = null;
        }
        App.list = new ListViewDominoes(array);
        App.visual = new Visual();
        App.area = new AreaMove();
        
        App.scene = new Scene(new Group());
        VBox back = new VBox();
        
        vSplitPane = new SplitPane();
        vSplitPane.setOrientation(Orientation.VERTICAL);
        
        vSP_body_hSplitPane = new SplitPane();
        
//        App.scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
//
//            @Override
//            public void handle(MouseEvent event) {
//                App.scene.setCursor(Cursor.DEFAULT);
//            }
//        });
        
        vSP_body_hSplitPane.getItems().add(App.list);
        vSP_body_hSplitPane.getItems().add(App.area);
        vSP_body_hSplitPane.getItems().add(App.visual);
        
        if(App.time == null){
        	App.setTimelime();
        }
        
        vSP_head_TimePane = new BorderPane();
        vSP_head_TimePane.setCenter(time);
        vSP_head_TimePane.visibleProperty().addListener(new ChangeListener<Boolean>() {
        	
        	@Override
        	public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
        		if(vSP_head_TimePane.isVisible()){
        			vSplitPane.getItems().remove(vSP_body_hSplitPane);
        			
        			vSplitPane.getItems().add(vSP_head_TimePane);
        			vSplitPane.getItems().add(vSP_body_hSplitPane);
        			
        		}else{
        			
        			vSplitPane.getItems().remove(vSP_head_TimePane);

        		}
        	}
		});
        
        if(Configuration.visibilityTimePane) vSplitPane.getItems().add(vSP_head_TimePane);
        vSplitPane.getItems().add(vSP_body_hSplitPane);
        
        back.getChildren().addAll(menu, vSplitPane);
        
        App.scene.setRoot(back);
        App.stage.setScene(App.scene);
//        App.setFullscreen(Configuration.fullscreen);
//        App.stage.show();
//        App.time.definitionSlider(stage);
        App.setFullscreen(Configuration.fullscreen);
    }
    
    @Override
    public void stop(){
    	// Destroy all remaining dominoes
    	
    }
    
    /**
     * This function is used to exit this program
     *
     * @param status status required by the operating system
     */
    @SuppressWarnings("unused")
	private static void exit(final int status) {
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
    	Configuration.fullscreen = fullscreen;
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
        
        if(App.time.isVisible()){
        	App.time.definitionSlider(stage);
        }
        App.list.setSize(Configuration.listWidth , App.height - padding);
        App.visual.setSize(App.width, App.height - padding);
        App.area.setSize(App.width, App.height - padding);

    }
    
    static void changeVisibleTimePane(){
	 	Configuration.visibilityTimePane = !Configuration.visibilityTimePane;
	 	time.setVisible(Configuration.visibilityTimePane);
	 	vSP_head_TimePane.setVisible(Configuration.visibilityTimePane);
    	
    }
    
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
   
   static Stage getStage(){
	   return App.stage;
   }
}
