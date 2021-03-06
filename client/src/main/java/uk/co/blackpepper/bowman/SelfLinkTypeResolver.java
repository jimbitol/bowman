package uk.co.blackpepper.bowman;

import java.net.URI;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.web.util.UriComponentsBuilder;

import uk.co.blackpepper.bowman.annotation.RemoteResource;

class SelfLinkTypeResolver implements TypeResolver {

	private Class<?>[] subtypes;
	
	SelfLinkTypeResolver(Class<?>[] subtypes) {
		this.subtypes = subtypes;
	}
	
	@Override
	public Class<?> resolveType(Class<?> declaredType, Links resourceLinks, Configuration configuration) {

		Link self = resourceLinks.getLink(Link.REL_SELF);
		
		if (self == null) {
			return declaredType;
		}

		for (Class<?> candidateClass : subtypes) {
			RemoteResource candidateClassInfo = AnnotationUtils.findAnnotation(candidateClass, RemoteResource.class);
			
			if (candidateClassInfo == null) {
				continue;
			}
			
			String resourcePath = candidateClassInfo.value();
			
			String resourceBaseUriString = UriComponentsBuilder.fromUri(configuration.getBaseUri())
				.path(resourcePath)
				.toUriString();
			
			String selfLinkUriString = toAbsoluteUriString(self.getHref(), configuration.getBaseUri());
			
			if (selfLinkUriString.startsWith(resourceBaseUriString + "/")) {
				return candidateClass;
			}
		}
		
		return declaredType;
	}
	
	private static String toAbsoluteUriString(String uri, URI baseUri) {
		if (UriComponentsBuilder.fromUriString(uri).build().getHost() != null) {
			return uri;
		}
		
		return UriComponentsBuilder.fromUri(baseUri)
			.path(uri)
			.toUriString();
	}
}
