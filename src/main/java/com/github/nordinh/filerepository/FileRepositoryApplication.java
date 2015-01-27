package com.github.nordinh.filerepository;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.commercehub.dropwizard.mongo.ManagedMongoClient;
import com.github.nordinh.filerepository.health.MongoHealthCheck;
import com.github.nordinh.filerepository.resources.FileResource;
import com.mongodb.DB;

public class FileRepositoryApplication extends Application<FileRepositoryConfiguration> {

	public static void main(String[] args) throws Exception {
		new FileRepositoryApplication().run(args);
	}

	@Override
	public String getName() {
		return "File Repository powered by MongoDB";
	}

	@Override
	public void initialize(Bootstrap<FileRepositoryConfiguration> bootstrap) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(FileRepositoryConfiguration configuration, Environment environment) throws Exception {
		ManagedMongoClient mongoClient = configuration.getMongo().build();
		environment.lifecycle().manage(mongoClient);
		DB db = mongoClient.getDB(configuration.getMongo().getDbName());
		environment.healthChecks().register("mongoDB", new MongoHealthCheck(db));
		environment.jersey().register(new FileResource(db));
	}

}
