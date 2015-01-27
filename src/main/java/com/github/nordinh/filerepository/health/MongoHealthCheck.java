package com.github.nordinh.filerepository.health;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.CommandResult;
import com.mongodb.DB;

public class MongoHealthCheck extends HealthCheck {

	private DB db;

	public MongoHealthCheck(DB db) {
		this.db = db;
	}

	@Override
	protected Result check() throws Exception {
		CommandResult result = db.command("ServerStatus");
		return result.isEmpty() ? Result.unhealthy("Could not retrieve mongo status") : Result.healthy();
	}

}
