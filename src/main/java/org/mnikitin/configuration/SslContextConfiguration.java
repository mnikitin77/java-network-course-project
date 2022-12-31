package org.mnikitin.configuration;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

public class SslContextConfiguration {

    private final String keyStorePath;
    private final String trustStorePath;
    private final String keyStorePwd;
    private final String trustStorePwd;

    public SslContextConfiguration(String keyStorePath,
                                   String ketStorePwd,
                                   String trustStorePath,
                                   String trustStorePwd) {
        this.keyStorePath = keyStorePath;
        this.trustStorePath = trustStorePath;
        this.keyStorePwd = ketStorePwd;
        this.trustStorePwd = trustStorePwd;
    }

    public SslContext getServerSslContext() throws Exception {
        return SslContextBuilder
                .forServer(getKeyManagerFactory())
                .clientAuth(ClientAuth.REQUIRE)
                .trustManager(getTrustManagerFactory())
                .build();
    }

    public SslContext getClientSslContext() throws Exception {
        return SslContextBuilder
                .forClient()
                .keyManager(getKeyManagerFactory())
                .trustManager(getTrustManagerFactory())
                .build();
    }

    private KeyManagerFactory getKeyManagerFactory() throws Exception {
        var factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        var keyStore = KeyStore.getInstance("JKS");
        try (var keyStoreFile = getClass().getResourceAsStream(keyStorePath);) {
            keyStore.load(keyStoreFile, keyStorePwd.toCharArray());
        }
        factory.init(keyStore, keyStorePwd.toCharArray());
        return factory;
    }

    private TrustManagerFactory getTrustManagerFactory() throws Exception {
        var factory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        var keyStore = KeyStore.getInstance("JKS");
        try (var trustStoreFile = getClass().getResourceAsStream(trustStorePath);) {
            keyStore.load(trustStoreFile, trustStorePwd.toCharArray());
        }
        factory.init(keyStore);
        return factory;
    }
}
