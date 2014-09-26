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

    private final ActivityRepository activityRepository;
    private Logger log = Logger.getLogger(ActivityService.class);
    @Autowired
    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }


    public void doWork() {
        activityRepository.deleteAll();

        Activity activity = new Activity();
        activity.setId("id1");
        activity.setContent("someone tweeted this");
        activity.setOrigin("twitter");

        activity = activityRepository.save(activity);

        Query query = new Query();
        query.setKey(ComplexKey.of("twitter"));
        List<Activity> activities = activityRepository.findByOrigin(query);
        for (Activity act : activities){
            log.info(act.getContent());
        }

    }

}
