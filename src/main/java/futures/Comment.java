package futures;

public class Comment {
    private int postId;
    private int id;
    private String name;
    private String email;
    private String body;

    @Override
    public String toString() {
        return "Comment{" +
                "postId=" + postId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    public int getPostId() {
        return postId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
