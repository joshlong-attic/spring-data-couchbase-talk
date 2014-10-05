package demo;

import com.couchbase.client.protocol.views.Query;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.repository.CrudRepository;

import java.util.Arrays;
import java.util.List;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application extends AbstractCouchbaseConfiguration {

    @Override
    protected List<String> bootstrapHosts() {
        return Arrays.asList("127.0.0.1");
    }

    @Override
    protected String getBucketName() {
        return "default";
    }

    @Override
    protected String getBucketPassword() {
        return "";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

interface ActivityRepository extends CrudRepository<Activity, String> {
    List<Activity> findByOrigin(Query query);
}

class Activity {

    @Id
    private String id;
    private String content;
    private String origin;


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