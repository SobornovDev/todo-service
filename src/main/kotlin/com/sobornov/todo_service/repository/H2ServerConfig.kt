package com.sobornov.todo_service.repository

import org.h2.tools.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class H2ServerConfig {
    @Bean(initMethod = "start", destroyMethod = "stop")
    fun h2TcpServer(): Server =
        Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092")
}