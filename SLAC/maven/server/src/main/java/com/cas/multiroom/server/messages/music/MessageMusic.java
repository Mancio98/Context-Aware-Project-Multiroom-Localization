package com.cas.multiroom.server.messages.music;

import com.cas.multiroom.server.messages.Message;

import java.util.List;

import com.cas.multiroom.server.database.MyAudioTrack;

public class MessageMusic extends Message {
	
	public List<MyAudioTrack> songs;
	
	public MessageMusic(List<MyAudioTrack> songs) {
		super("REQ_PLAYLIST");
		this.songs = songs;
	}
	
	public List<MyAudioTrack> getSong() {
		return this.songs;
	}
	
	public void setSong(List<MyAudioTrack> songs) {
		this.songs = songs;
	}
}
