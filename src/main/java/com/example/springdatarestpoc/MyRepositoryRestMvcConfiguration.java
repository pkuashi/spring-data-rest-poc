package com.example.springdatarestpoc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.PagedResourcesAssemblerArgumentResolver;

@Configuration
@Slf4j
public class MyRepositoryRestMvcConfiguration extends RepositoryRestMvcConfiguration {
    public MyRepositoryRestMvcConfiguration(
            ApplicationContext context, @Qualifier("mvcConversionService") ObjectFactory<ConversionService> conversionService) {
        super(context, conversionService);
    }

    @Bean
    @Primary
    public PagedResourcesAssembler<?> pagedResourcesAssembler() {
        log.info("Creating pagedResourcesAssembler");
        return new MyPagedResourcesAssembler<>(pageableResolver(), null);
    }

    @Bean
    public PagedResourcesAssemblerArgumentResolver pagedResourcesAssemblerArgumentResolver() {
        return new MyPagedResourcesAssemblerArgumentResolver(pageableResolver(), null);
    }
}
