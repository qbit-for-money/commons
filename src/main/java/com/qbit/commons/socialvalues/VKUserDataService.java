package com.qbit.commons.socialvalues;

import com.qbit.commons.crypto.util.EncryptionUtil;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;

/**
 * @author Alex
 */
public class VKUserDataService implements SocialNetworkUserDataService {
	public final static String VK_API_BASE_URL = "https://api.vk.com/method/";
	public final static String USERS_PATH = "users.get";
	public final static String WALL_PATH = "wall.get";
	public final static String PAGES_PATH = "users.getSubscriptions";
	private final JsonNode USER_JSON_NODE;
	private final JsonNode LAST_WALL_JSON_NODE;
	private final JsonNode FIRST_WALL_JSON_NODE;
	private final JsonNode PAGES_JSON_NODE;
	
	public VKUserDataService(String userId, String accessToken) throws IOException {
		Client client = ClientBuilder.newClient(new ClientConfig());
		
		Invocation.Builder builder = client.target(VK_API_BASE_URL).path(USERS_PATH)
				.queryParam("fields", "city, country, sex, bdate, counters, relatives, relation, connections")
				.queryParam("user_ids", userId)
				.queryParam("v", "5.24")
				.queryParam("access_token", accessToken)
				.request(MediaType.APPLICATION_JSON_TYPE);
		String response = builder.get(String.class);
		response = response.replace("{\"response\":[", "");
		response = response.substring(0, response.lastIndexOf("]}"));
		ObjectMapper objectMapper = new ObjectMapper();
		USER_JSON_NODE = objectMapper.readTree(response);
		
		Invocation.Builder wallBuilder = client.target(VK_API_BASE_URL).path(WALL_PATH)
				.queryParam("owner_id", userId)
				.queryParam("count", "1")
				.queryParam("v", "5.24")
				.queryParam("access_token", accessToken)
				.request(MediaType.APPLICATION_JSON_TYPE);
		String wallResponse = wallBuilder.get(String.class);
		LAST_WALL_JSON_NODE = objectMapper.readTree(wallResponse);
		int postsCount = getPostsCount();
		if(postsCount > 0) {
			postsCount -= 1;
		}
		Invocation.Builder firstWallBuilder = client.target(VK_API_BASE_URL).path(WALL_PATH)
				.queryParam("owner_id", userId)
				.queryParam("count", "1")
				.queryParam("offset", getPostsCount() - 1)
				.queryParam("v", "5.24")
				.queryParam("access_token", accessToken)
				.request(MediaType.APPLICATION_JSON_TYPE);
		String firstWallResponse = firstWallBuilder.get(String.class);
		FIRST_WALL_JSON_NODE = objectMapper.readTree(firstWallResponse);
		
		Invocation.Builder pagesBuilder = client.target(VK_API_BASE_URL).path(PAGES_PATH)
				.queryParam("user_id", userId)
				.queryParam("count", "1")
				.queryParam("extended", "0")
				.queryParam("v", "5.24")
				.queryParam("access_token", accessToken)
				.request(MediaType.APPLICATION_JSON_TYPE);
		String pagesResponse = pagesBuilder.get(String.class);
		PAGES_JSON_NODE = objectMapper.readTree(pagesResponse);
	}

	@Override
	public String getUserHash() {
		JsonNode firstName = USER_JSON_NODE.get("first_name");
		JsonNode lastName = USER_JSON_NODE.get("last_name");
		JsonNode id = USER_JSON_NODE.get("id");
		String common = firstName.asText() + lastName.asText() + id.asText();
		return EncryptionUtil.getMD5(common);
	}

	@Override
	public int getSex() {
		JsonNode sex = USER_JSON_NODE.findValue("sex");
		if ((sex == null) || (sex.asInt() == 0)) {
			return -1;
		}
		return (sex.asInt() == 1) ? 0 : 1;
	}

	@Override
	public int getAge() {
		JsonNode bDateNode = USER_JSON_NODE.findValue("bdate");
		if (bDateNode == null) {
			return -1;
		}
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Date bDate;
		try {
			bDate = dateFormat.parse(bDateNode.asText());
		} catch (ParseException ex) {
			return -1;
		}
		Calendar bdateCalendar = Calendar.getInstance();
		bdateCalendar.setTime(bDate);
		Calendar today = Calendar.getInstance();
		int age = today.get(Calendar.YEAR) - bdateCalendar.get(Calendar.YEAR);
		if (today.get(Calendar.DAY_OF_YEAR) <= bdateCalendar.get(Calendar.DAY_OF_YEAR)) {
			age--;
		}
		return age;
	}

