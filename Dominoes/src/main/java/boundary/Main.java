/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package boundary;

import util.ConfigurationFile;
import control.Controller;
import com.josericardojunior.dao.DominoesSQLDao;
import domain.Configuration;
import com.josericardojunior.arch.Session;

/**
 *
 * @author Daniel
 */
public class Main {
    public static void main(String args[]){
    	Controller.args = args;
        Main.init();
    }
    
    public static void init(){
        try {
            // read the configuration file
            control.Controller.loadConfiguration();
        	Session.startSession(Configuration.gpuDevice);
            DominoesSQLDao.openDatabase(Configuration.database);
            // call Application.launch()
            App.start();
            Session.closeSection();
            DominoesSQLDao.closeDatabase();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}
