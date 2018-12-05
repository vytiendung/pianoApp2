package com.vtd.pianoapp.songobject;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

public class Song implements Parcelable {
	public static final Creator<Song> CREATOR = new Creator<Song>() {
		public Song createFromParcel(Parcel in) {
			return new Song(in);
		}

		public Song[] newArray(int size) {
			return new Song[size];
		}
	};
	public boolean isLoadMoreItem = false;
	public boolean isLoadding = false;
	int id;
	String author = "";
	String title = "";
	String path = "";
	String cachePath = "";
	boolean isFavourite;
	int playCount;
	String startNote = "c4";
	String serverId;
	long downloadCount;
	long createdDate;
	int hand;
	int isActive;
	String objId = "";
	private String secret; // for musescore but no longer exist

	public Song() {

	}

	public Song(boolean isLoadMoreItem) {
		this.isLoadMoreItem = isLoadMoreItem;
	}

	public void setObjId(String objId){
		this.objId = objId;
	}
	public String getObjId (){
		return objId == null ? "" : objId;
	}

	public Song(int id, String author, String title, String path, boolean isFavourite, int playCount, String startNote,
	            String serverId,
	            long downloadCount, long createdDate, int hand) {
		super();
		this.id = id;
		this.author = author;
		this.title = title;
		this.path = path;
		this.isFavourite = isFavourite;
		this.playCount = playCount;
		this.startNote = startNote;
		this.serverId = serverId;
		this.downloadCount = downloadCount;
		this.createdDate = createdDate;
		this.hand = hand;
	}

	private Song(Parcel in) {
		id = in.readInt();
		author = in.readString();
		title = in.readString();
		path = in.readString();
		cachePath = in.readString();
		isFavourite = in.readByte() != 0 ? true : false;
		playCount = in.readInt();
		startNote = in.readString();
		serverId = in.readString();
		downloadCount = in.readLong();
		createdDate = in.readLong();
		hand = in.readInt();
		isActive = in.readInt();
		secret = in.readString();
		isFavourite = in.readByte() != 0 ? true : false;
		isLoadding = in.readByte() != 0 ? true : false;
		objId = in.readString();
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public int getHand() {
		return hand;
	}

	public void setHand(int hand) {
		this.hand = hand;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public long getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(long downloadCount) {
		this.downloadCount = downloadCount;
	}

	public long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}

	public String getAuthor() {
		if (author == null || author.trim() == "null")
			return "Unknown";
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCachePath() {
		return cachePath;
	}

	public void setCachePath(String cachePath) {
		this.cachePath = cachePath;
	}

	public boolean isFavourite() {
		return isFavourite;
	}

	public void setFavourite(boolean isFavourite) {
		this.isFavourite = isFavourite;
	}

	public int getPlayCount() {
		return playCount;
	}

	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	public String getStartNote() {
		return startNote;
	}

	public void setStartNote(String startNote) {
		this.startNote = startNote;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int isActive() {
		return isActive;
	}


	public void setActive(int isActive) {
		this.isActive = isActive;
	}

	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof Song) {
			Song otherSong = (Song) o;
			if (otherSong.getPath() != null && this.getPath() != null) {
				try {
					result = (this.getPath().trim().equalsIgnoreCase(otherSong.getPath().trim()));
				} catch (Exception ex){
					result = false;
				}
			}
		}
		return result;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(author);
		dest.writeString(title);
		dest.writeString(path);
		dest.writeString(cachePath);
		dest.writeByte((byte) (isFavourite ? 1 : 0));
		dest.writeInt(playCount);
		dest.writeString(startNote);
		dest.writeString(serverId);
		dest.writeLong(downloadCount);
		dest.writeLong(createdDate);
		dest.writeInt(hand);
		dest.writeInt(isActive);
		dest.writeString(secret);
		dest.writeByte((byte) (isFavourite ? 1 : 0));
		dest.writeByte((byte) (isLoadding ? 1 : 0));
		dest.writeString(objId);
	}

	public void setAttributeFromOtheSong(Song song) {
		this.id = song.id;
		this.author = song.author;
		this.title = song.title;
		this.path = song.path;
		this.cachePath = song.cachePath;
		this.isFavourite = song.isFavourite;
		this.playCount = song.playCount;
		this.startNote = song.startNote;
		this.serverId = song.serverId;
		this.downloadCount = song.downloadCount;
		this.createdDate = song.createdDate;
		this.hand = song.hand;
		this.isActive = song.isActive;
		this.secret = song.secret;
		this.isLoadMoreItem = song.isLoadMoreItem;
		this.isLoadding = song.isLoadding;
		this.objId = song.objId;
	}

	public Song(JSONObject jsonObject) throws JSONException {
		id = jsonObject.getInt("id");
		author = jsonObject.getString("author");
		title = jsonObject.getString("title");
		path = jsonObject.getString("path");
		createdDate = jsonObject.getInt("createdDate");
		hand = jsonObject.getInt("hand");
		if (jsonObject.has("startNote")) {
			startNote = jsonObject.getString("startNote");
		}
		if (jsonObject.has("cachePath")) {
			cachePath = jsonObject.getString("cachePath");
		}
	}

	JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", id);
			jsonObject.put("author", author);
			jsonObject.put("title", title);
			jsonObject.put("path", path);
			jsonObject.put("createdDate", createdDate);
			jsonObject.put("hand", hand);
			jsonObject.put("startNote", startNote);
			jsonObject.put("cachePath", cachePath);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
}
