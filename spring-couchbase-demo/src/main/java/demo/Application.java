package demo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.couchbase.core.mapping.event.ValidatingCouchbaseEventListener;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.Page;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.couchbase.client.protocol.views.Query;

/**
 * @author Laurent Doguin
 * @author Josh Long
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    @Bean
    LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    ValidatingCouchbaseEventListener validationEventListener() {
        return new ValidatingCouchbaseEventListener(validator());
    }

	@EnableCouchbaseRepositories
	@Configuration
	static class CouchbaseConfiguration extends AbstractCouchbaseConfiguration {


		@Value("${couchbase.cluster.bucket}")
		private String bucketName;

		@Value("${couchbase.cluster.password}")
		private String password;

		@Value("${couchbase.cluster.ip}")
		private String ip;

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
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	Facebook facebook(@Value("${facebook.accessToken}") String accessToken) {
		return new FacebookTemplate(accessToken);
	}

	@Bean
	CommandLineRunner commandLineRunner(PlaceRepository placeRepository,
			PlaceService placeService) {
		return args -> {

			Arrays.asList("Starbucks", "Philz Coffee").forEach(
					query -> placeService.search(query, 37.752494, -122.414166, 5280));

			System.out.println("------------------------");
			System.out.println("query:findAll");
			placeRepository.findAll().forEach(System.out::println);

			System.out.println("------------------------");
			System.out.println("query:count");
			System.out.println(placeRepository.count());

			System.out.println("------------------------");
			System.out.println("query:byName");
			Query query = new Query();
			query.setKey("Philz Coffee");
			placeRepository.findByName(query).forEach(System.out::println);

		};
	}

}

@Service
class PlaceService {

	private final Facebook facebook;
	private final CouchbaseTemplate couchbaseTemplate;
	private final PlaceRepository placeRepository;

	@Autowired
	PlaceService(Facebook facebook, CouchbaseTemplate couchbaseTemplate,
			PlaceRepository placeRepository) {
		this.facebook = facebook;
		this.couchbaseTemplate = couchbaseTemplate;
		this.placeRepository = placeRepository;
	}

	public List<String> search(String query, double lat, double lon, int distance) {
		return facebook.placesOperations().search(query, lat, lon, distance).stream()
				.map(p -> this.placeRepository.save(new Place(p))).map(Place::getId)
				.collect(Collectors.toList());
	}
}

interface PlaceRepository extends CrudRepository<Place, String> {

	// @View(designDocument = "place", viewName = "byName")
	Collection<Place> findByName(Query placeNameQuery);
}

@Document(expiry = 0)
class Place {

	@Id
	private String id;

	@Field
	private Location location;

	@Field
	@NotNull
	private String name;

	@Field
	private String affilitation, category, description, about;

	@Field
	private Date insertionDate;

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

	Place() {
	}

	public Place(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Place{" + "id='" + id + '\'' + ", location=" + location + ", name='"
				+ name + '\'' + ", affilitation='" + affilitation + '\'' + ", category='"
				+ category + '\'' + ", description='" + description + '\'' + ", about='"
				+ about + '\'' + ", insertionDate=" + insertionDate + '}';
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
		this(pageLocation.getCity(), pageLocation.getCountry(), pageLocation
				.getDescription(), pageLocation.getLatitude(), pageLocation
				.getLongitude(), pageLocation.getState(), pageLocation.getStreet(),
				pageLocation.getZip());
	}

	public Location(String city, String country, String description, double latitude,
			double longitude, String state, String street, String zip) {
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