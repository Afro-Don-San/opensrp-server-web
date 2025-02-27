package org.opensrp.web.rest;

import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class RestResource<T> {
	
	protected ObjectMapper objectMapper;
	
	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	};
	
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	private T createNew(@RequestBody T entity) {
		RestUtils.verifyRequiredProperties(requiredProperties(), entity);
		return create(entity);
	}
	
	@RequestMapping(value = "/{uniqueId}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	private T updateExisting(@PathVariable("uniqueId") String uniqueId, @RequestBody T entity) {
		RestUtils.verifyRequiredProperties(requiredProperties(), entity);
		return update(entity);//TODO
	}
	
	@RequestMapping(value = "/{uniqueId}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	private T getById(@PathVariable("uniqueId") String uniqueId) throws JsonProcessingException {
		return getByUniqueId(uniqueId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/search", produces = { MediaType.APPLICATION_JSON_VALUE })
	private ResponseEntity<String> searchBy(HttpServletRequest request) throws ParseException, JsonProcessingException {
		return new ResponseEntity<>(objectMapper.writeValueAsString(search(request)),HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	private List<T> filterBy(@RequestParam(value = "q", required = true) String query) throws JsonProcessingException {
		return filter(query);
	}
	
	public abstract List<T> filter(String query);
	
	public abstract List<T> search(HttpServletRequest request) throws ParseException;
	
	public abstract T getByUniqueId(String uniqueId);
	
	public abstract List<String> requiredProperties();
	
	public abstract T create(T entity);
	
	public abstract T update(T entity);
	
}
