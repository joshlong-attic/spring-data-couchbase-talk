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
        return Arrays.asList(this.ip);
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
        return new FacebookTemplate(this.accessToken);
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
            PlaceRepository placeRepository,
            PlaceService placeService) {
        return args -> {

            //couchbaseClient.flush().get();

            String starbucks = "Starbucks";
            String philzCoffee = "Philz Coffee";

            Arrays.asList(starbucks, philzCoffee)
                    .forEach(q -> placeService.search(q, 37.752494, -122.414166, 5280)
                            .stream()
                            .map(placeRepository::findOne)
                            .forEach(System.out::println));

            System.out.println(String.format("there are %s results.", placeRepository.count()));

            placeRepository.findAll().forEach(System.out::println);

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

    public Date getInsertionDate() {
        return insertionDate;
    }

    public String getId() {
        return id;
    }

    public Place(Page p) {
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

    public Place() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Place{");
        sb.append("name='").append(name).append('\'');
        sb.append(", insertionDate=").append(insertionDate);
        sb.append(", id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public Location getLocation() {
        return location;
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
}

@Service
class PlaceService {

    private final Facebook facebook;
    private final CouchbaseTemplate couchbaseTemplate;
    private final PlaceRepository placeRepository;

    @Scheduled(fixedDelay = 5000)
    void janitor() {

    }

    @Autowired
    PlaceService(Facebook facebook, CouchbaseTemplate couchbaseTemplate, PlaceRepository placeRepository) {
        this.facebook = facebook;
        this.couchbaseTemplate = couchbaseTemplate;
        this.placeRepository = placeRepository;
    }

    public Collection<Place> findPlacesInsertedBefore(Date date) {
        Query query = new Query();
        query.setStale(Stale.FALSE);
        query.setRangeEnd(Long.toString(date.getTime()));
        return placeRepository.findByInsertionDate(query);
    }

    public Place loadPlace(String id) {
        return this.placeRepository.findOne(id);
    }

    @Cacheable(value = "places", key = "'cache:'+#query")
    public List<String> search(String query, double lat, double lon, int distance) {
        return facebook.placesOperations()
                .search(query, lat, lon, distance)
                .stream()
                .map(p -> this.placeRepository.save(new Place(p)))
                .map(Place::getId)
                .collect(Collectors.toList());
    }
}

interface PlaceRepository extends CrudRepository<Place, String> {
    Collection<Place> findByInsertionDate(Query query);
}


class Location {
    private String city;
    private String country;
    private String description;
    private double latitude;
    private double longitude;
    private String state;
    private String street;
    private String zip;

    public Location() {
    }

    public Location(org.springframework.social.facebook.api.Location pageLocation) {
        this(pageLocation.getCity(), pageLocation.getCountry(),
                pageLocation.getDescription(), pageLocation.getLatitude(),
                pageLocation.getLongitude(), pageLocation.getState(),
                pageLocation.getStreet(), pageLocation.getZip());
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

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getState() {
        return state;
    }

    public String getStreet() {
        return street;
    }

    public String getZip() {
        return zip;
    }
}