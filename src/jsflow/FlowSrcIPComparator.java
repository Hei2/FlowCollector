/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsflow;

import java.util.Comparator;

/**
 *
 * @author wk
 */
public class FlowSrcIPComparator implements Comparator<Flow> {
    
    @Override
    public int compare(Flow f1, Flow f2) {
        return f1.getSrcIP().compareTo(f2.getSrcIP());
    }
    
}
