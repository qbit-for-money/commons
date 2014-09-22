package com.qbit.commons.socialvalues;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alex
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SocialValueRequest {

	@XmlElement(name = "user_hash")
	private String userHash;
	private int sex;
	private int age;
	private String city;
	private String country;
	@XmlElement(name = "albums_count")
	private int albumsCount;
	@XmlElement(name = "video_count")
	private int videoCount;
	@XmlElement(name = "audio_count")
	private int audioCount;
	@XmlElement(name = "notes_count")
	private int notesCount;
	@XmlElement(name = "photo_count")
	private int photoCount;
	@XmlElement(name = "groups_count")
	private int groupsCount;
	@XmlElement(name = "friends_count")
	private int friendsCount;
	@XmlElement(name = "followers_count")
	private int followersCount;
	@XmlElement(name = "pages_count")
	private int pagesCount;
	private String relativities;
	@XmlElement(name = "family_status")
	private String familyStatus;
	private String skype;
	private String facebook;
	private String twitter;
	private String livejournal;
	private String instagram;
	@XmlElement(name = "last_post_dt")
	private String lastPostDt;
	@XmlElement(name = "first_post_dt")
	private String firstPostDt;
	@XmlElement(name = "posts_count")
	private int postsCount;
	@XmlElement(name = "subscriptions_count")
	private int subscriptionsCount;
	@XmlElement(name = "likes_count")
	private int likesCount;
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUserHash() {
		return userHash;
	}

	public void setUserHash(String userHash) {
		this.userHash = userHash;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getAlbumsCount() {
		return albumsCount;
	}

	public void setAlbumsCount(int albumsCount) {
		this.albumsCount = albumsCount;
	}

	public int getVideoCount() {
		return videoCount;
	}

	public void setVideoCount(int videoCount) {
		this.videoCount = videoCount;
	}

	public int getAudioCount() {
		return audioCount;
	}

	public void setAudioCount(int audioCount) {
		this.audioCount = audioCount;
	}

	public int getNotesCount() {
		return notesCount;
	}

	public void setNotesCount(int notesCount) {
		this.notesCount = notesCount;
	}

	public int getPhotoCount() {
		return photoCount;
	}

	public void setPhotoCount(int photoCount) {
		this.photoCount = photoCount;
	}

	public int getGroupsCount() {
		return groupsCount;
	}

	public void setGroupsCount(int groupsCount) {
		this.groupsCount = groupsCount;
	}

	public int getFriendsCount() {
		return friendsCount;
	}

	public void setFriendsCount(int friendsCount) {
		this.friendsCount = friendsCount;
	}

	public int getFollowersCount() {
		return followersCount;
	}

	public void setFollowersCount(int followersCount) {
		this.followersCount = followersCount;
	}

	public int getPagesCount() {
		return pagesCount;
	}

	public void setPagesCount(int pagesCount) {
		this.pagesCount = pagesCount;
	}

	public String getRelativities() {
		return relativities;
	}

	public void setRelativities(String relativities) {
		this.relativities = relativities;
	}

	public String getFamilyStatus() {
		return familyStatus;
	}

	public void setFamilyStatus(String familyStatus) {
		this.familyStatus = familyStatus;
	}

	public String getSkype() {
		return skype;
	}

	public void setSkype(String skype) {
		this.skype = skype;
	}

	public String getFacebook() {
		return facebook;
	}

	public void setFacebook(String facebook) {
		this.facebook = facebook;
	}

	public String getTwitter() {
		return twitter;
	}

	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}

	public String getLivejournal() {
		return livejournal;
	}

	public void setLivejournal(String livejournal) {
		this.livejournal = livejournal;
	}

	public String getInstagram() {
		return instagram;
	}

	public void setInstagram(String instagram) {
		this.instagram = instagram;
	}

	public String getLastPostDt() {
		return lastPostDt;
	}

	public void setLastPostDt(String lastPostDt) {
		this.lastPostDt = lastPostDt;
	}

	public String getFirstPostDt() {
		return firstPostDt;
	}

	public void setFirstPostDt(String firstPostDt) {
		this.firstPostDt = firstPostDt;
	}

	public int getPostsCount() {
		return postsCount;
	}

	public void setPostsCount(int postsCount) {
		this.postsCount = postsCount;
	}

	public int getSubscriptionsCount() {
		return subscriptionsCount;
	}

	public void setSubscriptionsCount(int subscriptionsCount) {
		this.subscriptionsCount = subscriptionsCount;
	}

	public int getLikesCount() {
		return likesCount;
	}

	public void setLikesCount(int likesCount) {
		this.likesCount = likesCount;
	}

	@Override
	public String toString() {
		return "SocialValueRequest{" + "userHash=" + userHash + ", sex=" + sex + ", age=" + age + ", city=" + city + ", country=" + country + ", albumsCount=" + albumsCount + ", videoCount=" + videoCount + ", audioCount=" + audioCount + ", notesCount=" + notesCount + ", photoCount=" + photoCount + ", groupsCount=" + groupsCount + ", friendsCount=" + friendsCount + ", followersCount=" + followersCount + ", pagesCount=" + pagesCount + ", relativities=" + relativities + ", familyStatus=" + familyStatus + ", skype=" + skype + ", facebook=" + facebook + ", twitter=" + twitter + ", livejournal=" + livejournal + ", instagram=" + instagram + ", lastPostDt=" + lastPostDt + ", firstPostDt=" + firstPostDt + ", postsCount=" + postsCount + ", subscriptionsCount=" + subscriptionsCount + ", likesCount=" + likesCount + '}';
	}
}
