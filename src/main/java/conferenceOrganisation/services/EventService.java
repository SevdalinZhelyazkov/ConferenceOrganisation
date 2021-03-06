package conferenceOrganisation.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import conferenceOrganisation.managers.LectureManager;
import conferenceOrganisation.models.CitiesContainer;
import conferenceOrganisation.models.Event;
import conferenceOrganisation.utils.Utils;

@Stateless
@Path("events")
public class EventService {

	@Inject
	EventManager eventManager;

	@Inject
	LectureManager lectureManager;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Event> getAllPublishedEvents() {
		List<Event> events = new ArrayList<>();
		try {
			events = eventManager.getAllPublishedEvents();
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to get all published events.");
		}
		return events;
	}

	@Path("review")
	@POST
	public Response sendEventForReview(@QueryParam("eventId") int eventId) {
		try {
			eventManager.sendEventForReview(eventId);
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to publish event with id : " + eventId);
			return Utils.RESPONSE_ERROR;
		}
		return Utils.RESPONSE_OK;
	}

	@Path("cities")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public CitiesContainer getAllCytiesWithEvent() {
		CitiesContainer cyties = new CitiesContainer();
		try {
			cyties = eventManager.getAllCytiesWithEvent();
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to get available cities");
		}
		return cyties;
	}

	@Path("eventsByCity")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Event> getAllEventsByCity(@QueryParam("city") String city) {
		List<Event> events = new ArrayList<>();
		try {
			events = eventManager.getAllEventsByCity(city);
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to get all events by city : " + city);
		}
		return events;
	}

	@Path("eventByEventId")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Event getEventByEventId(@QueryParam("eventId") int eventId) {
		Event event = new Event();
		try {
			event = eventManager.getEventByEventId(eventId);
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to get event with id : " + eventId);
		}
		return event;
	}

	@Path("eventsByCreatorId")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Event> getAllEventsByCreatorId(@QueryParam("creatorId") int creatorId)
			throws SQLException, IOException {
		List<Event> events = eventManager.getAllEventsByUserId(creatorId);
		return events;
	}

	@Path("addRating")
	@POST
	public Response giveRatingToEvent(@QueryParam("eventId") int eventId, @QueryParam("score") int score) {
		try {
			if (!eventManager.checkIfUserIsAbleToGiveRatingToSpecificEvent(eventId)) {
				return Utils.RESPONSE_ERROR;
			}
			eventManager.giveRatingToEvent(eventId, score);
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to add new rating to event with id : " + eventId);
			e.printStackTrace();
			return Utils.RESPONSE_ERROR;
		}
		return Utils.RESPONSE_OK;
	}

	@Path("edit")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editEvent(Event event) {
		try {
			eventManager.editEvent(event);
			return Utils.RESPONSE_OK;
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to edit event with eventId : " + event.getEventId());
			return Utils.RESPONSE_ERROR;
		}
	}

	@Path("delete")
	@POST
	public Response deleteEvent(@QueryParam("eventId") int eventId) {
		try {
			eventManager.deleteEventByEventId(eventId);
			return Utils.RESPONSE_OK;
		} catch (SQLException | IOException e) {
			System.out.println("Exception while trying to delete event with eventId : " + eventId);
			return Utils.RESPONSE_ERROR;
		}
	}

}
