package com.github.nordinh.filerepository;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.commercehub.dropwizard.mongo.MongoClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileRepositoryConfiguration extends Configuration {

	@Valid
	@NotNull
	private MongoClientFactory mongo;

	@JsonProperty
	public MongoClientFactory getMongo() {
		return mongo;
	}

	@JsonProperty
	public void setMongo(MongoClientFactory mongo) {
		this.mongo = mongo;
	}

}
