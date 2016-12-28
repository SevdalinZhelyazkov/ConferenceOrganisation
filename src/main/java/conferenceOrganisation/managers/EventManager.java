package conferenceOrganisation.managers;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Singleton;
import javax.inject.Inject;

import conferenceOrganisation.database.connection.DatabaseConnection;
import conferenceOrganisation.models.CitiesContainer;
import conferenceOrganisation.models.Event;
import conferenceOrganisation.models.User;
import conferenceOrganisation.services.CurrentUser;

@Singleton
public class EventManager {

	@Inject
	private HallManager hallManager;

	@Inject
	CurrentUser currentUser;

	@Inject
	LectureManager lectureManager;

	@Inject
	DatabaseConnection dbConnection;

	public boolean addEvent(Event event) throws SQLException, IOException {
		User user = currentUser.getCurrentUser();
		int userId = user.getUserId();
		int hallId = event.getHallId();
		String title = event.getTitle();
		String description = event.getDescription();
		String date = event.getDate();
		double price = event.getPrice();
		int availableSeats = hallManager.getHallById(hallId).getCapacity();
		String txtQuery = String.format(
				"insert into events(creatorId, hallId, title, description, date, price, availableSeats) values (%d, %d, '%s', '%s', '%s', %s, %d)",
				userId, hallId, title, description, date, price, availableSeats);
		Statement statement = null;
		try {
			statement = dbConnection.createStatement();
			statement.executeQuery(txtQuery);
			currentUser.getCurrentUser().setEvents(getAllEventsByUserId(userId));
		} catch (SQLException | IOException e) {
			return false;
		}
		statement.close();
		return true;
	}

	public List<Event> getAllEvents() throws SQLException, IOException {
		List<Event> events = new ArrayList<Event>();
		String txtQuery = "select * from events e";
		Statement statement = dbConnection.createStatement();
		ResultSet rs = statement.executeQuery(txtQuery);
		while (rs.next()) {
			Event event = new Event();
			event.setEventId(rs.getInt("eventId"));
			event.setCreatorId(rs.getInt("creatorId"));
			event.setHallId(rs.getInt("hallId"));
			event.setHall(hallManager.getHallById(event.getHallId()));
			event.setTitle(rs.getString("title"));
			event.setDescription(rs.getString("description"));
			event.setPrice(rs.getDouble("price"));
			event.setDate(rs.getString("date"));
			event.setAvailableSeats(rs.getInt("availableSeats"));
			event.setLectures(lectureManager.getAllLectuersByEventId(event.getEventId()));
			events.add(event);
		}
		statement.close();
		return events;
	}

	public List<Event> getAllEventsByUserId(int userId) throws SQLException, IOException {
		List<Event> events = new ArrayList<Event>();
		String txtQuery = String.format("select * from events where events.creatorId=%s", String.valueOf(userId));
		Statement statement = dbConnection.createStatement();
		ResultSet rss = statement.executeQuery(txtQuery);
		while (rss.next()) {
			Event event = new Event();
			event.setEventId(rss.getInt("eventId"));
			event.setHall(hallManager.getHallById(rss.getInt("hallId")));
			event.setCreatorId(rss.getInt("creatorId"));
			event.setHallId(rss.getInt("hallId"));
			event.setTitle(rss.getString("title"));
			event.setDescription(rss.getString("description"));
			event.setDate(rss.getString("date"));
			event.setPrice(rss.getDouble("price"));
			event.setAvailableSeats(rss.getInt("availableSeats"));
			event.setLectures(lectureManager.getAllLectuersByEventId(event.getEventId()));
			events.add(event);
		}

		return events;
	}

	public CitiesContainer getAllCytiesWithEvent() throws SQLException, IOException {
		CitiesContainer cyties = new CitiesContainer();
		String txtQuery = "select distinct(city) from halls";
		Statement statement = dbConnection.createStatement();
		ResultSet rs = statement.executeQuery(txtQuery);
		while (rs.next()) {
			cyties.addCity(rs.getString("city"));
		}
		return cyties;
	}

	public List<Event> getAllEventsByCity(String city) throws SQLException, IOException {
		List<Event> events = new ArrayList<Event>();
		String txtQuery = String
				.format("select * from events where events.hallId IN (select hallId from halls where city='%s'", city);
		Statement statement = dbConnection.createStatement();
		ResultSet rs = statement.executeQuery(txtQuery);
		while (rs.next()) {
			Event event = new Event();
			event.setEventId(rs.getInt("eventId"));
			event.setHall(hallManager.getHallById(rs.getInt("hallId")));
			event.setCreatorId(rs.getInt("creatorId"));
			event.setHallId(rs.getInt("hallId"));
			event.setTitle(rs.getString("title"));
			event.setDescription(rs.getString("description"));
			event.setDate(rs.getString("date"));
			event.setPrice(rs.getDouble("price"));
			event.setAvailableSeats(rs.getInt("availableSeats"));
			event.setLectures(lectureManager.getAllLectuersByEventId(event.getEventId()));
			events.add(event);
		}
		statement.close();
		return events;
	}

}
