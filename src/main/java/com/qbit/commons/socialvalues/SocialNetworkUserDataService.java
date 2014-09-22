package com.qbit.commons.socialvalues;

/**
 * @author Alex
 */
public interface SocialNetworkUserDataService {
	String getUserHash();
	int getSex();
	int getAge();
	String getCity();
	String getCountry();
	int getAlbumsCount();
	int getVideoCount();
	int getAudioCount();
	int getNotesCount();
	int getPhotoCount();
	int getGroupsCount();
	int getFriendsCount();
	int getFollowersCount();
	String getRelatives();
	String getFamilyStatus();
	String getSkype();
	String getFacebook();
	String getTwitter();
	String getInstagram();
	String getLivejournal();
	int getPostsCount();
	String getLastPostDate();
	String getFirstPostDate();
	int getSubscriptionsCount();
	int getPagesCount();
}
