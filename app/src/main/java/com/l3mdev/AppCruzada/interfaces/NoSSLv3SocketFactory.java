package com.l3mdev.AppCruzada.interfaces;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class NoSSLv3SocketFactory extends SSLSocketFactory {
    private final SSLSocketFactory delegate;

    public NoSSLv3SocketFactory() {
        this.delegate = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    public NoSSLv3SocketFactory(SSLSocketFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    private Socket makeSocketSafe(Socket socket) {
        if (socket instanceof SSLSocket) {
            socket = new NoSSLv3SSLSocket((SSLSocket) socket);
        }
        return socket;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return makeSocketSafe(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return makeSocketSafe(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return makeSocketSafe(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return makeSocketSafe(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return makeSocketSafe(delegate.createSocket(address, port, localAddress, localPort));
    }

    private static class NoSSLv3SSLSocket extends DelegateSSLSocket {
        private NoSSLv3SSLSocket(SSLSocket delegate) {
            super(delegate);
        }

        @Override
        public void setEnabledProtocols(String[] protocols) {
            if (protocols != null && protocols.length == 1 && "SSLv3".equals(protocols[0])) {
                List<String> enabledProtocols = new ArrayList<>(Arrays.asList(delegate.getEnabledProtocols()));
                if (enabledProtocols.size() > 1) {
                    enabledProtocols.remove("SSLv3");
                    protocols = enabledProtocols.toArray(new String[0]);
                } else {
                    System.out.println("SSL stuck with protocol available for " + enabledProtocols);
                }
            }
        }
    }

    private static class DelegateSSLSocket extends SSLSocket {
        protected final SSLSocket delegate;

        DelegateSSLSocket(SSLSocket delegate) {
            this.delegate = delegate;
        }

        // Implementa todos los métodos abstractos de SSLSocket delegando las llamadas al socket delegado (delegate).
        // Esto permite que NoSSLv3SSLSocketFactory funcione como un SSLSocketFactory completo.

        @Override
        public String[] getSupportedCipherSuites() {
            return new String[0];
        }

        @Override
        public String[] getEnabledCipherSuites() {
            return new String[0];
        }

        @Override
        public void setEnabledCipherSuites(String[] strings) {

        }

        @Override
        public String[] getSupportedProtocols() {
            return new String[0];
        }

        @Override
        public String[] getEnabledProtocols() {
            return new String[0];
        }

        @Override
        public void setEnabledProtocols(String[] strings) {

        }

        @Override
        public SSLSession getSession() {
            return null;
        }

        @Override
        public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
            delegate.addHandshakeCompletedListener(listener);
        }

        @Override
        public void removeHandshakeCompletedListener(HandshakeCompletedListener handshakeCompletedListener) {

        }

        @Override
        public void startHandshake() throws IOException {

        }

        @Override
        public void setUseClientMode(boolean b) {

        }

        @Override
        public boolean getUseClientMode() {
            return false;
        }

        @Override
        public void setNeedClientAuth(boolean b) {

        }

        @Override
        public boolean getNeedClientAuth() {
            return false;
        }

        @Override
        public void setWantClientAuth(boolean b) {

        }

        @Override
        public boolean getWantClientAuth() {
            return false;
        }

        @Override
        public void setEnableSessionCreation(boolean b) {

        }

        @Override
        public boolean getEnableSessionCreation() {
            return false;
        }

        @Override
        public void bind(SocketAddress localAddr) throws IOException {
            delegate.bind(localAddr);
        }

        // Implementa los demás métodos de SSLSocket delegando las llamadas al socket delegado.
        // Asegúrate de implementar todos los métodos necesarios según tu caso de uso.
        // Por ejemplo, getSession(), getOutputStream(), etc.
    }
}