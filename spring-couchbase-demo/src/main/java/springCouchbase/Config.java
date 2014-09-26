package springCouchbase;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ldoguin on 25/09/14.
 */
@Configuration
@EnableCouchbaseRepositories
public class Config extends AbstractCouchbaseConfiguration {

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

}
