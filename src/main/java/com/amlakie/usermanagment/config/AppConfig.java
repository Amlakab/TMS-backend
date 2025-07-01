// C:/.../com/amlakie/usermanagment/config/AppConfig.java

package com.amlakie.usermanagment.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        // This method creates and configures the ModelMapper bean
        return new ModelMapper();
    }
}