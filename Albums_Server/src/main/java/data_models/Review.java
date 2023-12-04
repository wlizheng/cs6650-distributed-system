package data_models;

public class Review {
    private String reviewID;
    private String albumID;
    private String likeOrNot;

    public Review(String reviewID, String albumID, String likeOrNot) {
        this.reviewID = reviewID;
        this.albumID = albumID;
        this.likeOrNot = likeOrNot;
    }

    public String getReviewID() {
        return reviewID;
    }

    public void setReviewID(String reviewID) {
        this.reviewID = reviewID;
    }

    public String getAlbumID() {
        return albumID;
    }

    public void setAlbumID(String albumID) {
        this.albumID = albumID;
    }

    public String getLikeOrNot() {
        return likeOrNot;
    }

    public void setLikeOrNot(String likeOrNot) {
        this.likeOrNot = likeOrNot;
    }
}

