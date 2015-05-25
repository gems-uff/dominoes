package boundary;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.JFrame;

import control.Controller;
import dao.DominoesSQLDao;
import domain.Configuration;
import domain.Dominoes;

// Information fragment // opened question

@SuppressWarnings("restriction")
public class App extends Application {

    private static ListViewDominoes list;
    private static AreaMove area;
    private static DominoesMenuBar menu;
    private static TimePane time;
    private static Visual visual;

    public static Visual getVisual(){
    	return visual;
    }
    
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
    
    private static GUIManager manager;
    private static Date beginDate = null;
    private static Date endDate = null;
    private static String projectName = "Caminho";
    
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
	    			    beginDateWork =  time.getValueToolTipMin().substring(3, 7) + "-" + 
	    			    		time.getValueToolTipMin().substring(0, 2) + "-01  00:00:00";
	    			    endDateWork =  time.getValueToolTipMax().substring(3, 7) + "-" + 
	    			    		time.getValueToolTipMax().substring(0, 2) + "-31  00:00:00";
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
    public static void checkout(String _beginDate, String _endDate){
    	// Open database
        try {
			
			// Set begin and end date
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			beginDate = sdf.parse(_beginDate);
			endDate = sdf.parse(_endDate);
			//DominoesSQLDao.setBeginDate(sdf.parse(beginDate));
			//DominoesSQLDao.setEndDate(sdf.parse(endDate));
//			DominoesSQLDao.setBeginDate(sdf.parse("2013-11-01 00:00:00"));
//			DominoesSQLDao.setEndDate(sdf.parse("2014-01-31 00:00:00"));
//			DominoesSQLDao.openDatabase();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void setTimelime(){
    	beginDateWork = Configuration.beginDate; 
        endDateWork = Configuration.endDate;
    	
    	double min = 0, max = 0, unit = 0;
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

        App.stage.show();
        
        
		// Set begin and end date
        try {
			App.time = new TimePane(min, max, sdf.parse(beginDateWork), sdf.parse(endDateWork), 
					Configuration.database, projectName);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        App.time.setVisible(true);
        
		manager = GUIManager.getInstance();
		JFXPanel pane = new JFXPanel();
		pane.setScene(stage.getScene());
		JFrame window = new JFrame();
		window.add(pane);
		window.setAlwaysOnTop(true);
		manager.setMainWindow(window);
        
        time.setButtomOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0) {

				beginDateWork = time.getValueToolTipMin();
				endDateWork = time.getValueToolTipMax();

				App.load(beginDateWork, endDateWork);
			}
        });
    	
    }
    
    public static void load(String being, String end){
    	manager.run(new Runnable(){
			public void run(){
				Platform.runLater(new Runnable(){
					public void run(){
						try {
							App.menu.load(beginDateWork, endDateWork);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		});
    }
    
    /**
     * Set the basic configuration of this Application
     */
    public static void set() {
        if(Configuration.automaticCheck
//        		&& beginDateWork.compareTo(endDateWork) < 0
        		){
        	control.Controller.loadAllMatrices(beginDate, endDate, projectName);
        	App.list = new ListViewDominoes(Controller.resultLoadMatrices);
        }else{
        	App.list = new ListViewDominoes(null);
        }
        
        
        App.visual = new Visual();
        App.area = new AreaMove();
        
        App.scene = null;
        App.scene = new Scene(new Group());
        VBox back = new VBox();
        
        vSplitPane = new SplitPane();
        vSplitPane.setOrientation(Orientation.VERTICAL);
        
        vSP_body_hSplitPane = new SplitPane();
        vSP_body_hSplitPane.getItems().add(App.list);
        vSP_body_hSplitPane.getItems().add(App.area);
        vSP_body_hSplitPane.getItems().add(App.visual);

        
        vSP_head_TimePane = new BorderPane();
        vSP_head_TimePane.setCenter(time);
        vSP_head_TimePane.visibleProperty().addListener(new ChangeListener<Boolean>() {
        	
        	@Override
        	public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
        		if(vSP_head_TimePane.isVisible()){
        			vSplitPane.getItems().remove(vSP_body_hSplitPane);
        			
        			vSplitPane.getItems().add(vSP_head_TimePane);
        			vSplitPane.getItems().add(vSP_body_hSplitPane);
        			
        			vSP_body_hSplitPane.setPrefHeight(App.height/2);
        			vSP_head_TimePane.setPrefHeight(App.height/2);
        			
        		}else{
        			
        			vSplitPane.getItems().remove(vSP_head_TimePane);
        			vSP_body_hSplitPane.setPrefHeight(App.height);
        		}
        	}
		});
        
        if(Configuration.visibilityTimePane) 
        	vSplitPane.getItems().add(vSP_head_TimePane);
       
        vSplitPane.getItems().add(vSP_body_hSplitPane);
        
        back.getChildren().addAll(menu, vSplitPane);
        
        App.scene.setRoot(back);
        App.stage.setScene(App.scene);
//        App.stage.show();
        
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
        
        if(Configuration.visibilityTimePane){
        	App.time.definitionSlider(stage);
        }
        App.list.setSize(Configuration.listWidth , App.height - padding);
        App.visual.setSize(App.width, App.height - padding);
        App.area.setSize(App.width, App.height - padding);
        stage.show();
    }
    
    static void changeVisibleTimePane(){
	 	Configuration.visibilityTimePane = !Configuration.visibilityTimePane;
	 	App.time.setVisible(Configuration.visibilityTimePane);
	 	App.vSP_head_TimePane.setVisible(Configuration.visibilityTimePane);
    }
    
    /**
    *
    */
   public static void start() {
       launch(Controller.args );
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
   
   static void drawLineChart(Dominoes domino) {
       visual.addTabLineChart(domino);
   }
   
   static void drawTree(Dominoes domino) {
       visual.addTabTree(domino);
   }
   
   static Stage getStage(){
	   return App.stage;
   }
}