	@Override
	public String getCity() {
		JsonNode city = USER_JSON_NODE.findPath("city").get("title");
		return (city == null) ? null : city.asText();
	}

	@Override
	public String getCountry() {
		JsonNode country = USER_JSON_NODE.findPath("country").get("title");
		return (country == null) ? null : country.asText();
	}
	
	private int getElementCount(String element) {
		JsonNode count = USER_JSON_NODE.findPath("counters").get(element);
		return (count == null) ? null : count.asInt();
	}

	@Override
	public int getAlbumsCount() {
		return getElementCount("albums");
	}

	@Override
	public int getVideoCount() {
		return getElementCount("videos");
	}

	@Override
	public int getAudioCount() {
		return getElementCount("audios");
	}

	@Override
	public int getNotesCount() {
		return getElementCount("notes");
	}

	@Override
	public int getPhotoCount() {
		return getElementCount("photos");
	}

	@Override
	public int getGroupsCount() {
		return getElementCount("groups");
	}

	@Override
	public int getFriendsCount() {
		return getElementCount("friends");
	}

	@Override
	public int getFollowersCount() {
		return getElementCount("followers");
	}

	@Override
	public String getRelatives() {
		List<String> relatives = USER_JSON_NODE.get("relatives").findValuesAsText("id");
		
		if((relatives == null) || relatives.isEmpty()) {
			return "";
		}
		String relativesIds = "";
		for (String id : relatives) {
			relativesIds += id + ",";
		}
		if(!relativesIds.isEmpty()) {
			relativesIds = relativesIds.substring(0, relativesIds.length() - 1);
		}
		return relativesIds;
	}

	@Override
	public String getFamilyStatus() {
		JsonNode familyStatus = USER_JSON_NODE.get("relation");
		if(familyStatus == null) { 
			return null;
		}
		switch(familyStatus.asInt()) {
			case 1 : return "не женат/не замужем";
			case 2 : return "есть друг/есть подруга";
			case 3 : return "помолвлен/помолвлена";
			case 4 : return "женат/замужем";
			case 5 : return "всё сложно";
			case 6 : return "в активном поиске";
			case 7 : return "влюблён/влюблена";
			default: return null;
		}
	}
	
	private String getElement(String nodeStr) {
		JsonNode node = USER_JSON_NODE.get(nodeStr);
		return (node == null) ? null : node.asText();
	}

	@Override
	public String getSkype() {
		return getElement("skype");
	}

	@Override
	public String getFacebook() {
		return getElement("facebook");
	}

	@Override
	public String getTwitter() {
		return getElement("twitter");
	}

	@Override
	public String getInstagram() {
		return getElement("instagram");
	}

	@Override
	public String getLivejournal() {
		return getElement("livejournal");
	}

	@Override
	public int getPostsCount() {
		JsonNode count = LAST_WALL_JSON_NODE.findPath("response").get("count");
		return (count == null) ? -1 : count.asInt();
	}
	
	private String getPostDate(JsonNode jsonNode) {
		JsonNode date = jsonNode.findPath("items").findValue("date");
		if(date == null) {
			return null;
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		Date lastDate = new Date(date.asLong() * 1000);
		return dateFormat.format(lastDate);
	}

	@Override
	public String getLastPostDate() {
		String date = getPostDate(LAST_WALL_JSON_NODE);
		System.out.println("LAST DATE: " + date);
		return date;
	}

	@Override
	public String getFirstPostDate() {
		String date = getPostDate(FIRST_WALL_JSON_NODE);
		System.out.println("FIRST DATE: " + date);
		return date;
	}
	
	@Override
	public int getSubscriptionsCount() {
		JsonNode count = PAGES_JSON_NODE.findPath("users").get("count");
		return (count == null) ? null : count.asInt();
	}
	
	@Override
	public int getPagesCount() {
		JsonNode count = PAGES_JSON_NODE.findPath("groups").get("count");
		return (count == null) ? null : count.asInt();
	}
}
