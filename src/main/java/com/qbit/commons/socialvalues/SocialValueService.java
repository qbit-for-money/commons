package com.qbit.commons.socialvalues;

import java.io.IOException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;

/**
 * @author Alex
 */
public class SocialValueService {
	public final static String SOCIALVALUES_API_BASE_URL = "http://socialvalues.co/api/estimate";
	public static long getValue(SocialNetworkUserDataService service) {
		Client client = ClientBuilder.newClient(new ClientConfig());
		Invocation.Builder socialBuilder = client.target(SOCIALVALUES_API_BASE_URL)
				.request(MediaType.APPLICATION_FORM_URLENCODED);
		
		MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
		map.add("token", "fc9c3560373611e4a50104011c471c01");
		map.add("user_hash", service.getUserHash());
		map.add("sex", String.valueOf(service.getSex()));
		map.add("age", String.valueOf(service.getAge()));
		map.add("city", service.getCity());
		map.add("country", service.getCountry());
		map.add("albums_count", String.valueOf(service.getAlbumsCount()));
		map.add("video_count", String.valueOf(service.getVideoCount()));
		map.add("audio_count", String.valueOf(service.getAudioCount()));
		map.add("notes_count", String.valueOf(service.getNotesCount()));
		map.add("photo_count", String.valueOf(service.getPhotoCount()));
		map.add("groups_count", String.valueOf(service.getGroupsCount()));
		map.add("friends_count", String.valueOf(service.getFriendsCount()));
		map.add("followers_count", String.valueOf(service.getFollowersCount()));
		map.add("pages_count", String.valueOf(123));//getPagesCount(jsonNode)));
		map.add("relativities", service.getRelatives());
		map.add("family_status", "не женат/не замужем");//getFamilyStatus(jsonNode));
		map.add("skype", service.getSkype());
		map.add("facebook", service.getFacebook());
		map.add("twitter", service.getTwitter());
		map.add("livejournal", service.getLivejournal());
		map.add("instagram", service.getInstagram());
		map.add("last_post_dt", service.getLastPostDate());
		map.add("first_post_dt", service.getFirstPostDate());
		map.add("posts_count", String.valueOf(service.getPostsCount()));
		//map.add("subscriptions_count", String.valueOf(getSubscriptionsCount(jsonNode)));
		map.add("likes_count", String.valueOf(service.getGroupsCount()));
		
		String response = socialBuilder.post(Entity.form(map), String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(response);
		} catch (IOException ex) {
			throw new WebApplicationException();
		}
		
		return getValue(jsonNode);
	}
	
	private static int getValue(JsonNode jsonNode) {
		if(jsonNode == null) {
			return 0;
		}
		JsonNode value = jsonNode.get("value");
		return (value == null) ? 0 : value.asInt();
	}
}
