/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package classifier;

import static classifier.DNS.*;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 *
 * @author gxia
 */
public class ReverseDNSLookupThread implements Callable<String[]> {
    private String hostIp;
   
    ReverseDNSLookupThread(String ip){
        hostIp = ip;
    }

    @Override
    public String[] call() throws Exception {
        String[] Host = new String[2];
        Host[0] = hostIp;
        Host[1] = DNS.getHostName(hostIp);
        return Host;
    }
}