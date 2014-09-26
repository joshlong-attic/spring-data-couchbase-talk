package springCouchbase;

import com.couchbase.client.protocol.views.ComplexKey;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springCouchbase.domain.Activity;
import springCouchbase.domain.ActivityRepository;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {CouchbaseTestConfig.class})
@WebAppConfiguration
public class ApplicationTests {

    @Autowired
    private ActivityRepository activityRepository;

    @After
    public void tearDown() throws Exception {
        activityRepository.deleteAll();
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
	public void contextLoads() {
        assertNotNull(activityRepository);
	}

    @Test
    public void testActivityRepository() throws Exception {
        Activity activity = new Activity();
        activity.setId("someId");
        activity.setContent("Tweet content");
        activity.setOrigin("twitter");
        Activity savedActivity = activityRepository.save(activity);
        assertNotNull(savedActivity);
        assertEquals(savedActivity.getOrigin(), "twitter");

        Query query = new Query();
        query.setKey(ComplexKey.of("twitter"));
        query.setStale(Stale.FALSE);
        List<Activity> activities = activityRepository.findByOrigin(query);
        assertNotNull(activities);
        assertFalse(activities.isEmpty());


    }
}
