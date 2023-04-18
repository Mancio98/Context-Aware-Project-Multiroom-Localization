package com.example.multiroomlocalization;

import android.app.Activity;
import android.content.Context;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ListSongAdapter extends ArrayAdapter<MyAudioTrack> {


    Activity context;
    ArrayList<ImageButton> playButtons;

    public ListSongAdapter(int resource, Context context, @NonNull List<MyAudioTrack> objects, Activity activity) {
        super(context, resource,objects);
        this.context = activity;
        playButtons = new ArrayList<>();

    }

    public ListSongAdapter(@NonNull Context context, int resource, @NonNull List<MyAudioTrack> objects, Activity activity) {
        super(context, resource, objects);
        this.context = activity;
        playButtons = new ArrayList<>();
    }

    public ArrayList<ImageButton> getPlayButtons() {
        return playButtons;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.list_tracks_layout, parent, false);

        MyAudioTrack track = getItem(position);

        TextView name = (TextView) listItem.findViewById(R.id.song_title);

        name.setText(String.format("%s %s", track.getTitle(), ""));
        TextView artist = (TextView) listItem.findViewById(R.id.song_artist);

        artist.setText(String.format("%s %s", track.getAuthor(), ""));
        ImageButton play = (ImageButton) listItem.findViewById(R.id.play_list);
        playButtons.add(play);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pbState = MediaControllerCompat.getMediaController(context).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {

                    MediaControllerCompat.getMediaController(context).getTransportControls().pause();

                }
                else
                    MediaControllerCompat.getMediaController(context).getTransportControls().playFromMediaId(String.valueOf(position),null);
                    //TODO change icon on pause
            }
        });

        return listItem;
    }
}
