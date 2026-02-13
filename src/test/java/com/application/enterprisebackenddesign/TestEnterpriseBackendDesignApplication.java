package com.application.enterprisebackenddesign;

import org.springframework.boot.SpringApplication;

public class TestEnterpriseBackendDesignApplication {

    public static void main(String[] args) {
        SpringApplication.from(EnterpriseBackendDesignApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
