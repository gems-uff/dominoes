/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boundary;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

//import boundary.components.MenuColor;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import domain.Configuration;
import javafx.scene.control.Slider;
import javafx.scene.control.CustomMenuItem;

/**
 *
 * @author Daniel
 */
@SuppressWarnings("restriction")
public class DominoesMenuBar extends MenuBar {

//------DOMINOES MENU ITENS-----------------------------------------------------
    private final Menu mDominoes;

    private final MenuItem mDominoes_new;
    private final MenuItem mDominoes_loadAll;
    private final MenuItem mDominoes_exit;
    private final MenuItem mDominoes_exitAndSave;
    private final Menu mDominoes_save;
    private final MenuItem mDominoes_save_saveAll;
    private final CheckMenuItem mDominoes_save_autoSave;
    private final SeparatorMenuItem mDominoes_separator;

//------EDIT MENU ITENS---------------------------------------------------------
    private final Menu mEdit;
    
    private final Menu mEdit_editMatrix;
    
//    private final MenuColor mEdit_editMatrix_mcMatrixColor;
    private final CheckMenuItem mEdit_showHistoric;
    private final CheckMenuItem mEdit_showType;

//------COFIGURATION MENU ITENS-------------------------------------------------
    private final Menu mConfiguration;
    private final CheckMenuItem mConfiguration_fullScreen;
    private final Menu mConfiguration_database;
    private final RadioMenuItem mConfiguration_database_accessTXT;
    private final RadioMenuItem mConfiguration_database_accessSQL;
    private final ToggleGroup mConfiguration_database_accessGroup;
    
    private final SeparatorMenuItem mConfiguration_separator;
    
//------TIME MENU ITENS----------------------------------------------------
    private final Menu mTimeline;
    private final CheckMenuItem mTimeline_ShowTimeline;
    
    /**
     * Builder class
     */
    public DominoesMenuBar() {
        this.setHeight(30);
//------DOMINOES MENU ITENS-----------------------------------------------------
        this.mDominoes = new Menu("Dominoes");

        this.mDominoes_new = new MenuItem("New");
        this.mDominoes_loadAll = new MenuItem("Load All");
        this.mDominoes_loadAll.setDisable(true);
        this.mDominoes_save = new Menu("Save");
        this.mDominoes_save_saveAll = new MenuItem("Save All");
        this.mDominoes_save_autoSave = new CheckMenuItem("Auto Save");
        this.mDominoes_save_autoSave.setSelected(Configuration.autoSave);
        this.mDominoes_exit = new MenuItem("Exit");
        this.mDominoes_exitAndSave = new MenuItem("Exit And Save");

        this.mDominoes_save.getItems().addAll(this.mDominoes_save_saveAll,
                this.mDominoes_save_autoSave);

        this.mDominoes_separator = new SeparatorMenuItem();

        this.mDominoes.getItems().addAll(this.mDominoes_new, this.mDominoes_loadAll,
                this.mDominoes_save, this.mDominoes_separator, mDominoes_exitAndSave,
                this.mDominoes_exit);

//------EDIT MENU ITENS---------------------------------------------------------
        this.mEdit = new Menu("Edit");

        this.mEdit_editMatrix = new Menu("Edit Matrix");
        
//        this.mEdit_editMatrix_mcMatrixColor = new MenuColor("Matrix colors");

//        this.mEdit_editMatrix.getItems().addAll(this.mEdit_editMatrix_mcMatrixColor);
        
        this.mEdit_showHistoric = new CheckMenuItem("Show Historic");
        this.mEdit_showHistoric.setSelected(Configuration.visibilityHistoric);

        mEdit_showType = new CheckMenuItem("Show Type");
        mEdit_showType.setSelected(Configuration.visibilityType);
        
        this.mEdit.getItems().addAll(this.mEdit_showHistoric, this.mEdit_showType);
//        this.mEdit.getItems().addAll(this.mEdit_editMatrix, this.mEdit_showHistoric, this.mEdit_showType);

//------CONFIGURATION MENU ITENS------------------------------------------------
        this.mConfiguration = new Menu("Configuration");

        this.mConfiguration_fullScreen = new CheckMenuItem("Full Screen");
        this.mConfiguration_fullScreen.setSelected(Configuration.fullscreen);
        this.mConfiguration_database = new Menu("Access Mode");
        this.mConfiguration_database_accessGroup = new ToggleGroup();
        this.mConfiguration_database_accessTXT = new RadioMenuItem("TXT Access");
        this.mConfiguration_database_accessSQL = new RadioMenuItem("SQL Access");
        this.mConfiguration_database_accessTXT.setToggleGroup(mConfiguration_database_accessGroup);
        this.mConfiguration_database_accessSQL.setToggleGroup(mConfiguration_database_accessGroup);
        this.mConfiguration_database_accessTXT.setSelected(true);
        
        this.mConfiguration_separator = new SeparatorMenuItem();
        
        this.mConfiguration_database.getItems().addAll(this.mConfiguration_database_accessTXT, this.mConfiguration_database_accessSQL);
        
        this.mConfiguration.getItems().addAll(this.mConfiguration_database, this.mConfiguration_separator, this.mConfiguration_fullScreen);

//------TIME MENU ITENS----------------------------------------------------
        this.mTimeline_ShowTimeline = new CheckMenuItem("View Time");
        this.mTimeline_ShowTimeline.setSelected(Configuration.visibilityTimePane);
        this.mTimeline = new Menu("Time");
        this.mTimeline.getItems().addAll(mTimeline_ShowTimeline);
        
//------MENU ITENS--------------------------------------------------------------
        this.getMenus().addAll(this.mDominoes, this.mEdit, this.mConfiguration, mTimeline);
//        this.getMenus().addAll(this.mDominoes, this.mEdit, this.mConfiguration);

        
        if(!Configuration.automaticCheck || Configuration.endDate.compareTo(Configuration.beginDate) <= 0){
        	this.changeEnableDisble();
        }
//------ADD LISTENERS-----------------------------------------------------------
//----------DOMINOES MENU ITENS-------------------------------------------------
        this.mDominoes_new.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                App.clear();
                changeEnableDisble();

            }
        });
        this.mDominoes_loadAll.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
        		App.load(Configuration.beginDate, Configuration.endDate);
            }
        });

        this.mDominoes_save_saveAll.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    App.saveAll();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        });

        this.mDominoes_save_autoSave.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Configuration.autoSave = /*this.*/mDominoes_save_autoSave.isSelected();
                
            }
        });

        this.mDominoes_exitAndSave.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    App.saveAll();
                    System.exit(0);
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        });

        this.mDominoes_exit.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.exit(0);
            }
        });

