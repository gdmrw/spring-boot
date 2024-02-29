/*
 * Copyright 2012-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package smoketest.actuator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Basic integration tests for service demo application.
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SampleActuatorApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ApplicationContext applicationContext;

	@Mock
	private TestRestTemplate mockRestTemplate;

	@Autowired
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private MockMvc mockMvc;

	@Test
	void testHomeMock() throws Exception {
		this.mockMvc.perform(get("/").with(httpBasic("user", "password")))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Hello Phil")));
	}

	@Test
	void testHealthMock() {
		ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("\"status\":\"UP\"", HttpStatus.OK);
		when(this.mockRestTemplate.getForEntity("/actuator/health", String.class)).thenReturn(mockResponseEntity);

		ResponseEntity<String> response = this.mockRestTemplate.getForEntity("/actuator/health", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("\"status\":\"UP\"", response.getBody());
	}

	@Test
	void testHomeIsSecure() {
		ResponseEntity<Map<String, Object>> entity = asMapEntity(this.restTemplate.getForEntity("/", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(entity.getHeaders()).doesNotContainKey("Set-Cookie");
	}

	@Test
	void testMetricsIsSecure() {
		ResponseEntity<Map<String, Object>> entity = asMapEntity(
				this.restTemplate.getForEntity("/actuator/metrics", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		entity = asMapEntity(this.restTemplate.getForEntity("/actuator/metrics/", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		entity = asMapEntity(this.restTemplate.getForEntity("/actuator/metrics/foo", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		entity = asMapEntity(this.restTemplate.getForEntity("/actuator/metrics.json", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void testHome() {
		ResponseEntity<Map<String, Object>> entity = asMapEntity(
				this.restTemplate.withBasicAuth("user", "password").getForEntity("/", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).containsEntry("message", "Hello Phil");
	}

	@Test
	@SuppressWarnings("unchecked")
	void testMetrics() {
		testHome(); // makes sure some requests have been made
		ResponseEntity<Map<String, Object>> entity = asMapEntity(
				this.restTemplate.withBasicAuth("user", "password").getForEntity("/actuator/metrics", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).containsKey("names");
		List<String> names = (List<String>) entity.getBody().get("names");
		assertThat(names).contains("jvm.buffer.count");
	}

	@Test
	void testEnv() {
		ResponseEntity<Map<String, Object>> entity = asMapEntity(
				this.restTemplate.withBasicAuth("user", "password").getForEntity("/actuator/env", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).containsKey("propertySources");
	}

	@Test
	void healthInsecureByDefault() {
		ResponseEntity<String> entity = this.restTemplate.getForEntity("/actuator/health", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).contains("\"status\":\"UP\"");
		assertThat(entity.getBody()).doesNotContain("\"hello\":\"1\"");
	}

	@Test
	void testErrorPage() {
		ResponseEntity<String> entity = this.restTemplate.withBasicAuth("user", "password")
			.getForEntity("/foo", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		String body = entity.getBody();
		assertThat(body).contains("\"error\":");
	}

	@Test
	void testHtmlErrorPage() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.TEXT_HTML));
		HttpEntity<?> request = new HttpEntity<Void>(headers);
		ResponseEntity<String> entity = this.restTemplate.withBasicAuth("user", "password")
			.exchange("/foo", HttpMethod.GET, request, String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		String body = entity.getBody();
		assertThat(body).as("Body was null").isNotNull();
		assertThat(body).contains("This application has no explicit mapping for /error");
	}

	@Test
	void testErrorPageDirectAccess() {
		ResponseEntity<Map<String, Object>> entity = asMapEntity(
				this.restTemplate.withBasicAuth("user", "password").getForEntity("/error", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(entity.getBody()).containsEntry("error", "None");
		assertThat(entity.getBody()).containsEntry("status", 999);
	}

	@Test
	void testBeans() {
		ResponseEntity<Map<String, Object>> entity = asMapEntity(
				this.restTemplate.withBasicAuth("user", "password").getForEntity("/actuator/beans", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).containsOnlyKeys("contexts");
	}

	@Test
	@SuppressWarnings("unchecked")
	void testConfigProps() {
		ResponseEntity<Map<String, Object>> entity = asMapEntity(
				this.restTemplate.withBasicAuth("user", "password").getForEntity("/actuator/configprops", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		Map<String, Object> body = entity.getBody();
		Map<String, Object> contexts = (Map<String, Object>) body.get("contexts");
		Map<String, Object> context = (Map<String, Object>) contexts.get(this.applicationContext.getId());
		Map<String, Object> beans = (Map<String, Object>) context.get("beans");
		assertThat(beans).containsKey("spring.datasource-" + DataSourceProperties.class.getName());
	}

	@Test
	void testLegacyDot() {
		ResponseEntity<Map<String, Object>> entity = asMapEntity(
				this.restTemplate.withBasicAuth("user", "password").getForEntity("/actuator/legacy", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).contains(entry("legacy", "legacy"));
	}

	@Test
	void testLegacyHyphen() {
		ResponseEntity<Map<String, Object>> entity = asMapEntity(
				this.restTemplate.withBasicAuth("user", "password").getForEntity("/actuator/anotherlegacy", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).contains(entry("legacy", "legacy"));
	}

	@Test
	void testMetricsRedirectsToLogin() {
		ResponseEntity<String> entity = this.restTemplate.getForEntity("/actuator/metrics", String.class);

		// check the http code is 302 Found，represent redirection
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
	}

	@Test
	void testEnvRedirectsToLogin() {
		ResponseEntity<String> entity = this.restTemplate.getForEntity("/actuator/env", String.class);

		// check the http code is 302 Found，represent redirection
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
	}

	@Test
	void testLoggersRedirectsToLogin() {
		ResponseEntity<String> entity = this.restTemplate.getForEntity("/actuator/loggers", String.class);

		// check the http code is 302 Found，represent redirection
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
	}

	@Test
	void testHealthDirectToTargetPage() {
		ResponseEntity<String> entity = this.restTemplate.getForEntity("/actuator/health", String.class);

		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void testHttpStateChangeAfterVerification() {
		ResponseEntity<String> entity = this.restTemplate.withBasicAuth("user", "password")
			.getForEntity("/actuator/env", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		String body = entity.getBody();
		assertThat(body).contains("\"java.class.path\"");
	}

	@Test
	void testHttpStateNotFoundAfterVerification() {
		ResponseEntity<String> entity = this.restTemplate.withBasicAuth("user", "password")
			.getForEntity("/actuator/yeeeeeeeeh", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testHttpStateChangeToInternalErrorAfterVerification() {
		ResponseEntity<String> entity = this.restTemplate.withBasicAuth("user", "password")
			.getForEntity("/foo", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	@SuppressWarnings("unchecked")
	void testInfo() {
		ResponseEntity<Map<String, Object>> entity = asMapEntity(
				this.restTemplate.withBasicAuth("user", "password").getForEntity("/actuator/info", Map.class));
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).containsKey("build");
		Map<String, Object> body = entity.getBody();
		Map<String, Object> example = (Map<String, Object>) body.get("example");
		assertThat(example).containsEntry("someKey", "someValue");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <K, V> ResponseEntity<Map<K, V>> asMapEntity(ResponseEntity<Map> entity) {
		return (ResponseEntity) entity;
	}

}
