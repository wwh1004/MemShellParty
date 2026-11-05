plugins {
    alias(libs.plugins.lombok)
    id("java8-library-conventions")
}

description = "MemShellParty Godzilla HW shell tool"

dependencies {
    api(project(":memshell-party-core"))
    annotationProcessor(libs.metainf.services)
    compileOnly(libs.metainf.services)
    // Valve 模板：org.apache.catalina.*（编译期桩）
    compileOnly(project(":memshell-party-stub:memshell-party-stub-tomcat"))
    // Jetty Handler 模板：org.eclipse.jetty.*（编译期桩）
    compileOnly(project(":memshell-party-stub:memshell-party-stub-jetty"))
    // Interceptor / ControllerHandler 模板：org.springframework.web.servlet.*
    compileOnly(libs.spring.webmvc)
    // Servlet / Filter / Listener 模板：javax.servlet.*
    compileOnly(libs.javax.servlet.api)
    // NettyHandler 模板：io.netty.*
    compileOnly(libs.netty.all)
    // WebSocket 模板：javax.websocket.*
    compileOnly(libs.javax.websocket.api)

    // 生成期测试：内嵌 Tomcat、仓库内哥斯拉客户端、Spring MVC/Struts2 服务端
    testImplementation(project(":memshell-party-server:memshell-party-server-servlet")) {
        exclude(group = "io.github.reajason", module = "memshell-party-stub-tomcat")
    }
    testImplementation(project(":memshell-party-packer"))
    testImplementation(project(":memshell-party-server:memshell-party-server-spring-webmvc"))
    testImplementation(project(":memshell-party-server:memshell-party-server-struts2"))
    testImplementation(project(":memshell-party-server:memshell-party-server-spring-webflux"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.tomcat.embed.core)
    testImplementation(libs.javax.servlet.api)
    // Jakarta 变体生成后要加载类来断言
    testImplementation(libs.jakarta.servlet.api)
    testImplementation(project(":tools:godzilla"))
    testImplementation(libs.okhttp3)
    testRuntimeOnly(libs.junit.platform.launcher)
}
