/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsflow;

/**
 *
 * @author wk
 */
public class FlowDestIPComparator implements java.util.Comparator<Flow> {

    @Override
    public int compare(Flow f1, Flow f2) {
        return f1.getDestIP().compareTo(f2.getDestIP());
    }
    
}
