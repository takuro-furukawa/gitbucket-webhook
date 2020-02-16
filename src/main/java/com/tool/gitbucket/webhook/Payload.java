package com.tool.gitbucket.webhook;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Payload for PushEvent and PullRequestEvent
 */
@Data
public class Payload implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty
	String action;
	@JsonProperty(value = "pull_request")
	PullRequest pullRequest;
	@JsonProperty
	Repository repository;

	@JsonProperty
	String ref;
}

@Data
class PullRequest {
	@JsonProperty
	Base base;
	@JsonProperty
	boolean merged;
}

@Data
class Base {
	@JsonProperty
	String ref;
}

@Data
class Repository {
	@JsonProperty
	String name;
	@JsonProperty
	String url;
}