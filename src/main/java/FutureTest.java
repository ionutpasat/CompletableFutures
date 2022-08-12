import com.google.gson.Gson;
import futures.Comment;
import futures.Post;
import futures.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FutureTest {
    static HttpClient httpClient = HttpClient.newHttpClient();
    static Gson gson = new Gson();


    public static void getAllPostEntriesWithUserId3() throws URISyntaxException, ExecutionException, InterruptedException {
        URI uri = new URI("https://jsonplaceholder.typicode.com/posts");
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    List<Post> posts = Arrays.asList(gson.fromJson(response.body(), Post[].class));
                    posts = posts.stream()
                            .filter(post -> post.getUserId() == 3).collect(Collectors.toList());
                    for (Post post : posts) {
                        System.out.print("-> ");
                        System.out.println(post);
                    }
                })
                .get();
    }

    public static void getAllCommentsPrintEmailOfId4() throws URISyntaxException, ExecutionException, InterruptedException {
        URI uri = new URI("https://jsonplaceholder.typicode.com/comments");
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    List<Comment> comments = Arrays.asList(gson.fromJson(response.body(), Comment[].class));
                    List<String> filteredEmails = comments.stream()
                            .filter(comment -> comment.getPostId() == 4)
                            .map(Comment::getEmail).toList();
                    System.out.println(filteredEmails);
                })
                .get();
    }

    public static List<Integer> getPostIdFromUserId3() throws URISyntaxException, ExecutionException, InterruptedException {
        URI uri = new URI("https://jsonplaceholder.typicode.com/posts");
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    List<Post> posts = Arrays.asList(gson.fromJson(response.body(), Post[].class));
                    return posts.stream()
                            .filter(post -> post.getUserId() == 3)
                            .map(Post::getId).collect(Collectors.toList());
                })
                .get();
    }

    public static void deletePostsWithUserId3() throws URISyntaxException, ExecutionException, InterruptedException {
        List<Integer> idList = getPostIdFromUserId3();
        assert idList != null;
        for (Integer id : idList) {
            URI uri = new URI("https://jsonplaceholder.typicode.com/posts/" + id);
            HttpRequest httpRequest = HttpRequest.newBuilder(uri).DELETE().build();
            httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(System.out::println)
                    .get();
        }
    }

    public static String getUserURIFromPost() throws URISyntaxException, ExecutionException, InterruptedException {
        URI uriTitle = new URI("https://jsonplaceholder.typicode.com/posts/4");
        HttpRequest httpRequestTitle = HttpRequest.newBuilder(uriTitle).build();
        return httpClient.sendAsync(httpRequestTitle, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    Post post = gson.fromJson(response.body(), Post.class);
                    return "https://jsonplaceholder.typicode.com/users/" + post.getUserId();
                }).get();
    }

    public static void getDetailsPostId4() throws URISyntaxException, ExecutionException, InterruptedException {
        URI uriTitle = new URI("https://jsonplaceholder.typicode.com/posts/4");
        HttpRequest httpRequestTitle = HttpRequest.newBuilder(uriTitle).build();
        CompletableFuture<String> completableFutureTitle = httpClient
                .sendAsync(httpRequestTitle, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    Post post = gson.fromJson(response.body(), Post.class);
                    return post.getTitle();
                });
        URI uriUser = new URI(getUserURIFromPost());
        HttpRequest httpRequestUser = HttpRequest.newBuilder(uriUser).build();
        CompletableFuture<String> completableFutureUser = httpClient
                .sendAsync(httpRequestUser, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    User user = gson.fromJson(response.body(), User.class);
                    return user.getName();
                });
        URI uriComments = new URI("https://jsonplaceholder.typicode.com/comments");
        HttpRequest httpRequestComments = HttpRequest.newBuilder(uriComments).build();
        CompletableFuture<List<String>> completableFutureComments = httpClient
                .sendAsync(httpRequestComments, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    List<Comment> comments = Arrays.asList(gson.fromJson(response.body(), Comment[].class));
                    return comments.stream()
                            .filter(comment -> comment.getPostId() == 4)
                            .map(Comment::getName).toList();
                });
        completableFutureTitle.thenCombine(completableFutureUser,
                        (title, name) -> "PostTitle: " + title + "\nUserName: " + name + "\n")
                .thenCombine(completableFutureComments,
                        (titleAndName, commentList) -> {
                            StringBuilder result = new StringBuilder(titleAndName);
                            for (String comment : commentList) {
                                result.append(comment).append("\n");
                            }
                            return result;
                        })
                .thenAccept(System.out::println)
                .get();
    }

    public static void main(String[] args) {
        try {
            System.out.println("------------------------(Posts with userId 3)--------------------------------------");
            getAllPostEntriesWithUserId3();
            System.out.println("\n---------------------------(Filtered Emails)-------------------------------------");
            getAllCommentsPrintEmailOfId4();
            System.out.println("\n------------------------------(Deleted posts)------------------------------------");
            deletePostsWithUserId3();
            System.out.println("\n------------------------(Details of post with id 4)------------------------------");
            getDetailsPostId4();
        } catch (URISyntaxException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
