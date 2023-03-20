package com.example.multiroomlocalization;

import android.app.Activity;
import android.content.Context;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ListSongAdapter extends ArrayAdapter<myAudioTrack> {


    Activity context;
    ArrayList<ImageButton> playButtons;

    public ListSongAdapter(int resource, Context context, @NonNull List<myAudioTrack> objects, Activity activity) {
        super(context, resource,objects);
        this.context = activity;

    }

    public ListSongAdapter(@NonNull Context context, int resource, @NonNull List<myAudioTrack> objects, Activity activity) {
        super(context, resource, objects);
        this.context = activity;
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


        myAudioTrack track = getItem(position);

        TextView name = (TextView) listItem.findViewById(R.id.song_title);

        name.setText(String.format("%s %s", track.getTitle(), track.getAuthor()));
        ImageButton play = (ImageButton) listItem.findViewById(R.id.play_list);
        playButtons.add(play);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pbState = MediaControllerCompat.getMediaController(context).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {

                    MediaControllerCompat.getMediaController(context).getTransportControls().pause();
                    //TODO change icon on play
                }
                else
                    MediaControllerCompat.getMediaController(context).getTransportControls().playFromMediaId(String.valueOf(position),null);
                    //TODO change icon on pause
            }
        });

        return listItem;
    }
}
