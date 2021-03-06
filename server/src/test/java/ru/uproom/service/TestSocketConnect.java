package ru.uproom.service;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import ru.uproom.gate.transport.command.HandshakeCommand;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.Socket;

/**
 * Created by hedin on 30.08.2014.
 */
@Ignore
public class TestSocketConnect {
    private static final int testPort = 19999;

    // TODO move to common helper
    public static void fillPrivateField(Object target, Object value, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("failed to inject fieldName :" + fieldName + " of " + target + " with " + value, e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("failed to inject fieldName :" + fieldName + " of " + target + " with " + value, e);
        }
    }

    @Test
    public synchronized void testSocketHandshake() throws IOException, InterruptedException {
        GateServiceImpl service = new GateServiceImpl();
        fillPrivateField(service, testPort, "port");
        DeviceStorageService storageService = Mockito.mock(DeviceStorageService.class);
        fillPrivateField(service, storageService, "deviceStorage");
        service.init();

        Socket socket = new Socket("localhost", testPort);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(new HandshakeCommand(1));
        wait(100);
        GateSocketHandler handler = service.getHandler(1);
        Assert.assertNotNull(handler);
    }
}
