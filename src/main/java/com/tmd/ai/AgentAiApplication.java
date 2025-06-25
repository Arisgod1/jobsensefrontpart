package com.tmd.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AgentAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentAiApplication.class, args);
    }

}
