package com.example.springdatarestpoc;

import org.springframework.core.MethodParameter;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

public class MyMethodParameterAwarePagedResourcesAssembler<T> extends MyPagedResourcesAssembler<T> {
    private final MethodParameter parameter;

    public MyMethodParameterAwarePagedResourcesAssembler(MethodParameter parameter, HateoasPageableHandlerMethodArgumentResolver resolver, UriComponents baseUri) {
        super(resolver, baseUri);

        Assert.notNull(parameter, "Method parameter must not be null!");
        this.parameter = parameter;
    }

    @Override
    protected MethodParameter getMethodParameter() {
        return parameter;
    }
}
