package springCouchbase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;
import springCouchbase.domain.ActivityService;

/**
 * Created by ldoguin on 25/09/14.
 */
@Controller
public class Runner implements CommandLineRunner{

        @Autowired
        private ActivityService activityService;


        @Override
        public void run(String... strings) throws Exception {
            activityService.doWork();;
        }

}
