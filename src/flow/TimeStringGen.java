/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package flow;

/**
 *
 * @author gxia
 */
public class TimeStringGen {
    private static String startQueryTimeStr;
    private static String startQuerySecsStr;
    public TimeStringGen() {
        java.util.Date dateObj = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        startQueryTimeStr = sdf.format(dateObj);
        startQuerySecsStr = String.valueOf((System.currentTimeMillis() / 1000));        
    }
    
    public static String getTimeStr(){
        return startQueryTimeStr;
    }
    public static void setTimeStr(){

        //Acquire datetime string, pass it to SQL query to filter newly added ip entries
        java.util.Date dateObj = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        startQueryTimeStr = sdf.format(dateObj);
    }
    
    public static String getSecsStr(){
        return startQuerySecsStr;
    }
    public static void setSecsStr(){

        //Acquire datetime string, pass it to SQL query to filter newly added ip entries
        startQuerySecsStr = String.valueOf((System.currentTimeMillis() / 1000));
    }
}
