package com.carshare;

import com.carshare.framework.config.PortReleaseListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan({"com.carshare.mapper", "com.carshare.system.mapper"})
@EnableAsync
@EnableScheduling
public class CarShareApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CarShareApplication.class);
        app.addListeners(new PortReleaseListener());
        app.run(args);
    }
}
