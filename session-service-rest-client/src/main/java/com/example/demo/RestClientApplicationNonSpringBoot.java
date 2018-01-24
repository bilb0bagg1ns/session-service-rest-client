package com.example.demo;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Standalone client app that tests Session-Service
 * 
 *
 */
@SpringBootApplication
public class RestClientApplicationNonSpringBoot {

	private static final Logger log = LoggerFactory.getLogger(RestClientApplicationNonSpringBoot.class);

	private static String resourceUrl = "http://localhost:8090/session/";

	/**
	 * GET from a remote RESTful service for the sessionId
	 * 
	 * Lookup by a session id and convert the JSON content in the response back to a
	 * Quote object.
	 * 
	 * @param sessionId
	 */
	public void getByIdTest(String sessionId) {

		try {
			RestTemplate restTemplate = new RestTemplate();

			// build URL in this format :
			// http://localhost:8090/session/5a68b25deac2901f7c4e404f
			String getResourceUrl = resourceUrl + sessionId;

			// GET from restful service
			ResponseEntity<String> response = restTemplate.getForEntity(getResourceUrl, String.class);

			// convert JSON string to object
			ObjectMapper mapper = new ObjectMapper();
			Quote quote = mapper.readValue(response.getBody(), Quote.class);
			log.info("================ GET Response =======================");
			log.info("JSON retrieved and converted to Quote object:" + quote);
			log.info("=====================================================");

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * POST a JSON to a remote RESTful service and returns a sessionID associated
	 * with the data that was stored
	 * 
	 * Step: Create a Quote object , convert it into a JSON string and POST it for
	 * storage
	 */
	public String postTest() {
		String sessionId = null;
		try {

			RestTemplate restTemplate = new RestTemplate();

			// create a test object to be JSONified and posted
			Quote quote = createTestObject();

			// Convert object to JSON string
			ObjectMapper mapper = new ObjectMapper();
			String jsonInString = mapper.writeValueAsString(quote);
			log.info("jsonInString:" + jsonInString);

			// POST to restful service
			// https://stackoverflow.com/questions/4075991/post-request-via-resttemplate-in-json
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<String>(jsonInString, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(resourceUrl, entity, String.class);

			log.info("============================================================");
			log.info("Session Id of the posted JSON : [" + response.getBody() + "]");
			log.info("============================================================");

			sessionId = response.getBody();

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return sessionId;
	}

	/**
	 * Http Post to a remote RESTful service and returns a sessionID associated with
	 * the data that was stored.
	 * 
	 * Create a Quote object , convert it into a JSON string and POST it for storage
	 */
	public String httpPostTest() {
		String sessionId = null;
		try {

			// create a test object to be JSONified and posted
			Quote quote = createTestObject();

			// Convert object to JSON string
			ObjectMapper mapper = new ObjectMapper();
			String jsonInString = mapper.writeValueAsString(quote);
			log.info("jsonInString:" + jsonInString);

			// Build HttpClient for POST
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(resourceUrl);
			StringEntity se = new StringEntity(jsonInString);
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			post.setEntity(se);

			// POST to restful service
			HttpResponse response = client.execute(post);
			org.apache.http.HttpEntity entity = response.getEntity();
			if (entity != null) {
				sessionId = EntityUtils.toString(entity);
				log.info("===================================================");
				log.info("Session Id of the posted JSON : [" + sessionId + "]");
				log.info("===================================================");

			}

		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return sessionId;
	}

	/**
	 * Create test object for JSONifying and POSTing
	 * 
	 * @return
	 */
	private Quote createTestObject() {
		/* create object to JSONIFY and store */
		Value value = new Value();
		value.setId(1234L);
		value.setQuote("Here is the quote");

		Quote quote = new Quote();
		quote.setType("Invoice");
		quote.setValue(value);
		return quote;
	}

	public static void main(String args[]) {

		RestClientApplicationNonSpringBoot restClientApplicationNonSpringBoot = new RestClientApplicationNonSpringBoot();

		/* POST to remote RESTful service */
		String sessionId = restClientApplicationNonSpringBoot.postTest();

		/* GET from remote RESTful service */
		restClientApplicationNonSpringBoot.getByIdTest(sessionId);

		/* Http Post to remote RESTful service */
		restClientApplicationNonSpringBoot.httpPostTest();

	}
}
