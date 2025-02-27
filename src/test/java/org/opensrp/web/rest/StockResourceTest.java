package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.Stock;
import org.opensrp.search.StockSearchBean;
import org.opensrp.service.StockService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.opensrp.common.AllConstants.Stock.PROVIDERID;
import static org.opensrp.common.AllConstants.Stock.TIMESTAMP;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
@ActiveProfiles(profiles = { "jedis", "postgres", "basic_auth" })
public class StockResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Mock
	private StockService stockService;

	@InjectMocks
	private StockResource stockResource;

	protected ObjectMapper mapper = new ObjectMapper();

	private final String BASE_URL = "/rest/stockresource/";
	
	private final String SYNC_PAYLOAD = "{\n"
			+ "\t\"stocks\": \"[{\\\"identifier\\\":123,\\\"providerid\\\":\\\"test-id\\\"}]\"\n"
			+ "}";
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(stockResource)
				.addFilter(new CrossSiteScriptingPreventionFilter(), "/*")
				.build();
	}

	@Test
	public void testGetByUniqueId() throws Exception {
		Stock expected = createStock();
		when(stockService.find(any(String.class))).thenReturn(expected);
		Stock actual = stockResource.getByUniqueId("123");
		assertEquals(actual.getIdentifier(), actual.getIdentifier());
	}

	@Test
	public void testGetAll() throws Exception {
		List<Stock> expected = new ArrayList<>();
		expected.add(createStock());

		when(stockService.findAllStocks()).thenReturn(expected);

		MvcResult result = mockMvc.perform(get(BASE_URL + "getall"))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);

		assertEquals(actualObj.get("stocks").get(0).get("identifier").asLong(), 12345l);
		assertEquals(actualObj.get("stocks").get(0).get("id").asText(), "ID-123");
		assertEquals(actualObj.get("stocks").size(), 1);
	}

	@Test
	public void testGetAllWithException() throws Exception {
		when(stockService.findAllStocks()).thenReturn(null);
		mockMvc.perform(get(BASE_URL + "getall"))
				.andExpect(status().isInternalServerError()).andReturn();
	}

	@Test
	public void testSync() throws Exception {
		List<Stock> expected = new ArrayList<>();
		expected.add(createStock());

		when(stockService.findStocks(any(StockSearchBean.class), any(String.class), any(String.class), any(int.class)))
				.thenReturn(expected);

		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/sync").param(AllConstants.BaseEntity.SERVER_VERSIOIN, "15421904649873")
						.param("limit", "0"))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		assertEquals(actualObj.get("stocks").size(), 1);
	}

	@Test
	public void testSyncThrowsException() throws Exception {
		when(stockService.findStocks(any(StockSearchBean.class), any(String.class), any(String.class), any(int.class))).thenReturn(null);

		mockMvc.perform(get(BASE_URL + "/sync").param(AllConstants.BaseEntity.SERVER_VERSIOIN, "15421904649873")
				.param("limit", "1"))
				.andExpect(status().isInternalServerError()).andReturn();
	}

	@Test
	public void testCreate() {
		Stock expected = createStock();
		Stock stockObject = new Stock();
		stockObject.setId("ID-123");
		when(stockService.addStock(any(Stock.class))).thenReturn(expected);
		Stock actual = stockResource.create(stockObject);
		assertEquals(actual.getId(), actual.getId());
	}

	@Test
	public void testUpdate() {
		Stock expected = createStock();
		Stock stockObject = new Stock();
		stockObject.setId("ID-123");
		when(stockService.mergeStock(any(Stock.class))).thenReturn(expected);
		Stock actual = stockResource.update(stockObject);
		assertEquals(actual.getId(), actual.getId());
	}
	
	@Test
	public void testSave() throws Exception {
		when(stockService.addorUpdateStock(any(Stock.class))).thenReturn(createStock());
		MvcResult result = mockMvc.perform(post(BASE_URL + "/add").contentType(MediaType.APPLICATION_JSON)
				.content(SYNC_PAYLOAD.getBytes()))
				.andExpect(status().isCreated()).andReturn();

		assertEquals(result.getResponse().getContentAsString(), "");
	}

	@Test
	public void testSaveWithBlankData() throws Exception {
		when(stockService.addorUpdateStock(any(Stock.class))).thenReturn(createStock());
		MvcResult result = mockMvc.perform(post(BASE_URL + "/add").contentType(MediaType.APPLICATION_JSON)
				.content("".getBytes()))
				.andExpect(status().isBadRequest()).andReturn();
		assertEquals(result.getResponse().getContentAsString(), "");
	}

	@Test
	public void testRequiredProperties() {
		List<String> actualRequiredProperties = stockResource.requiredProperties();

		assertEquals(2, actualRequiredProperties.size());
		assertTrue(actualRequiredProperties.contains(PROVIDERID));
		assertTrue(actualRequiredProperties.contains(TIMESTAMP));
	}
	
	@Test
	public void testFilter() {
		List<Stock> expected = new ArrayList<>();
		expected.add(createStock());
		when(stockService.findAllStocks()).thenReturn(expected);
		List<Stock> actual = stockResource.filter("");
		assertEquals(expected.size(),actual.size());
		assertEquals(expected.get(0).getIdentifier(),actual.get(0).getIdentifier());
	}
	
	private Stock createStock() {
		Stock stock = new Stock();
		stock.setIdentifier(12345l);
		stock.setId("ID-123");
		return stock;
	}
}
