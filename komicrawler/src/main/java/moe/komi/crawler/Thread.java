package moe.komi.crawler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Thread implements Serializable {
	public ArrayList<Post> posts = new ArrayList<>();

    public String res;

	public Integer ignoredNumber = 0;

    public long uuid;

    public Thread() { uuid = UUID.randomUUID().hashCode(); }

	public Thread(ArrayList<Post> posts){
		this.posts = posts;
	}
	
	public Integer getPostCount(){
		return ignoredNumber + posts.size();
	}

    public Integer getReplyCount() { return getPostCount() - 1; }

    public void addPost(Post post) { posts.add(post); }

    public void print() {
        System.out.printf("Thread No.%s Posts.%d\n", posts.get(0).no, posts.size());
    }
}
