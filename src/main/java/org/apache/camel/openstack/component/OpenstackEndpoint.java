package org.apache.camel.openstack.component;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.jclouds.ContextBuilder;
import org.jclouds.openstack.nova.v2_0.NovaApi;

/**
 * Created by EKabardinsky on 4/22/2016.
 */

@UriEndpoint(scheme = "openstack", syntax = "", title = "")
public class OpenstackEndpoint extends DefaultEndpoint {

    public enum Operation {
        listFlavors,
        listImages,
        createKeyPair,
        createVM,
        serverStatus
    }

    @UriParam(name = "password")
    private String password;

    @UriParam(name = "identity")
    private String identity;

    @UriParam(name = "region")
    private String region;

    @UriParam(name = "provider")
    private String provider;

    @UriParam(name = "novaUrl")
    private String novaUrl;

    private Operation operation;

    private NovaApi novaApi;

    public OpenstackEndpoint(Operation operation) {
        this.operation = operation;
    }
    public Producer createProducer() throws Exception {
        if (novaApi == null) {
            connect();
        }

        return new OpenstackProducer(this, novaApi, operation);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new RuntimeCamelException("Consuming not support on this endpoint");
    }

    @Override
    protected String createEndpointUri() {
        return "openstack://operation";
    }

    public boolean isSingleton() {
        return false;
    }

    private void connect() {

        novaApi = ContextBuilder.newBuilder(provider)
                .credentials(identity, password)
                .endpoint(novaUrl)
                .buildApi(NovaApi.class);
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNovaUrl() {
        return novaUrl;
    }

    public void setNovaUrl(String novaUrl) {
        this.novaUrl = novaUrl;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