//----------EDIT MENU ITENS-----------------------------------------------------
        

        this.mEdit_showHistoric.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Configuration.visibilityHistoric = mEdit_showHistoric.isSelected();
                App.setVisibleHistoric();
            }
        });
        
        this.mEdit_showType.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Configuration.visibilityType = mEdit_showType.isSelected();
                App.setVisibleType();
            }
        });

//----------CONFIGURATION MENU ITENS------------------------------------------------
        this.mConfiguration_fullScreen.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                App.setFullscreen(mConfiguration_fullScreen.isSelected());
            }
        });
//----------TIME PANE MENU ITENS----------------------------------------------------
        mTimeline_ShowTimeline.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
            	
                App.changeVisibleTimePane();
                
            }
        });
    }
	public void changeEnableDisble() {
		
		this.mDominoes_new.setDisable(!this.mDominoes_new.isDisable());
        this.mDominoes_loadAll.setDisable(!this.mDominoes_loadAll.isDisable());
        this.mDominoes_save.setDisable(!this.mDominoes_save.isDisable());
        this.mDominoes_exit.setDisable(!this.mDominoes_exit.isDisable());
        this.mDominoes_exitAndSave.setDisable(!this.mDominoes_exitAndSave.isDisable());
        
//        this.mEdit.setDisable(!this.mEdit.isDisable());
        
//        this.mConfiguration.setDisable(!this.mConfiguration.isDisable());
        
//        this.mTimeline.setDisable(false);
		
	}
	
	public void load(String begin, String end) throws ParseException{	
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try{
			sdf.parse(begin);
			sdf.parse(end);
		}catch(ParseException e){
		}finally{
			try{
				sdf.parse(begin);
				sdf.parse(end);
			}catch(ParseException e){
				System.err.println("format not found");
				throw e;
			}
		}
		
		changeEnableDisble();
    	Configuration.automaticCheck = true;
    	App.checkout(begin, end);
        App.set();
		
	}
	
}
