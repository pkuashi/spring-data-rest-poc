package com.example.springdatarestpoc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.PagedResourcesAssemblerArgumentResolver;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MethodLinkBuilderFactory;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public class MyPagedResourcesAssemblerArgumentResolver extends PagedResourcesAssemblerArgumentResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyPagedResourcesAssemblerArgumentResolver.class);

    private static final String SUPERFLOUS_QUALIFIER = "Found qualified {} parameter, but a unique unqualified {} parameter. Using that one, but you might wanna check your controller method configuration!";
    private static final String PARAMETER_AMBIGUITY = "Discovered muliple parameters of type Pageable but no qualifier annotations to disambiguate!";


    private final HateoasPageableHandlerMethodArgumentResolver resolver;
    private final MethodLinkBuilderFactory<?> linkBuilderFactory;

    public MyPagedResourcesAssemblerArgumentResolver(HateoasPageableHandlerMethodArgumentResolver resolver, @Nullable MethodLinkBuilderFactory<?> linkBuilderFactory) {
        super(resolver, linkBuilderFactory);

        this.resolver = resolver;
        this.linkBuilderFactory = linkBuilderFactory;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        UriComponents fromUriString = resolveBaseUri(parameter);
        MethodParameter pageableParameter = findMatchingPageableParameter(parameter);

        if (pageableParameter != null) {
            return new MyMethodParameterAwarePagedResourcesAssembler<>(pageableParameter, resolver, fromUriString);
        } else {
            return new MyPagedResourcesAssembler<>(resolver, fromUriString);
        }
    }

    @Nullable
    private UriComponents resolveBaseUri(MethodParameter parameter) {

        try {
            Link linkToMethod = linkBuilderFactory.linkTo(parameter.getDeclaringClass(), parameter.getMethod()).withSelfRel();
            return UriComponentsBuilder.fromUriString(linkToMethod.getHref()).build();
        } catch (IllegalArgumentException o_O) {
            return null;
        }
    }

    @Nullable
    private static MethodParameter findMatchingPageableParameter(MethodParameter parameter) {

        MethodParameters parameters = new MethodParameters(parameter.getMethod());
        List<MethodParameter> pageableParameters = parameters.getParametersOfType(Pageable.class);
        Qualifier assemblerQualifier = parameter.getParameterAnnotation(Qualifier.class);

        if (pageableParameters.isEmpty()) {
            return null;
        }

        if (pageableParameters.size() == 1) {

            MethodParameter pageableParameter = pageableParameters.get(0);
            MethodParameter matchingParameter = returnIfQualifiersMatch(pageableParameter, assemblerQualifier);

            if (matchingParameter == null) {
                LOGGER.info(SUPERFLOUS_QUALIFIER, PagedResourcesAssembler.class.getSimpleName(), Pageable.class.getName());
            }

            return pageableParameter;
        }

        if (assemblerQualifier == null) {
            throw new IllegalStateException(PARAMETER_AMBIGUITY);
        }

        for (MethodParameter pageableParameter : pageableParameters) {

            MethodParameter matchingParameter = returnIfQualifiersMatch(pageableParameter, assemblerQualifier);

            if (matchingParameter != null) {
                return matchingParameter;
            }
        }

        throw new IllegalStateException(PARAMETER_AMBIGUITY);
    }

    private static MethodParameter returnIfQualifiersMatch(MethodParameter pageableParameter,
                                                           @Nullable Qualifier assemblerQualifier) {

        if (assemblerQualifier == null) {
            return pageableParameter;
        }

        Qualifier pageableParameterQualifier = pageableParameter.getParameterAnnotation(Qualifier.class);

        if (pageableParameterQualifier == null) {
            return null;
        }

        return pageableParameterQualifier.value().equals(assemblerQualifier.value()) ? pageableParameter : null;
    }
}

