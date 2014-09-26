package springCouchbase.domain;

import com.couchbase.client.protocol.views.ComplexKey;
import com.couchbase.client.protocol.views.Query;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Created by ldoguin on 25/09/14.
 */
@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    private Logger log = Logger.getLogger(ActivityService.class);


    public void doWork() {

    }

}
