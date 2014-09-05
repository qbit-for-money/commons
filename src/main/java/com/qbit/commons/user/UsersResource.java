package com.qbit.commons.user;

import com.qbit.commons.auth.AuthFilter;
import com.qbit.commons.log.model.Location;
import com.qbit.commons.log.model.OperationType;
import com.qbit.commons.log.service.LogScheduler;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@Path("users")
@Singleton
public class UsersResource {

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class UserId {

		private String userId;

		public UserId() {
		}

		public UserId(String userId) {
			this.userId = userId;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		@Override
		public String toString() {
			return "UserId{" + "userId=" + userId + '}';
		}
	}
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class MachineId {
		private String machineId;

		public String getMachineId() {
			return machineId;
		}

		public void setMachineId(String machineId) {
			this.machineId = machineId;
		}
	}

	@Context
	private HttpServletRequest request;

	@Inject
	private UserDAO userDAO;
	@Inject
	LogScheduler logScheduler;

	@GET
	@Path("current")
	@Produces(MediaType.APPLICATION_JSON)
	public UserInfo current() {
		return userDAO.findFromAllIds(AuthFilter.getUserId(request));
	}

	@GET
	@Path("current/alt-id")
	@Produces(MediaType.APPLICATION_JSON)
	public UserId currentAltId() {
		return new UserId(AuthFilter.getUserAltId(request));
	}

	@GET
	@Path("{id}/alt-id")
	@Produces(MediaType.APPLICATION_JSON)
	public UserInfo byAltId(@PathParam("id") String id) {
		return userDAO.findFromAllIds(id);
	}

	@POST
	@Path("logout")
	public boolean logout() {
		if (request.getSession().getAttribute(AuthFilter.USER_ID_KEY) != null) {
			String userId = AuthFilter.getUserId(request);
			request.getSession().removeAttribute(AuthFilter.USER_ID_KEY);
			request.getSession().removeAttribute(AuthFilter.USER_ALT_ID_KEY);
			if (logScheduler != null) {
				logScheduler.createLog(OperationType.LOGIN_LOGOUT,
						userId, userId);
			}
			return true;
		}
		return false;
	}

	@POST
	@Path("machine")
	public void setMachineId(MachineId machineIdRequest) {
		if ((machineIdRequest == null) || (machineIdRequest.getMachineId() == null) 
				|| machineIdRequest.getMachineId().isEmpty()) {
			return;
		}
		String machineId = machineIdRequest.getMachineId();
		if((request.getSession().getAttribute(AuthFilter.MACHINE_ID_KEY) == null)
				|| !machineId.equals(AuthFilter.getMachineId(request))) {
			request.getSession().setAttribute(AuthFilter.MACHINE_ID_KEY, machineId);
		}
	}
	
	@POST
	@Path("location")
	public void setUserLocation(Location location) {
		if(location == null) {
			return;
		}
		if((request.getSession().getAttribute(AuthFilter.USER_LOCATION) == null)
				|| !location.equals(AuthFilter.getUserLocation(request))) {
			request.getSession().setAttribute(AuthFilter.USER_LOCATION, location);
		}
	}
}
