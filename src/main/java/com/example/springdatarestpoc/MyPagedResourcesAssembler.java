package com.example.springdatarestpoc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

@Slf4j
public class MyPagedResourcesAssembler<T> extends PagedResourcesAssembler<T> {

    private final EmbeddedWrappers wrappers = new EmbeddedWrappers(false);
    private HateoasPageableHandlerMethodArgumentResolver pageableResolver;
    private Optional<UriComponents> baseUri;

    public MyPagedResourcesAssembler(@Nullable HateoasPageableHandlerMethodArgumentResolver resolver,
                                   @Nullable UriComponents baseUri) {

        super(resolver, baseUri);

        this.pageableResolver = resolver;
        this.baseUri = Optional.ofNullable(baseUri);
    }

    @Override
    public PagedResources<Resource> toResource(Page entity) {
        log.info("convert page to resources");
        return this.createResource(entity, it -> new Resource<>(it), Optional.empty());
    }

    private <S, R extends ResourceSupport> PagedResources<R> createResource(Page<S> page,
                                                                            ResourceAssembler<S, R> assembler, Optional<Link> link) {

        Assert.notNull(page, "Page must not be null!");
        Assert.notNull(assembler, "ResourceAssembler must not be null!");

        List<R> resources = new ArrayList<>(page.getNumberOfElements());

        for (S element : page) {
            resources.add(assembler.toResource(element));
        }

        PagedResources<R> resource = this.createPagedResource(resources, asPageMetadata(page), page);

        return this.addPaginationLinks(resource, page, link);
    }

    private <R> PagedResources<R> addPaginationLinks(PagedResources<R> resources, Page<?> page, Optional<Link> link) {

        log.info("Create links for resources");

        UriTemplate base = getUriTemplate(link);

        if (page.hasPrevious()) {
            resources.add(createLink(base, page.previousPageable(), Link.REL_PREVIOUS));
        }

        Link selfLink = link.map(it -> it.withSelfRel())//
                .orElseGet(() -> createLink(base, page.getPageable(), Link.REL_SELF));

        resources.add(selfLink);

        if (page.hasNext()) {
            resources.add(createLink(base, page.nextPageable(), Link.REL_NEXT));
        }

        return resources;
    }

    private UriTemplate getUriTemplate(Optional<Link> baseLink) {
        return new UriTemplate(baseLink.map(Link::getHref).orElseGet(this::baseUriOrCurrentRequest));
    }

    private Link createLink(UriTemplate base, Pageable pageable, String rel) {

        UriComponentsBuilder builder = fromUri(base.expand());
        pageableResolver.enhance(builder, getMethodParameter(), pageable);

        return new Link(new UriTemplate(builder.build().toString()), rel);
    }

    @Nullable
    protected MethodParameter getMethodParameter() {
        return null;
    }

    private PagedResources.PageMetadata asPageMetadata(Page<?> page) {

        Assert.notNull(page, "Page must not be null!");

        int number = page.getNumber();

        return new PagedResources.PageMetadata(page.getSize(), number, page.getTotalElements(), page.getTotalPages());
    }

    private String baseUriOrCurrentRequest() {
        return baseUri.map(Object::toString).orElseGet(MyPagedResourcesAssembler::currentRequest);
    }

    private static String currentRequest() {
        return ServletUriComponentsBuilder.fromCurrentRequest().build().toString();
    }
}
