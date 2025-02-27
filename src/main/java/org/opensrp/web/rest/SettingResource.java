package org.opensrp.web.rest;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.LocationTree;
import org.opensrp.api.util.TreeNode;
import org.opensrp.common.AllConstants;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.postgres.handler.SettingTypeHandler;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.SettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static java.text.MessageFormat.format;

@Controller
@RequestMapping (value = "/rest/settings")
public class SettingResource {
	
	public static final String SETTING_CONFIGURATIONS = "settingConfigurations";
	private SettingService settingService;
	private OpenmrsLocationService openmrsLocationService;
	
	private static final Logger logger = LoggerFactory.getLogger(SettingResource.class.toString());
	
	@Autowired
	public void setSettingService(SettingService settingService, OpenmrsLocationService openmrsLocationService) {
		this.settingService = settingService;
		this.openmrsLocationService = openmrsLocationService;
	}

	private Map<String, TreeNode<String, Location>> getChildParentLocationTree(String locationId) {
		String locationTreeString = new Gson().toJson(openmrsLocationService.getLocationTreeOf(locationId));
		LocationTree locationTree = new Gson().fromJson(locationTreeString, LocationTree.class);
		Map<String, TreeNode<String, Location>> treeNodeHashMap = new HashMap<>();
		if (locationTree != null) {
			treeNodeHashMap = locationTree.getLocationsHierarchy();
		}

		return treeNodeHashMap;
	}
	
	@RequestMapping (method = RequestMethod.GET, value = "/sync")
	public @ResponseBody
	ResponseEntity<String> findSettingsByVersion(HttpServletRequest request) {
		JSONObject response = new JSONObject();
		ResponseEntity responseEntity;
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
		try {
			String serverVersion = RestUtils.getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
			String providerId = RestUtils.getStringFilter(AllConstants.Event.PROVIDER_ID, request);
			String locationId = RestUtils.getStringFilter(AllConstants.Event.LOCATION_ID, request);
			String team = RestUtils.getStringFilter(AllConstants.Event.TEAM, request);
			String teamId = RestUtils.getStringFilter(AllConstants.Event.TEAM_ID, request);
			boolean resolveSettings = RestUtils.getBooleanFilter(AllConstants.Event.RESOLVE_SETTINGS, request);
			List<SettingConfiguration> SettingConfigurations = null;
			Map<String, TreeNode<String, Location>> treeNodeHashMap = null;
			
			if (StringUtils.isBlank(serverVersion)) {
				return new ResponseEntity<>(response.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
			}
			
			SettingSearchBean settingQueryBean = new SettingSearchBean();
			settingQueryBean.setTeam(team);
			settingQueryBean.setTeamId(teamId);
			settingQueryBean.setProviderId(providerId);
			settingQueryBean.setLocationId(locationId);
			settingQueryBean.setServerVersion(Long.parseLong(serverVersion) + 1);
			settingQueryBean.setV1Settings(true);
			if (StringUtils.isNotBlank(locationId)) {
				settingQueryBean.setResolveSettings(resolveSettings);
				treeNodeHashMap = getChildParentLocationTree(locationId);
			}
			
			SettingConfigurations = settingService.findSettings(settingQueryBean, treeNodeHashMap);
			
			SettingTypeHandler settingTypeHandler = new SettingTypeHandler();
			String settingsArrayString = settingTypeHandler.mapper.writeValueAsString(SettingConfigurations);
			
			responseEntity = new ResponseEntity<>(new JSONArray(settingsArrayString).toString(), responseHeaders, HttpStatus.OK); // todo: why is this conversion to json array necessary?
		} catch (Exception e) {
			logger.error(format("Sync data processing failed with exception {0}.- ", e));
			responseEntity = new ResponseEntity<>(responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return responseEntity;
	}
	
	@RequestMapping (headers = {"Accept=application/json"}, method = RequestMethod.POST, value = "/sync", consumes = {MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE})
	public ResponseEntity<String> saveSetting(@RequestBody String data) {
		JSONObject response = new JSONObject();
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
		
		JSONObject syncData = new JSONObject(data);
		
		if (!syncData.has(SETTING_CONFIGURATIONS)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} else {
			
			JSONArray clientSettings = syncData.getJSONArray(AllConstants.Event.SETTING_CONFIGURATIONS);
			JSONArray dbSettingsArray = new JSONArray();
			
			for (int i = 0; i < clientSettings.length(); i++) {
				dbSettingsArray.put(settingService.saveSetting(clientSettings.get(i).toString()));
			}
			
			response.put("validated_records", dbSettingsArray);
			
		}
		
		return new ResponseEntity<>(response.toString(), responseHeaders, HttpStatus.OK);
	}
}
