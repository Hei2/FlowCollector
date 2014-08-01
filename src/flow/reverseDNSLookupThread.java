/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package flow;

import static flow.DNS.*;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 *
 * @author gxia
 */
public class reverseDNSLookupThread implements Callable<String> {
    private String hostIp;
   
    reverseDNSLookupThread(String ip){
        hostIp = ip;
    }

    @Override
    public String call() throws Exception {
        return DNS.getHostName(hostIp);
    }
}
