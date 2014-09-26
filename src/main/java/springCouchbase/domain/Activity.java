package springCouchbase.domain;

import org.springframework.data.annotation.Id;

/**
 * Created by ldoguin on 25/09/14.
 */
public class Activity {

    @Id
    String id;
    String content;
    String origin;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}

