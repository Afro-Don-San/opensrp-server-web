package org.opensrp.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opensrp.api.domain.User;
import org.opensrp.api.util.LocationTree;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.security.DrishtiAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest {
	
	public static final String TEAM_MEMEBER_OBJECT = "{\"teamMemberId\":\"1\",\"identifier\":\"7869\",\"isTeamLead\":\"false\",\"person\":{\"uuid\":\"bc3245ed-ecac-4afa-842c-8f8a7a242639\",\"display\":\"Ebad Ahmed Ezam\",\"gender\":\"Male\",\"birthdateEstimated\":\"false\",\"dead\":\"false\",\"preferredName\":{\"uuid\":\"7dacabb7-ad79-4e4d-874c-c61b3fc91f02\",\"display\":\"Ebad Ahmed Ezam\",},\"voided\":\"false\"},\"uuid\":\"6bd9a982-77d4-42ee-88a5-8c511719214a\",\"location\":{\"location\":{\"uuid\":\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\",\"display\":\"Karachi\",\"name\":\"Karachi\",\"tags\":{\"locationtag\":{\"uuid\":\"2db4a766-eba2-4780-91ff-e601193a86f2\",\"display\":\"City\",\"links\":{\"link\":{\"rel\":\"self\",\"uri\":\" NEED-TO-CONFIGURE/ws/rest/v1/locationtag/2db4a766-eba2-4780-91ff-e601193a86f2 \"}}}},\"parentLocation\":{\"uuid\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\",\"display\":\"Sindh\",\"links\":{\"link\":{\"rel\":\"self\",\"uri\":\" NEED-TO-CONFIGURE/ws/rest/v1/location/461f2be7-c95d-433c-b1d7-c68f272409d7 \"}}},\"childLocations\":{\"location\":{\"uuid\":\"60c21502-fec1-40f5-b77d-6df3f92771ce\",\"display\":\"Baldia\",\"links\":{\"link\":{\"rel\":\"self\",\"uri\":\" NEED-TO-CONFIGURE/ws/rest/v1/location/60c21502-fec1-40f5-b77d-6df3f92771ce \"}}}},\"retired\":\"false\",\"links\":{\"link\":[{\"rel\":\"self\",\"uri\":\" NEED-TO-CONFIGURE/ws/rest/v1/location/a529e2fc-6f0d-4e60-a5df-789fe17cca48 \"},{\"rel\":\"full\",\"uri\":\" NEED-TO-CONFIGURE/ws/rest/v1/location/a529e2fc-6f0d-4e60-a5df-789fe17cca48?v=full \"}]},\"resourceVersion\":\"1.9\"}},\"team\":{\"display\":\"TBR3-team1\",\"teamIdentifier\":\"1234\",\"teamName\":\"TBR3-team1\",\"uuid\":\"97ba7a47-58bd-4d9f-96d6-aaaa5c7b6cf0\",\"dateCreated\":\"2015-05-08T01:46:54.000-0400\",\"location\":{\"uuid\":\"765cb701-9e61-4ead-afb9-a63c943f4f14\",\"display\":\"Korangi\",\"name\":\"Korangi\",\"tags\":{\"locationtag\":{\"uuid\":\"295bfa65-859e-4e52-9a89-63393139df1e\",\"display\":\"Town\",\"links\":{\"link\":{\"rel\":\"self\",\"uri\":\" NEED-TO-CONFIGURE/ws/rest/v1/locationtag/295bfa65-859e-4e52-9a89-63393139df1e \"}}}},\"retired\":\"false\",\"links\":{\"link\":[{\"rel\":\"self\",\"uri\":\" NEED-TO-CONFIGURE/ws/rest/v1/location/765cb701-9e61-4ead-afb9-a63c943f4f14 \"},{\"rel\":\"full\",\"uri\":\" NEED-TO-CONFIGURE/ws/rest/v1/location/765cb701-9e61-4ead-afb9-a63c943f4f14?v=full \"}]}}}}";
	
	public static final String LOCATION_TREE_OBJECT = "{\"locationsHierarchy\":{\"map\":{\"215caa30-1906-4210-8294-23eb7914c1dd\":{\"id\":\"215caa30-1906-4210-8294-23eb7914c1dd\",\"label\":\"3-KHA\",\"node\":{\"locationId\":\"215caa30-1906-4210-8294-23eb7914c1dd\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\",\"name\":\"Ward-3\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"4ccd5a33-c462-4b53-b8c1-a1ad1c3ba0cf\":{\"id\":\"4ccd5a33-c462-4b53-b8c1-a1ad1c3ba0cf\",\"label\":\"DURGAPUR\",\"node\":{\"locationId\":\"4ccd5a33-c462-4b53-b8c1-a1ad1c3ba0cf\",\"name\":\"DURGAPUR\",\"parentLocation\":{\"locationId\":\"215caa30-1906-4210-8294-23eb7914c1dd\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"215caa30-1906-4210-8294-23eb7914c1dd\"}},\"parent\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\"},\"429feb8b-0b8d-4496-8e54-fdc94affed07\":{\"id\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\",\"label\":\"1-KHA\",\"node\":{\"locationId\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\",\"name\":\"1-KHA\",\"parentLocation\":{\"locationId\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"9047a5e3-66cf-4f83-b0b6-3cdd3d611272\":{\"id\":\"9047a5e3-66cf-4f83-b0b6-3cdd3d611272\",\"label\":\"Chapadaha Mauza\",\"node\":{\"locationId\":\"9047a5e3-66cf-4f83-b0b6-3cdd3d611272\",\"name\":\"Chapadaha Mauza\",\"parentLocation\":{\"locationId\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\",\"name\":\"1-KHA\",\"parentLocation\":{\"locationId\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\",\"name\":\"Ward-1\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\"},\"a8b7d760-0e7e-4fdb-9450-b41d31d1ec34\":{\"id\":\"a8b7d760-0e7e-4fdb-9450-b41d31d1ec34\",\"label\":\"Kuptala-1-KHA\",\"node\":{\"locationId\":\"a8b7d760-0e7e-4fdb-9450-b41d31d1ec34\",\"name\":\"Kuptala-1-KHA\",\"parentLocation\":{\"locationId\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\",\"name\":\"1-KHA\",\"parentLocation\":{\"locationId\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\",\"name\":\"Ward-1\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\"}},\"parent\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\"},\"f2f803d5-857a-42a4-a05b-142c3327b4fc\":{\"id\":\"f2f803d5-857a-42a4-a05b-142c3327b4fc\",\"label\":\"SONORAY\",\"node\":{\"locationId\":\"f2f803d5-857a-42a4-a05b-142c3327b4fc\",\"name\":\"SONORAY\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"e0d50bc5-09b2-4102-809d-687fe71d5fd0\":{\"id\":\"e0d50bc5-09b2-4102-809d-687fe71d5fd0\",\"label\":\"SARBANANDA\",\"node\":{\"locationId\":\"e0d50bc5-09b2-4102-809d-687fe71d5fd0\",\"name\":\"SARBANANDA\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\":{\"id\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\",\"label\":\"2-KHA\",\"node\":{\"locationId\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\",\"name\":\"2-KHA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"3a041478-5d39-4d42-b785-67c2ae56febb\":{\"id\":\"3a041478-5d39-4d42-b785-67c2ae56febb\",\"label\":\"Kuptala-2KHA\",\"node\":{\"locationId\":\"3a041478-5d39-4d42-b785-67c2ae56febb\",\"name\":\"Kuptala-2KHA\",\"parentLocation\":{\"locationId\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\",\"name\":\"2-KHA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\"},\"27e6d636-0683-4539-90b8-2c795318dc08\":{\"id\":\"27e6d636-0683-4539-90b8-2c795318dc08\",\"label\":\"BERADANGA\",\"node\":{\"locationId\":\"27e6d636-0683-4539-90b8-2c795318dc08\",\"name\":\"BERADANGA\",\"parentLocation\":{\"locationId\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\",\"name\":\"2-KHA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\"}},\"parent\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\"},\"5d0661b5-4868-49eb-a697-e4dc4348dfab\":{\"id\":\"5d0661b5-4868-49eb-a697-e4dc4348dfab\",\"label\":\"SHANTIRAM\",\"node\":{\"locationId\":\"5d0661b5-4868-49eb-a697-e4dc4348dfab\",\"name\":\"SHANTIRAM\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"f48a6482-2ffd-4596-8d9b-46dadc3c73df\":{\"id\":\"f48a6482-2ffd-4596-8d9b-46dadc3c73df\",\"label\":\"SRIPUR\",\"node\":{\"locationId\":\"f48a6482-2ffd-4596-8d9b-46dadc3c73df\",\"name\":\"SRIPUR\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"42423d74-a061-463b-93a1-2f773f0aae21\":{\"id\":\"42423d74-a061-463b-93a1-2f773f0aae21\",\"label\":\"1-KA\",\"node\":{\"locationId\":\"42423d74-a061-463b-93a1-2f773f0aae21\",\"name\":\"1-KA\",\"parentLocation\":{\"locationId\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"88abc9f1-d698-41e3-8e2d-0c900b16dfe6\":{\"id\":\"88abc9f1-d698-41e3-8e2d-0c900b16dfe6\",\"label\":\"Kuptala-1-KA\",\"node\":{\"locationId\":\"88abc9f1-d698-41e3-8e2d-0c900b16dfe6\",\"name\":\"Kuptala-1-KA\",\"parentLocation\":{\"locationId\":\"42423d74-a061-463b-93a1-2f773f0aae21\",\"name\":\"1-KA\",\"parentLocation\":{\"locationId\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\",\"name\":\"Ward-1\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"42423d74-a061-463b-93a1-2f773f0aae21\"}},\"parent\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\"},\"dff51374-be72-46cb-a9a3-c7989e24430c\":{\"id\":\"dff51374-be72-46cb-a9a3-c7989e24430c\",\"label\":\"DHOPADANGA\",\"node\":{\"locationId\":\"dff51374-be72-46cb-a9a3-c7989e24430c\",\"name\":\"DHOPADANGA\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"e8e88d43-e181-42f1-9de5-143149922eea\":{\"id\":\"e8e88d43-e181-42f1-9de5-143149922eea\",\"label\":\"RAMJIBAN\",\"node\":{\"locationId\":\"e8e88d43-e181-42f1-9de5-143149922eea\",\"name\":\"RAMJIBAN\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"d3367458-f5e5-4039-b1e7-f087cc5be3fa\":{\"id\":\"d3367458-f5e5-4039-b1e7-f087cc5be3fa\",\"label\":\"KANCHIBARI\",\"node\":{\"locationId\":\"d3367458-f5e5-4039-b1e7-f087cc5be3fa\",\"name\":\"KANCHIBARI\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"fa32786b-4063-4f39-b72d-a5bc0e549193\":{\"id\":\"fa32786b-4063-4f39-b72d-a5bc0e549193\",\"label\":\"3-KA\",\"node\":{\"locationId\":\"fa32786b-4063-4f39-b72d-a5bc0e549193\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\",\"name\":\"Ward-3\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"f872c792-32ac-49e7-a386-f6b968968ef1\":{\"id\":\"f872c792-32ac-49e7-a386-f6b968968ef1\",\"label\":\"Kuptala-3-KA\",\"node\":{\"locationId\":\"f872c792-32ac-49e7-a386-f6b968968ef1\",\"name\":\"Kuptala-3-KA\",\"parentLocation\":{\"locationId\":\"fa32786b-4063-4f39-b72d-a5bc0e549193\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"fa32786b-4063-4f39-b72d-a5bc0e549193\"}},\"parent\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\"},\"765cb701-9e61-4ead-afb9-a63c943f4f14\":{\"id\":\"765cb701-9e61-4ead-afb9-a63c943f4f14\",\"label\":\"Korangi\",\"node\":{\"locationId\":\"765cb701-9e61-4ead-afb9-a63c943f4f14\",\"name\":\"Korangi\",\"tags\":[\"Town\"],\"voided\":false}},\"a57cef08-b47e-4b59-acd8-354279a63027\":{\"id\":\"a57cef08-b47e-4b59-acd8-354279a63027\",\"label\":\"3-KA\",\"node\":{\"locationId\":\"a57cef08-b47e-4b59-acd8-354279a63027\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"parentLocation\":{\"locationId\":\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\",\"name\":\"NALDANGA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"f30df310-0d30-4482-8dfe-667def649c20\":{\"id\":\"f30df310-0d30-4482-8dfe-667def649c20\",\"label\":\"PROTAP - MANDUAR PARA\",\"node\":{\"locationId\":\"f30df310-0d30-4482-8dfe-667def649c20\",\"name\":\"PROTAP - MANDUAR PARA\",\"parentLocation\":{\"locationId\":\"a57cef08-b47e-4b59-acd8-354279a63027\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"a57cef08-b47e-4b59-acd8-354279a63027\"},\"f6933584-9248-409d-b06a-0988c470ce45\":{\"id\":\"f6933584-9248-409d-b06a-0988c470ce45\",\"label\":\"PROTAP - FUL PARA\",\"node\":{\"locationId\":\"f6933584-9248-409d-b06a-0988c470ce45\",\"name\":\"PROTAP - FUL PARA\",\"parentLocation\":{\"locationId\":\"a57cef08-b47e-4b59-acd8-354279a63027\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"a57cef08-b47e-4b59-acd8-354279a63027\"},\"bac5a3b2-456f-4500-93a7-7a24be91909e\":{\"id\":\"bac5a3b2-456f-4500-93a7-7a24be91909e\",\"label\":\"PROTAP - KATA PROTAP\",\"node\":{\"locationId\":\"bac5a3b2-456f-4500-93a7-7a24be91909e\",\"name\":\"PROTAP - KATA PROTAP\",\"parentLocation\":{\"locationId\":\"a57cef08-b47e-4b59-acd8-354279a63027\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"a57cef08-b47e-4b59-acd8-354279a63027\"}},\"parent\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\"},\"f4e3cb47-fea1-418c-9a63-26374e424043\":{\"id\":\"f4e3cb47-fea1-418c-9a63-26374e424043\",\"label\":\"RANGPUR\",\"node\":{\"locationId\":\"f4e3cb47-fea1-418c-9a63-26374e424043\",\"name\":\"RANGPUR\",\"tags\":[\"Division\"],\"voided\":false},\"children\":{\"a556070e-cd96-49bc-b079-2a415d476a97\":{\"id\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"label\":\"GAIBANDHA\",\"node\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"parentLocation\":{\"locationId\":\"f4e3cb47-fea1-418c-9a63-26374e424043\",\"name\":\"RANGPUR\",\"voided\":false},\"tags\":[\"District\"],\"voided\":false},\"children\":{\"960ada36-be32-4867-a0aa-b7f4b835c61f\":{\"id\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"label\":\"SADULLAPUR\",\"node\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"parentLocation\":{\"locationId\":\"f4e3cb47-fea1-418c-9a63-26374e424043\",\"name\":\"RANGPUR\",\"voided\":false},\"voided\":false},\"tags\":[\"Upazilla\"],\"voided\":false},\"children\":{\"bd57db27-71b9-467e-9503-ce2dec74e61b\":{\"id\":\"bd57db27-71b9-467e-9503-ce2dec74e61b\",\"label\":\"JAMALPUR\",\"node\":{\"locationId\":\"bd57db27-71b9-467e-9503-ce2dec74e61b\",\"name\":\"JAMALPUR\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"},\"1b93c923-5ebb-4c0a-8bbb-067cc5fc5c9f\":{\"id\":\"1b93c923-5ebb-4c0a-8bbb-067cc5fc5c9f\",\"label\":\"FARIDPUR\",\"node\":{\"locationId\":\"1b93c923-5ebb-4c0a-8bbb-067cc5fc5c9f\",\"name\":\"FARIDPUR\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"},\"a39ce1d7-d8ee-49e9-8a81-02f7949f5ff0\":{\"id\":\"a39ce1d7-d8ee-49e9-8a81-02f7949f5ff0\",\"label\":\"KUMARPARA\",\"node\":{\"locationId\":\"a39ce1d7-d8ee-49e9-8a81-02f7949f5ff0\",\"name\":\"KUMARPARA\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"},\"07b798a0-2219-4447-8b72-2510c0526a15\":{\"id\":\"07b798a0-2219-4447-8b72-2510c0526a15\",\"label\":\"DAMODARPUR\",\"node\":{\"locationId\":\"07b798a0-2219-4447-8b72-2510c0526a15\",\"name\":\"DAMODARPUR\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"},\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\":{\"id\":\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\",\"label\":\"NALDANGA\",\"node\":{\"locationId\":\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\",\"name\":\"NALDANGA\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"},\"e7d39ba2-45a1-498c-bcc5-937f179d81fa\":{\"id\":\"e7d39ba2-45a1-498c-bcc5-937f179d81fa\",\"label\":\"RASULPUR\",\"node\":{\"locationId\":\"e7d39ba2-45a1-498c-bcc5-937f179d81fa\",\"name\":\"RASULPUR\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"}},\"parent\":\"a556070e-cd96-49bc-b079-2a415d476a97\"},\"57b34716-c291-4ca4-a7c8-28e65ab8819a\":{\"id\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\",\"label\":\"GAIBANDHA SADAR\",\"node\":{\"locationId\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\",\"name\":\"GAIBANDHA SADAR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"parentLocation\":{\"locationId\":\"f4e3cb47-fea1-418c-9a63-26374e424043\",\"name\":\"RANGPUR\",\"voided\":false},\"voided\":false},\"tags\":[\"Upazilla\"],\"voided\":false},\"children\":{\"7491ac95-05d2-49a8-b6a9-463f357171eb\":{\"id\":\"7491ac95-05d2-49a8-b6a9-463f357171eb\",\"label\":\"LAKSHMIPUR\",\"node\":{\"locationId\":\"7491ac95-05d2-49a8-b6a9-463f357171eb\",\"name\":\"LAKSHMIPUR\",\"parentLocation\":{\"locationId\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\",\"name\":\"GAIBANDHA SADAR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\"},\"725658c6-4d94-4791-bad6-614dec63d83b\":{\"id\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"label\":\"KUPTALA\",\"node\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"parentLocation\":{\"locationId\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\",\"name\":\"GAIBANDHA SADAR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\"},\"d658d99a-1941-406b-bbdc-b46a2545de92\":{\"id\":\"d658d99a-1941-406b-bbdc-b46a2545de92\",\"label\":\"MALIBARI\",\"node\":{\"locationId\":\"d658d99a-1941-406b-bbdc-b46a2545de92\",\"name\":\"MALIBARI\",\"parentLocation\":{\"locationId\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\",\"name\":\"GAIBANDHA SADAR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\"}},\"parent\":\"a556070e-cd96-49bc-b079-2a415d476a97\"}},\"parent\":\"f4e3cb47-fea1-418c-9a63-26374e424043\"}}},\"cd4ed528-87cd-42ee-a175-5e7089521ebd\":{\"id\":\"cd4ed528-87cd-42ee-a175-5e7089521ebd\",\"label\":\"Pakistan\",\"node\":{\"locationId\":\"cd4ed528-87cd-42ee-a175-5e7089521ebd\",\"name\":\"Pakistan\",\"tags\":[\"Country\"],\"voided\":false},\"children\":{\"461f2be7-c95d-433c-b1d7-c68f272409d7\":{\"id\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\",\"label\":\"Sindh\",\"node\":{\"locationId\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\",\"name\":\"Sindh\",\"parentLocation\":{\"locationId\":\"cd4ed528-87cd-42ee-a175-5e7089521ebd\",\"name\":\"Pakistan\",\"voided\":false},\"tags\":[\"Province\"],\"voided\":false},\"children\":{\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\":{\"id\":\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\",\"label\":\"Karachi\",\"node\":{\"locationId\":\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\",\"name\":\"Karachi\",\"parentLocation\":{\"locationId\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\",\"name\":\"Sindh\",\"parentLocation\":{\"locationId\":\"cd4ed528-87cd-42ee-a175-5e7089521ebd\",\"name\":\"Pakistan\",\"voided\":false},\"voided\":false},\"tags\":[\"City\"],\"voided\":false},\"children\":{\"60c21502-fec1-40f5-b77d-6df3f92771ce\":{\"id\":\"60c21502-fec1-40f5-b77d-6df3f92771ce\",\"label\":\"Baldia\",\"node\":{\"locationId\":\"60c21502-fec1-40f5-b77d-6df3f92771ce\",\"name\":\"Baldia\",\"parentLocation\":{\"locationId\":\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\",\"name\":\"Karachi\",\"parentLocation\":{\"locationId\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\",\"name\":\"Sindh\",\"voided\":false},\"voided\":false},\"tags\":[\"Town\"],\"attributes\":{\"at1\":\"atttt1\"},\"voided\":false},\"parent\":\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\"}},\"parent\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\"}},\"parent\":\"cd4ed528-87cd-42ee-a175-5e7089521ebd\"}}},\"96cd1c2a-f678-4687-bd87-8f4c5eae261a\":{\"id\":\"96cd1c2a-f678-4687-bd87-8f4c5eae261a\",\"label\":\"BAMANDANGA\",\"node\":{\"locationId\":\"96cd1c2a-f678-4687-bd87-8f4c5eae261a\",\"name\":\"BAMANDANGA\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"e1f223f5-a59e-4a54-b44e-472ff2438684\":{\"id\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\",\"label\":\"2-KA\",\"node\":{\"locationId\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\",\"name\":\"2-KA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"fbe4c8cf-5d52-4bc3-a4ec-9dcc1f5504cd\":{\"id\":\"fbe4c8cf-5d52-4bc3-a4ec-9dcc1f5504cd\",\"label\":\"RAMPRASHAD\",\"node\":{\"locationId\":\"fbe4c8cf-5d52-4bc3-a4ec-9dcc1f5504cd\",\"name\":\"RAMPRASHAD\",\"parentLocation\":{\"locationId\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\",\"name\":\"2-KA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\"},\"36fc5398-8e7a-430b-ab3b-557788b4d89f\":{\"id\":\"36fc5398-8e7a-430b-ab3b-557788b4d89f\",\"label\":\"Kuptala-2-KA\",\"node\":{\"locationId\":\"36fc5398-8e7a-430b-ab3b-557788b4d89f\",\"name\":\"Kuptala-2-KA\",\"parentLocation\":{\"locationId\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\",\"name\":\"2-KA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\"}},\"parent\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\"},\"774bca32-01ab-4c7a-91f0-b5c51c41945a\":{\"id\":\"774bca32-01ab-4c7a-91f0-b5c51c41945a\",\"label\":\"CHHAPARHATI\",\"node\":{\"locationId\":\"774bca32-01ab-4c7a-91f0-b5c51c41945a\",\"name\":\"CHHAPARHATI\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"f332d8ac-e57f-49ba-8fb0-c428651697a2\":{\"id\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\",\"label\":\"3-KHA\",\"node\":{\"locationId\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"parentLocation\":{\"locationId\":\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\",\"name\":\"NALDANGA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"2fc43738-ace5-4961-8e8f-ab7d00e5bc63\":{\"id\":\"2fc43738-ace5-4961-8e8f-ab7d00e5bc63\",\"label\":\"DASLIA - ALL PARAS\",\"node\":{\"locationId\":\"2fc43738-ace5-4961-8e8f-ab7d00e5bc63\",\"name\":\"DASLIA - ALL PARAS\",\"parentLocation\":{\"locationId\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\"},\"50d3dddd-9fba-4895-9b96-fe66d42e6fed\":{\"id\":\"50d3dddd-9fba-4895-9b96-fe66d42e6fed\",\"label\":\"PROTAP - OPADANI PARA\",\"node\":{\"locationId\":\"50d3dddd-9fba-4895-9b96-fe66d42e6fed\",\"name\":\"PROTAP - OPADANI PARA\",\"parentLocation\":{\"locationId\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\"},\"80efdc06-59b7-4594-bf24-561a7eb12676\":{\"id\":\"80efdc06-59b7-4594-bf24-561a7eb12676\",\"label\":\"PROTAP - SARDAR PARA\",\"node\":{\"locationId\":\"80efdc06-59b7-4594-bf24-561a7eb12676\",\"name\":\"PROTAP - SARDAR PARA\",\"parentLocation\":{\"locationId\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\"}},\"parent\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\"}},\"parentChildren\":{\"215caa30-1906-4210-8294-23eb7914c1dd\":[\"4ccd5a33-c462-4b53-b8c1-a1ad1c3ba0cf\"],\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\":[\"e1f223f5-a59e-4a54-b44e-472ff2438684\",\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\"],\"429feb8b-0b8d-4496-8e54-fdc94affed07\":[\"9047a5e3-66cf-4f83-b0b6-3cdd3d611272\",\"a8b7d760-0e7e-4fdb-9450-b41d31d1ec34\"],\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\":[\"3a041478-5d39-4d42-b785-67c2ae56febb\",\"27e6d636-0683-4539-90b8-2c795318dc08\"],\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\":[\"a57cef08-b47e-4b59-acd8-354279a63027\",\"f332d8ac-e57f-49ba-8fb0-c428651697a2\"],\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\":[\"60c21502-fec1-40f5-b77d-6df3f92771ce\"],\"42423d74-a061-463b-93a1-2f773f0aae21\":[\"88abc9f1-d698-41e3-8e2d-0c900b16dfe6\"],\"fa32786b-4063-4f39-b72d-a5bc0e549193\":[\"f872c792-32ac-49e7-a386-f6b968968ef1\"],\"a57cef08-b47e-4b59-acd8-354279a63027\":[\"f30df310-0d30-4482-8dfe-667def649c20\",\"f6933584-9248-409d-b06a-0988c470ce45\",\"bac5a3b2-456f-4500-93a7-7a24be91909e\"],\"cd4ed528-87cd-42ee-a175-5e7089521ebd\":[\"461f2be7-c95d-433c-b1d7-c68f272409d7\"],\"f4e3cb47-fea1-418c-9a63-26374e424043\":[\"a556070e-cd96-49bc-b079-2a415d476a97\"],\"e1f223f5-a59e-4a54-b44e-472ff2438684\":[\"fbe4c8cf-5d52-4bc3-a4ec-9dcc1f5504cd\",\"36fc5398-8e7a-430b-ab3b-557788b4d89f\"],\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\":[\"429feb8b-0b8d-4496-8e54-fdc94affed07\",\"42423d74-a061-463b-93a1-2f773f0aae21\"],\"11eaac2c-12d6-4958-b548-2d6768776b10\":[\"dff51374-be72-46cb-a9a3-c7989e24430c\",\"e8e88d43-e181-42f1-9de5-143149922eea\",\"d3367458-f5e5-4039-b1e7-f087cc5be3fa\",\"96cd1c2a-f678-4687-bd87-8f4c5eae261a\",\"f2f803d5-857a-42a4-a05b-142c3327b4fc\",\"e0d50bc5-09b2-4102-809d-687fe71d5fd0\",\"774bca32-01ab-4c7a-91f0-b5c51c41945a\",\"5d0661b5-4868-49eb-a697-e4dc4348dfab\",\"f48a6482-2ffd-4596-8d9b-46dadc3c73df\"],\"461f2be7-c95d-433c-b1d7-c68f272409d7\":[\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\"],\"a556070e-cd96-49bc-b079-2a415d476a97\":[\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"57b34716-c291-4ca4-a7c8-28e65ab8819a\"],\"960ada36-be32-4867-a0aa-b7f4b835c61f\":[\"bd57db27-71b9-467e-9503-ce2dec74e61b\",\"1b93c923-5ebb-4c0a-8bbb-067cc5fc5c9f\",\"a39ce1d7-d8ee-49e9-8a81-02f7949f5ff0\",\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\",\"07b798a0-2219-4447-8b72-2510c0526a15\",\"e7d39ba2-45a1-498c-bcc5-937f179d81fa\"],\"57b34716-c291-4ca4-a7c8-28e65ab8819a\":[\"7491ac95-05d2-49a8-b6a9-463f357171eb\",\"725658c6-4d94-4791-bad6-614dec63d83b\",\"d658d99a-1941-406b-bbdc-b46a2545de92\"],\"f332d8ac-e57f-49ba-8fb0-c428651697a2\":[\"2fc43738-ace5-4961-8e8f-ab7d00e5bc63\",\"50d3dddd-9fba-4895-9b96-fe66d42e6fed\",\"80efdc06-59b7-4594-bf24-561a7eb12676\"],\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\":[\"215caa30-1906-4210-8294-23eb7914c1dd\",\"fa32786b-4063-4f39-b72d-a5bc0e549193\"]}}}";
	
	@Mock
	private OpenmrsLocationService locationservice;
	
	@Mock
	private OpenmrsUserService userservice;
	
	@Mock
	private UserController controller;

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Mock
	private OrganizationService organizationService;

	@Mock
	private PractitionerService practitionerService;

	@Mock
	private PhysicalLocationService physicalLocationService;
	
	private DrishtiAuthenticationProvider auth;
	
	private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
	
	@Mock
	private Authentication authentication;
	
	@Mock
	private HttpServletRequest servletRequest;

	@InjectMocks
	private UserController userController;

	protected ObjectMapper mapper = new ObjectMapper();
	
	public UserControllerTest() throws IOException {
		super();
	}
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		auth = new DrishtiAuthenticationProvider(userservice);
		usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken("demook", "demook");
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(userController)
				.addFilter(new CrossSiteScriptingPreventionFilter(), "/*")
				.build();
		userController.setOpensrpAuthenticationProvider(auth);
		userController.setOrganizationService(organizationService);
		userController.setPractitionerService(practitionerService);
		userController.setLocationService(physicalLocationService);
		ReflectionTestUtils.setField(userController, "opensrpAllowedSources", "");
		ReflectionTestUtils.setField(userController, "OPENMRS_VERSION", "2.1.3");

	}
	
	@Test
	public void test() throws JSONException {
		User u = new User("test user 1");
		u.withAttribute("Location", "cd4ed528-87cd-42ee-a175-5e7089521ebd");
		when(controller.getAuthenticationAdvisor(servletRequest)).thenReturn(usernamePasswordAuthenticationToken);
		when(controller.currentUser(servletRequest, authentication)).thenReturn(u);
		JSONObject to = new JSONObject(TEAM_MEMEBER_OBJECT);
		
		doReturn(to).when(userservice).getTeamMember(any(String.class));
		LocationTree ltree = new Gson().fromJson(LOCATION_TREE_OBJECT, LocationTree.class);
		doReturn(ltree).when(locationservice).getLocationTreeOf(any(String[].class));
	}
	
	@Test
	public void shoudPassOnCorrectPassword() throws JSONException {
		User u = new User("test user 1");
		u.withAttribute("Location", "cd4ed528-87cd-42ee-a175-5e7089521ebd");
		
		when(userservice.authenticate("demook", "demook")).thenReturn(true);
		when(controller.getAuthenticationAdvisor(servletRequest)).thenReturn(usernamePasswordAuthenticationToken);
		
		when(auth.getDrishtiUser(usernamePasswordAuthenticationToken, "demook")).thenReturn(u);
		
		controller.authenticate(servletRequest, authentication);
	}

	@Test
	public void testAuthenticateUser() throws Exception {
		MvcResult result = mockMvc.perform(get("/authenticate-user"))
				.andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testAuthenticate() throws Exception {
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		Authentication authentication = getMockedAuthentication();
		Map<String, Object> userAttributes = new HashMap<>();
		userAttributes.put("Location", "Test");
		userAttributes.put("_PERSON_UUID", "Test-UUID");
		User user = new User("Base-entity-id");
		user.setUsername("admin");
		user.setPassword("admin");
		user.setAttributes(userAttributes);
		String[] locations = new String[5];
		locations[0] = "Test";
		LocationTree locationTree = mock(LocationTree.class);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("user", user);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("test", data);

		when(userservice.authenticate(anyString(), anyString())).thenReturn(Boolean.TRUE);
		when(userservice.getUser(anyString())).thenReturn(user);
		when(locationservice.getLocationTreeOf(locations)).thenReturn(locationTree);
		when(userservice.getTeamMember(anyString())).thenReturn(jsonObject);

		ResponseEntity<String> result = userController.authenticate(httpServletRequest, authentication);
		String responseString = result.getBody();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		assertEquals(result.getStatusCode(), HttpStatus.OK);
		assertEquals(actualObj.get("user").get("username").asText(), "admin");
	}

	@Test
	public void testConfiguration() throws Exception {
		MvcResult result = mockMvc.perform(get("/security/configuration"))
				.andExpect(status().isOk()).andReturn();
	}

	private Authentication getMockedAuthentication() {
		Authentication authentication = new Authentication() {

			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}

			@Override
			public Object getCredentials() {
				return "";
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return "Test User";
			}

			@Override
			public boolean isAuthenticated() {
				return false;
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

			}

			@Override
			public String getName() {
				return "admin";
			}
		};

		return authentication;
	}
}
