/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package boundary;

import arch.Session;

/**
 *
 * @author Daniel
 */
public class Main {
    public static void main(String args[]){
        Main.init();
    }
    
    public static void init(){
        try {
        	Session.startSession();
            // read the configuration file
            control.Controller.loadConfiguration();
            
            // call Application.launch()
            App.start();
            Session.closeSection();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}
