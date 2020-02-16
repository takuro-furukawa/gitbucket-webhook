package com.tool.gitbucket.webhook;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Main Application Class
 */
@SpringBootApplication
public class GitBucketWebhookApplication {

	public static void main(String[] args) {
		SpringApplication.run(GitBucketWebhookApplication.class, args);
	}

}

/**
 * API Endpoint Class
 */
@RestController
@RequestMapping("/webhook")
class WebhookController {

	/**
	 * logger
	 */
	Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Rest Client
	 */
	private final RestTemplate restTemplate;

	/**
	 * Constructor
	 * @param restTemplateBuilder template builder
	 */
	public WebhookController(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	/**
	 * create HttpHeader for Rest Client
	 * @param username
	 * @param password
	 * @return
	 */
	@SuppressWarnings("serial")
	private HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {
			{
				// username:token
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);
				set("Authorization", authHeader);
				set("User-Agent", "custom");
			}
		};
	}

	/**
	 * property from jenkins.user
	 */
	@Value("${jenkins.user:admin}")
	String username;

	/**
	 * property from jenkins.token
	 */
	@Value("${jenkins.token:xxx}")
	String password;

	/**
	 * property from jenkins.host
	 */
	@Value("${jenkins.host:localhost}")
	String host;

	/**
	 * property from jenkins.port
	 */
	@Value("${jenkins.port:9090}")
	int port;

	/**
	 * property from jenkins.job
	 */
	@Value("${jenkins.job:newJob}")
	String jobName;

	@RequestMapping(value = "{branchName}", consumes = { MediaType.APPLICATION_JSON_VALUE })
	public void execute(@RequestHeader("X-Github-Event") String event, @PathVariable("branchName") String branchName,
			@RequestBody Payload body) {
		log.debug("event:" + event);
		log.debug("target branch:" + branchName);

		if ("pull_request".equals(event)) {
			log.debug("action:" + body.getAction());
			log.debug("merged:" + body.getPullRequest().isMerged());
			log.debug("ref:" + body.getPullRequest().getBase().getRef());
		}

		// if webhook event is push
		//    and push was against {branchName}
		// or webhook event is pull_request
		//    and pull_request is closed
		//    and pull_request is merged
		//    and pull_request is merged to {branchName}
		if (("push".equals(event) && body.getRef().endsWith(branchName)) ||
				("pull_request".equals(event) && 
				"closed".equals(body.getAction()) && 
				body.getPullRequest().isMerged()
				&& body.getPullRequest().getBase().getRef().endsWith(branchName))) {
			log.debug("hook detected!!!");

			StringBuilder sb = new StringBuilder()
					.append("http://")
					.append(host)
					.append(":")
					.append(port)
					.append("/job/")
					.append(jobName)
					.append("/buildWithParameters");
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(sb.toString())
					.queryParam("token", "hoge")
					.queryParam("branchName", branchName)
					.queryParam("repositoryName", body.getRepository().getName())
					.queryParam("repositoryUrl", body.getRepository().getUrl());

			log.debug(builder.toUriString());
			// execute jenkins job
			ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
					new HttpEntity<String>(createHeaders(username, password)), String.class);
			log.debug(result.getBody());
		} else {
			log.debug("nothing to execute...");
		}
	}

}

