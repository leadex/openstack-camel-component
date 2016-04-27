package org.apache.camel.openstack.component;

import com.google.common.base.Optional;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.*;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;

import java.io.IOException;
import java.util.Set;

/**
 * Created by EKabardinsky on 4/22/2016.
 */
class OpenstackProducer extends DefaultProducer {

    private NovaApi novaApi;

    private OpenstackEndpoint.Operation operation;

    public OpenstackProducer(Endpoint endpoint, NovaApi novaApi, OpenstackEndpoint.Operation operation) {
        super(endpoint);
        this.novaApi = novaApi;
        this.operation = operation;
    }

    public void process(Exchange exchange) throws Exception {
        Object[] parameters = exchange.getIn().getBody(Object[].class);

        if (operation == OpenstackEndpoint.Operation.listFlavors) {
            exchange.getOut().setBody(listFlavors().toArray());
        } else if (operation == OpenstackEndpoint.Operation.listImages) {
            exchange.getOut().setBody(listImages().toArray());
        } else if (operation == OpenstackEndpoint.Operation.createKeyPair) {
            if (parameters.length != 1) {
                throw new Exception("Wrong parameters count : " + parameters.length);
            }
            String keyPairName  = (String) parameters[0];
            exchange.getOut().setBody(createKeyPair(keyPairName));
        } else if (operation == OpenstackEndpoint.Operation.serverStatus) {
            if (parameters.length != 1) {
                throw new Exception("Wrong parameters count : " + parameters.length);
            }
            String serverRef = (String) parameters[0];
            exchange.getOut().setBody(serverStatus(serverRef));
        } else if (operation == OpenstackEndpoint.Operation.createVM) {
            if (parameters.length != 4) {
                throw new Exception("Wrong parameters count : " + parameters.length);
            }
            String serverName = (String) parameters[0];
            String imageRef = (String) parameters[1];
            String flavorRef = (String) parameters[2];
            CreateServerOptions options = (CreateServerOptions) parameters[3];
            exchange.getOut().setBody(createVM(serverName, imageRef, flavorRef, options));
        } else {
            throw new Exception("Unknown operation : " + operation);
        }
    }

    private Set<? extends Flavor> listFlavors() throws IOException {
        FlavorApi flavorApi = novaApi.getFlavorApiForZone(getRegion());
        return flavorApi.listInDetail().concat().toSet();
    }

    private Set<? extends Image> listImages()  throws IOException {
        ImageApi imageApi = novaApi.getImageApiForZone(getRegion());
        return imageApi.listInDetail().concat().toSet();
    }

    private KeyPair createKeyPair(String keyPairName) throws Exception {
        Optional<? extends KeyPairApi> keyPairApi = novaApi.getKeyPairExtensionForZone(getRegion());

        if (!keyPairApi.isPresent()) {
            throw new Exception("Missing key pair api");
        }

        return keyPairApi.get().create(keyPairName);
    }

    private ServerCreated createVM(String serverName, String imageRef, String flavorRef, CreateServerOptions options)  throws IOException {
        ServerApi serverApi = novaApi.getServerApiForZone(getRegion());
        return serverApi.create(serverName, imageRef, flavorRef, options);
    }

    private Server.Status serverStatus(String serverRef) throws Exception {
        ServerApi serverApi = novaApi.getServerApiForZone(getRegion());
        Server server = serverApi.get(serverRef);
        if (server == null) {
            throw new Exception("Wrong server reference");
        }
        return server.getStatus();
    }

    private String getRegion() {
        return ((OpenstackEndpoint) getEndpoint()).getRegion();
    }
}
