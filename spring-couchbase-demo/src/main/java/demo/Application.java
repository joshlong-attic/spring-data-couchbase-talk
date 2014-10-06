package demo;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.Page;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableCouchbaseRepositories
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

    @Bean
    Facebook facebook() {
        return new FacebookTemplate("594228000699830|LNmCmAnJrszIbU835yOfP0Yk6IU");
    }

    @Bean
    CouchbaseCacheManager cacheManager() throws Exception {
        HashMap<String, CouchbaseClient> instances = new HashMap<>();
        instances.put("persistent", couchbaseClient());
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

    private String json(Object o) {
        StringWriter stringWriter = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(stringWriter, o);
            return stringWriter.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    CommandLineRunner commandLineRunner(
            CouchbaseClient couchbaseClient,
            PlaceRepository placeRepository,
            CouchbaseTemplate couchbaseTemplate,
            Facebook facebook) {
        return args -> {

            couchbaseClient.flush().get();

            facebook.placesOperations().search("Starbucks", 37.752494, -122.414166, 5280).forEach((Page p) -> {
                couchbaseTemplate.insert(new Place(p));
             });
            Query q = new Query();

            // low level query call
//            couchbaseTemplate.findByView("place","all",q,Place.class);
//            placeRepository.findAll().forEach(System.out::println);
            // work with the couchbase template

            // use the Couchbase cache manager

            // use the Spring Session integration

            //
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

interface PlaceRepository extends CrudRepository<Place, String> {

    Collection<Place> findByFirstName(Query query);
}


@Document(expiry = 0)
class Place {
    @Field
    private Location location;

    @Field
    private String name, affilitation, category, description, about;

    @Id
    private String id;


    Place(Page p) {
        this.affilitation = p.getAffiliation();
        this.id = p.getId();
        this.name = p.getName();
        this.category = p.getCategory();
        this.description = p.getDescription();
        this.about = p.getAbout();

        org.springframework.social.facebook.api.Location pageLocation = p.getLocation();
        this.location = new Location(pageLocation);
    }

    public Location getLocation() {
        return location;
    }
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