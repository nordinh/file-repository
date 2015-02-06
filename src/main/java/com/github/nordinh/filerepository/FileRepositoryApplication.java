package com.github.nordinh.filerepository;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.net.UnknownHostException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;

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
		bootstrap.addBundle(new AssetsBundle("/api-doc"));
	}

	@Override
	public void run(FileRepositoryConfiguration configuration, Environment environment) throws Exception {
		DB db = configureMongoDB(configuration, environment);
		configureCORS(environment);
		environment.jersey().register(new FileResource(db));
	}

	protected DB configureMongoDB(FileRepositoryConfiguration configuration, Environment environment)
			throws UnknownHostException {
		ManagedMongoClient mongoClient = configuration.getMongo().build();
		environment.lifecycle().manage(mongoClient);
		DB db = mongoClient.getDB(configuration.getMongo().getDbName());
		environment.healthChecks().register("mongoDB", new MongoHealthCheck(db));
		return db;
	}

	protected void configureCORS(Environment environment) {
		final FilterRegistration.Dynamic cors =
				environment.servlets().addFilter("CORS", CrossOriginFilter.class);

		// Configure CORS parameters
		cors.setInitParameter("allowedOrigins", "*");
		cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
		cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

		// Add URL mapping
		cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
	}

}
