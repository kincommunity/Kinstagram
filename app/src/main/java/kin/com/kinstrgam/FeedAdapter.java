package kin.com.kinstrgam;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by kyungsoohong on 12/13/17.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ImageViewHolder> {

    private static final String TAG = "FeedAdapter";

    private Context _context;
    private List<FeedInfo> _feedInfoList;

    public FeedAdapter(Context context, List<FeedInfo> feedInfoList) {
        this._context = context;
        this._feedInfoList = feedInfoList;
    }

    @Override
    public FeedAdapter.ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_feed, viewGroup, false);

        ImageViewHolder imageViewHolder = new ImageViewHolder(view);

        return imageViewHolder;
    }

    @Override
    public void onBindViewHolder(FeedAdapter.ImageViewHolder imageViewHolder, int position) {
        final FeedInfo feedInfo = _feedInfoList.get(position);

        imageViewHolder.imageNameTextView.setText(feedInfo.getName());
        imageViewHolder.timeStampTextView.setText(feedInfo.getTimeStamp());
        imageViewHolder.cardPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FeedAdapter"," Card Pay selected "+ feedInfo.getName() + " --> " + feedInfo.getImageUrl());
            }
        });


        Picasso.with(_context).load(feedInfo.getImageUrl()).into(imageViewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return _feedInfoList.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView imageNameTextView;
        public TextView timeStampTextView;
        public ImageView cardPayView;

        public ImageViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.cardView_image);

            imageNameTextView = (TextView) itemView.findViewById(R.id.cardView_name);

            timeStampTextView = (TextView) itemView.findViewById(R.id.cardView_timestamp);

            cardPayView = (ImageView) itemView.findViewById(R.id.cardView_pay);
        }
    }
}
