package springCouchbase.domain;

import com.couchbase.client.protocol.views.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by ldoguin on 25/09/14.
 */
public interface ActivityRepository extends CrudRepository<Activity, String> {

    List<Activity> findByOrigin(Query query);
}
