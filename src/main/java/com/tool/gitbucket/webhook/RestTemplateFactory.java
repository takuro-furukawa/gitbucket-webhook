package com.tool.gitbucket.webhook;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean {

	private RestTemplate restTemplate;

	public RestTemplate getObject() {
		return restTemplate;
	}

	public Class<RestTemplate> getObjectType() {
		return RestTemplate.class;
	}

	public boolean isSingleton() {
		return true;
	}

	@Value("${jenkins.host:localhost}")
	String host;
	@Value("${jenkins.port:9090}")
	int port;

	public void afterPropertiesSet() {
		HttpHost httpHost = new HttpHost(host, port, "http");
		restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactoryBasicAuth(httpHost));
	}
}