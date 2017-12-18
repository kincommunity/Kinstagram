package kin.com.kinstagram;

/**
 * Created by kyungsoohong on 12/13/17.
 */

public class FeedInfo {
    private String name, imageUrl, timeStamp;

    public FeedInfo() {

    }

    public FeedInfo(String name, String imageUrl, String timeStamp) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.timeStamp = timeStamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
