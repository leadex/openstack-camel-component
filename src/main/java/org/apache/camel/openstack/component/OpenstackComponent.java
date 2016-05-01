package org.apache.camel.openstack.component;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

import java.util.Map;

/**
 * Created by EKabardinsky on 4/22/2016.
 */
public class OpenstackComponent extends DefaultComponent {

    protected Endpoint createEndpoint(String s, String s1, Map<String, Object> map) throws Exception {
        return new OpenstackEndpoint(OpenstackEndpoint.Operation.valueOf(s1));
    }
}
