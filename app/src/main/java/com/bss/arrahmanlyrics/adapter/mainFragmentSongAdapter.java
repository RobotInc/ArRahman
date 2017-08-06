package com.bss.arrahmanlyrics.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bss.arrahmanlyrics.Fragments.songs;
import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.models.songWithTitle;
import com.bss.arrahmanlyrics.utils.FirstLetterUpperCase;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import me.piruin.quickaction.ActionItem;
import me.piruin.quickaction.QuickAction;

/**
 * Created by mohan on 5/20/17.
 */

public class mainFragmentSongAdapter extends RecyclerView.Adapter<mainFragmentSongAdapter.MyViewHolder>{
	private QuickAction quickAction;
	private QuickAction quickIntent;
	private Context mContext;
	private List<songWithTitle> songlist;
	private songs s;

	public mainFragmentSongAdapter(Context context, songs songs, List<songWithTitle> songlist) {
		this.mContext = context;
		this.songlist = songlist;
		this.s = songs;

		//QuickAction.setDefaultColor(ResourcesCompat.getColor(s.getResources(), R.color.white, null));
		//QuickAction.setDefaultTextColor(Color.BLACK);





	}


	public class MyViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
		public TextView name, lyricist, movietitle;
		ImageView dots;
		CircularImageView imageView;
		LinearLayout layout;
		ImageView icon;

		public MyViewHolder(View view) {
			super(view);
			name = (TextView) view.findViewById(R.id.MainSongtitle);
			lyricist = (TextView) view.findViewById(R.id.MainSonglyricist);
			movietitle = (TextView) view.findViewById(R.id.MainMovieTitle);
			dots = (ImageButton) view.findViewById(R.id.menu_button);
			imageView = (CircularImageView) view.findViewById(R.id.songCover);
			layout = (LinearLayout) view.findViewById(R.id.songholder);
			icon = (ImageView) view.findViewById(R.id.playhingicon);

			layout.setOnClickListener(this);
			dots.setOnClickListener(this);




			//albumCover = (ImageView) view.findViewById(R.id.album_artwork);
		}

		@Override
		public void onClick(View v) {
			final int id = v.getId();
			quickAction = new QuickAction(v.getContext(), QuickAction.VERTICAL);
			ActionItem play= new ActionItem(1,"Play",R.drawable.playicon);
			ActionItem playAll= new ActionItem(2,"Play All",R.drawable.playall);
			ActionItem queue= new ActionItem(3,"Add to Queue",R.drawable.queue);
			ActionItem fav= new ActionItem(4,"Add to Favorite",R.drawable.favadd);
			ActionItem favremove= new ActionItem(5,"Remove From Favorite",R.drawable.favremove);

			//add action items into QuickAction
			quickAction.addActionItem(play);
			quickAction.addActionItem(playAll);
			quickAction.addActionItem(queue);
			quickAction.addActionItem(fav);
			quickAction.addActionItem(favremove);


			quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
				@Override
				public void onItemClick(ActionItem item) {
					if(item.getActionId()==1){
						s.play(getmodel(movietitle.getText().toString(),name.getText().toString()));
						icon.setVisibility(View.VISIBLE);
					}else if(item.getActionId()==2){
						s.playAll(getmodel(movietitle.getText().toString(),name.getText().toString()));
					}else if(item.getActionId()==3){
						s.addToQueue(getmodel(movietitle.getText().toString(),name.getText().toString()));
					}else if(item.getActionId()==4){
						//songWithTitle song = getmodel(movietitle.getText().toString(),name.getText().toString());
						s.addToFavorite(getmodel(movietitle.getText().toString(),name.getText().toString()));
					}else if(item.getActionId()==5){
						s.removeFromFavorite(getmodel(movietitle.getText().toString(),name.getText().toString()));
					}
				}
			});


			if(R.id.menu_button == id){
				quickAction.show(v);

			}
			if(R.id.songholder == id){
				s.play(getmodel(movietitle.getText().toString(),name.getText().toString()));
			}


		}
	}



	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.main_song_list_view, parent, false);
		RecyclerView.ViewHolder holder = new MyViewHolder(itemView);

		return (MyViewHolder) holder;
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, int position) {
		songWithTitle actualsong = songlist.get(position);

		final Typeface title = Typeface.createFromAsset(mContext.getAssets(), "MavenPro.ttf");
		final Typeface lyricist = Typeface.createFromAsset(mContext.getAssets(), "MavenPro.ttf");
		holder.name.setTypeface(title);
		holder.lyricist.setTypeface(lyricist);

		holder.name.setText(FirstLetterUpperCase.convert(actualsong.getSongTitle()));
		//holder.name.setText(actualsong.getSongTitle());
		Glide.with(mContext).load(actualsong.getImages()).into(holder.imageView);

		holder.lyricist.setText(FirstLetterUpperCase.convert("Lyricist: " + actualsong.getLyricistName()));
		holder.movietitle.setText(FirstLetterUpperCase.convert("Movie: "+actualsong.getMovietitle()));

		//holder.lyricist.setText("Lyricist : " + actualsong.getLyricistNames());

		/*holder.dots.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				//creating a popup menu
				PopupMenu popup = new PopupMenu(mContext, holder.dots);
				//inflating menu from xml resource
				popup.inflate(R.menu.songlistmenu);
				//adding click listener
				/*popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch (item.getItemId()) {
							case R.id.menu1:
								//handle menu1 click
								break;
							case R.id.menu2:
								//handle menu2 click
								break;
							case R.id.menu3:
								//handle menu3 click
								break;
						}
						return false;
					}
				});
				//displaying the popup
				popup.show();

			}
		});*/

	}


	@Override
	public int getItemCount() {
		return songlist.size();
	}

	public songWithTitle getmodel(String movie,String songTitle){
		movie = movie.replaceAll("Movie: ","");
		for(songWithTitle songwith:songlist){

			if(songwith.getMovietitle().trim().equalsIgnoreCase(movie)&&songwith.getSongTitle().trim().equalsIgnoreCase(songTitle)){

				return songwith;
			}
		}

		return null;
	}

	public String FirstLetterUpperCase(String source) {
		source = source.toLowerCase();

		StringBuffer res = new StringBuffer();

		String[] strArr = source.split(" ");
		for (String str : strArr) {
			char[] stringArray = str.trim().toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			str = new String(stringArray);

			res.append(str).append(" ");
		}

		return res.toString().trim();
	}

	public void setFilter(List<songWithTitle> songlists) {
		songlist = new ArrayList<>();
		songlist.addAll(songlists);
		notifyDataSetChanged();
	}


}
