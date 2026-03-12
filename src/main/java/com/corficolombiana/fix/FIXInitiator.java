package com.company.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.SLF4JLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

import quickfix.Application;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FIXInitiator {

    private static final Logger log = LoggerFactory.getLogger(FIXInitiator.class);

    private final SocketInitiator initiator;

    public FIXInitiator(String configFile) throws ConfigError {
        SessionSettings settings = loadSettings(configFile);
        Application application = new FIXApplication();
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new SLF4JLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        this.initiator = new SocketInitiator(
                application, storeFactory, settings, logFactory, messageFactory
        );
    }

    public void start() throws ConfigError, RuntimeError {
        log.info("Starting FIX Initiator...");
        initiator.start();
        log.info("FIX Initiator started. Waiting for logon...");
    }

    public void stop() {
        log.info("Stopping FIX Initiator...");
        initiator.stop();
        log.info("FIX Initiator stopped.");
    }

    public SessionID getSessionID() {
        return initiator.getSessions().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No sessions configured"));
    }

    public boolean isLoggedOn() {
        return initiator.isLoggedOn();
    }

    private static SessionSettings loadSettings(String configFile) throws ConfigError {
        Path filePath = Path.of(configFile);
        if (Files.exists(filePath)) {
            log.info("Loading config from filesystem: {}", filePath.toAbsolutePath());
            try (InputStream is = new FileInputStream(filePath.toFile())) {
                return new SessionSettings(is);
            } catch (IOException e) {
                throw new ConfigError("Failed to read config file: " + filePath, e);
            }
        }

        InputStream classpathStream = FIXInitiator.class.getClassLoader().getResourceAsStream(configFile);
        if (classpathStream != null) {
            log.info("Loading config from classpath: {}", configFile);
            return new SessionSettings(classpathStream);
        }

        throw new ConfigError("Configuration file not found: " + configFile);
    }
}
