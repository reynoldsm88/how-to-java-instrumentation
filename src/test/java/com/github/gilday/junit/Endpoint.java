package com.github.gilday.junit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Value object which contains the endpoint of an instrumented web application
 */
@Accessors(fluent = true)
@Value(staticConstructor = "of")
public class Endpoint {

    private final InetAddress host;
    private final int port;

    public boolean isListening() {
        try (final Socket ignored = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}