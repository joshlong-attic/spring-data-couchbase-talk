package demo;

import com.couchbase.client.protocol.views.Query;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${couchbase.cluster.bucket}")
    private String bucketName;

    @Value("${couchbase.cluster.password}")
    private String password;

    @Value("${couchbase.cluster.ip}")
    private String ip;

    @Override
    protected List<String> bootstrapHosts() {
        return Arrays.asList(ip);
    }

    @Override
    protected String getBucketName() {
        return this.bucketName;
    }

    @Override
    protected String getBucketPassword() {
        return this.password;
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