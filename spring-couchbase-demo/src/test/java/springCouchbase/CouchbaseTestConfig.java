package springCouchbase;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ldoguin on 26/09/14.
 */
@Configuration
@EnableCouchbaseRepositories
public class CouchbaseTestConfig extends AbstractCouchbaseConfiguration {

    @Override
    protected List<String> bootstrapHosts() {
        return Arrays.asList("127.0.0.1");
    }

    @Override
    protected String getBucketName() {
        return "test";
    }

    @Override
    protected String getBucketPassword() {
        return "";
    }

}
