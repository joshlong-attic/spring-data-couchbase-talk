package demo;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.cache.CouchbaseCacheManager;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.couchbase.core.mapping.event.ValidatingCouchbaseEventListener;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.Page;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableCouchbaseRepositories
@EnableScheduling
@EnableCaching
public class Application extends AbstractCouchbaseConfiguration {

    @Value("${couchbase.cluster.bucket}")
    private String bucketName;

    @Value("${couchbase.cluster.password}")
    private String password;

    @Value("${couchbase.cluster.ip}")
    private String ip;

    @Value("${facebook.accessToken}")
    private String accessToken;

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

    @Bean
    Facebook facebook() {
        return new FacebookTemplate(
                accessToken);
    }

    @Bean
    CouchbaseCacheManager cacheManager(CouchbaseClient couchbaseClient) throws Exception {
        HashMap<String, CouchbaseClient> instances = new HashMap<>();
        instances.put("places", couchbaseClient);
        return new CouchbaseCacheManager(instances);
    }

    @Bean
    LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    ValidatingCouchbaseEventListener validationEventListener() {
        return new ValidatingCouchbaseEventListener(validator());
    }


    @Bean
    CommandLineRunner commandLineRunner(
            CouchbaseClient couchbaseClient,
            PlaceRepository placeRepository,
            CouchbaseTemplate couchbaseTemplate,
            PlaceService placeService) {
        return args -> {
            couchbaseClient.flush().get();

            placeService.search("Starbucks", 37.752494, -122.414166, 5280)
                    .forEach(id -> {
                        System.out.println(json(placeRepository.findOne(id)));
                    });


            // use the Spring Session integration

            //
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private String json(Object o) {
        StringWriter stringWriter = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(stringWriter, o);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

@Service
class PlaceService {

    private final CacheManager cacheManager;

    private final Facebook facebook;

    private final CouchbaseTemplate couchbaseTemplate;

    private final PlaceRepository placeRepository;

    @Autowired
    PlaceService(CacheManager cacheManager, Facebook facebook, CouchbaseTemplate couchbaseTemplate, PlaceRepository placeRepository) {
        this.cacheManager = cacheManager;
        this.facebook = facebook;
        this.couchbaseTemplate = couchbaseTemplate;
        this.placeRepository = placeRepository;
    }

    @Scheduled(fixedDelay = 4000)
    void janitor() {

        Date date = new Date(System.currentTimeMillis() - (5 * 1000  ));
        Query query = new Query();
        query.setStale(Stale.FALSE);
        query.setRangeEnd(Long.toString(date.getTime()));

        placeRepository.findByInsertionDate(query).forEach(System.out::println);
        System.out.println("hello, world! ");
    }

    @Cacheable(value = "places", key = "#query")
    public List<String> search(String query, double lat, double lon, int distance) {
        return facebook.placesOperations()
                .search(query, lat, lon, distance)
                .stream()
                .map(p -> {
                    Place objectToInsert = new Place(p);
                    couchbaseTemplate.insert(objectToInsert);
                    return objectToInsert;
                })
                .map(Place::getId)
                .collect(Collectors.toList());
    }
}

interface PlaceRepository extends CrudRepository<Place, String> {

    Collection<Place> findByFirstName(Query query);

    Collection<Place> findByInsertionDate(Query query);
}


@Document(expiry = 0)
class Place {
    @Field
    private Location location;

    @Field
    private String name, affilitation, category, description, about;

    @Field
    private Date insertionDate;

    @Id
    private String id;

    public String getName() {
        return name;
    }

    public String getAffilitation() {
        return affilitation;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getAbout() {
        return about;
    }

    public String getId() {
        return id;
    }

    public Date getInsertionDate() {
        return insertionDate;
    }

    Place(Page p) {
        this.affilitation = p.getAffiliation();
        this.id = p.getId();
        this.name = p.getName();
        this.category = p.getCategory();
        this.description = p.getDescription();
        this.about = p.getAbout();
        this.insertionDate = new Date();
        org.springframework.social.facebook.api.Location pageLocation = p.getLocation();
        this.location = new Location(pageLocation);
    }

    Place() {
    }

    public Location getLocation() {
        return location;
    }
}

class Location {
    Location() {
    }

    private String city;
    private String country;
    private String description;
    private double latitude;
    private double longitude;
    private String state;
    private String street;
    private String zip;

    public String getCity() {
        return city;
    }

    Location(org.springframework.social.facebook.api.Location pageLocation) {
        this(pageLocation.getCity(), pageLocation.getCountry(), pageLocation.getDescription(), pageLocation.getLatitude(), pageLocation.getLongitude(), pageLocation.getState(), pageLocation.getStreet(), pageLocation.getZip());
    }

    public Location(String city, String country, String description, double latitude, double longitude, String state, String street, String zip) {
        this.city = city;
        this.country = country;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.street = street;
        this.zip = zip;
    }


}