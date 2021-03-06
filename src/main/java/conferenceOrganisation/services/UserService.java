package conferenceOrganisation.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import conferenceOrganisation.managers.EventManager;
import conferenceOrganisation.managers.TicketManager;
import conferenceOrganisation.managers.UserManager;
import conferenceOrganisation.models.Event;
import conferenceOrganisation.models.User;
import conferenceOrganisation.models.UserPasswordChange;
import conferenceOrganisation.utils.Utils;

@Stateless
@Path("user")
public class UserService {

	private static final Response RESPONSE_OK = Response.ok().build();

	@Inject
	private UserManager userManager;

	@Inject
	private CurrentUser currentUser;

	@Inject
	private TicketManager ticketManager;

	@Inject
	private EventManager eventManager;

	@Path("login")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loginUser(User user) {
		User foundUser;
		try {
			foundUser = userManager.getUserByEmailAndPassword(user.getEmail(), user.getPassword());
			if (foundUser == null) {
				return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).build();
			}
			return Utils.RESPONSE_OK;
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to get user.");
			return Utils.RESPONSE_ERROR;
		}
	}

	@Path("register")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUser(final User user) throws IOException {
		try {
			userManager.addUser(user);
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Utils.sendWelcomeEmail(user);
				}
			});
			thread.start();
			return RESPONSE_OK;
		} catch (SQLException e) {
			System.out.println("Problem occurs while trying to add new user (User with same email already exist");
			return Utils.RESPONSE_ERROR;
		}
	}

	@Path("current")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser() {
		if (currentUser.getCurrentUser() == null) {
			return null;
		}
		return currentUser.getCurrentUser();
	}

	@Path("logout")
	@GET
	public void logoutUser() {
		currentUser.setCurrentUser(null);
	}

	@Path("bookTicket")
	@POST
	public Response bookTicket(@QueryParam("eventId") int eventId) throws SQLException, IOException {
		if (!ticketManager.addTicketToUser(eventId)) {
			return Response.status(401).build();
		}
		return RESPONSE_OK;
	}

	@Path("createEvent")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createEvent(Event event) {
		if (event.getTitle() == null || event.getTitle().trim().equals("") || event.getHall().getCapacity() <= 0) {
			return Utils.RESPONSE_ERROR;
		}
		try {
			int eventId = eventManager.addEvent(event);
			return Response.ok(eventId, MediaType.TEXT_PLAIN).build();

		} catch (SQLException | IOException e) {
			e.printStackTrace();
			return Utils.RESPONSE_ERROR;
		}
	}

	@Path("resetPassword")
	@POST
	public Response resetUserPassword(@QueryParam("email") String email) {
		final String toEmail = email;
		try {
			final String newPassword = userManager.changeUserPassword(email);
			if (newPassword != null) {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						Utils.sendNewPasswordEmail(toEmail, newPassword);
					}
				});
				thread.start();
				return Utils.RESPONSE_OK;
			} else {
				return Utils.RESPONSE_ERROR;
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			return Utils.RESPONSE_ERROR;
		}
	}

	@Path("edit")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editUser(User user) {
		try {
			userManager.editUser(user);
			return Utils.RESPONSE_OK;
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to edit user with userId : " + user.getUserId());
			return Utils.RESPONSE_ERROR;
		}
	}

	@Path("editPassword")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editPassword(UserPasswordChange userPasswordChange) {
		try {
			if (!userManager.checkUserByIdAndPassword(userPasswordChange.getUserId(),
					userPasswordChange.getOldPassword())) {
				return Utils.RESPONSE_ERROR;
			}
			userManager.editPassword(userPasswordChange);
			return Utils.RESPONSE_OK;
		} catch (SQLException | IOException e) {
			System.out.println(
					"Exception while trying to edit password to user with userId : " + userPasswordChange.getUserId());
			return Utils.RESPONSE_ERROR;
		}
	}

	@Path("editEmail")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editEmail(User user) {
		try {
			if (!userManager.checkUserByIdAndPassword(user.getUserId(), user.getPassword())) {
				return Utils.RESPONSE_ERROR;
			}
			userManager.editEmail(user);
			return Utils.RESPONSE_OK;
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to edit email to user with userId : " + user.getUserId());
			return Utils.RESPONSE_ERROR;
		}
	}

	@Path("canUserBuyTicket")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public boolean canUserBuyTicketForEvent(@QueryParam("eventId") int eventId) {
		try {
			return userManager.canCurrentUserBuyTicketForEvent(eventId);
		} catch (SQLException | IOException e) {
			//
			return false;
		}
	}
}
