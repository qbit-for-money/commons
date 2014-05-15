package com.qbit.commons.env;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("env_util")
@Singleton
public class CommonsEnvResource {

	@Inject
	private CommonsEnv envUtil;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public CommonsEnv get() {
		return envUtil;
	}
}