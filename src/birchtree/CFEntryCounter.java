/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package birchtree;

/**
 *
 * @author gxia
 */
public class CFEntryCounter {
    private int NumOfLeafEnt;
    public void CFEntryCounter(){
        this.NumOfLeafEnt = 0;
    }
    public void CFEntryCounter(int Num){
        this.NumOfLeafEnt = Num;
    }
    public void CounterIncrease(){
        NumOfLeafEnt++;
    }
    public int getCounter(){
        return NumOfLeafEnt;
    }
    public void copy(CFEntryCounter c){
        this.NumOfLeafEnt = c.getCounter();
    }
    
}
