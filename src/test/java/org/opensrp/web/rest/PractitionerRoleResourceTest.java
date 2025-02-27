package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opensrp.domain.PractitionerRole;
import org.opensrp.domain.PractitionerRoleCode;
import org.opensrp.service.PractitionerRoleService;
import org.springframework.test.web.server.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class PractitionerRoleResourceTest extends BaseResourceTest<PractitionerRole>{

    private final static String BASE_URL = "/rest/practitionerRole/";

    private final static String BATCH_SAVE_ENDPOINT = "add";

    private final static String DELETE_ENDPOINT = "delete/";

    private final static String DELETE_BY_PRACTITIONER_ENDPOINT = "deleteByPractitioner";

    private PractitionerRoleService practitionerRoleService;

    private ArgumentCaptor<PractitionerRole> argumentCaptor = ArgumentCaptor.forClass(PractitionerRole.class);

    private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

    private final String practitionerRoleJson = "{\"identifier\":\"pr1-identifier\",\"active\":true,\"organization\":\"org1\",\"practitioner\":\"p1-identifier\",\"code\":{\"text\":\"pr1Code\"}}";

    private final String practitionerRoleListJson = "[{\"identifier\":\"pr1-identifier\",\"active\":true,\"organization\":\"org1\",\"practitioner\":\"p1-identifier\",\"code\":{\"text\":\"pr1Code\"}}]";

    @Before
    public void setUp() {
        practitionerRoleService = mock(PractitionerRoleService.class);
        PractitionerRoleResource practitionerRoleResource = webApplicationContext.getBean(PractitionerRoleResource.class);
        practitionerRoleResource.setPractitionerRoleService(practitionerRoleService);
    }

    @Test
    public void testGetPractitionerRolesShouldReturnAllPractitionerRoles() throws Exception {
        List<PractitionerRole> expectedPractitoinerRoles =  new ArrayList<>();

        PractitionerRole expectedPractitionerRole = initTestPractitionerRole1();
        expectedPractitoinerRoles.add(expectedPractitionerRole);

        expectedPractitionerRole = initTestPractitionerRole2();
        expectedPractitoinerRoles.add(expectedPractitionerRole);

        doReturn(expectedPractitoinerRoles).when(practitionerRoleService).getAllPractitionerRoles();

        String actualPractitionerRolessString = getResponseAsString(BASE_URL, null, MockMvcResultMatchers.status().isOk());
        List<PractitionerRole> actualPractitioners = new Gson().fromJson(actualPractitionerRolessString, new TypeToken<List<PractitionerRole>>(){}.getType());

        assertListsAreSameIgnoringOrder(actualPractitioners, expectedPractitoinerRoles);
    }

    @Test
    public void testGetPractitionerRoleByUniqueIdShouldReturnCorrectPractititonerRole() throws Exception {
        List<PractitionerRole> expectedPractitoinerRoles =  new ArrayList<>();

        PractitionerRole expectedPractitionerRole = initTestPractitionerRole1();
        expectedPractitoinerRoles.add(expectedPractitionerRole);

        List<String> practitionerRoleIdList = new ArrayList<>();
        practitionerRoleIdList.add(expectedPractitionerRole.getIdentifier());

        doReturn(expectedPractitionerRole).when(practitionerRoleService).getPractitionerRole(anyString());

        String actualPractitionerRoleString = getResponseAsString(BASE_URL + "pr1-identifier", null,
                MockMvcResultMatchers.status().isOk());
        PractitionerRole actualPractitionerRole = new Gson().fromJson(actualPractitionerRoleString, new TypeToken<PractitionerRole>(){}.getType());

        assertNotNull(actualPractitionerRole);
        assertEquals(actualPractitionerRole.getIdentifier(), expectedPractitionerRole.getIdentifier());
        assertEquals(actualPractitionerRole.getOrganizationIdentifier(), expectedPractitionerRole.getOrganizationIdentifier());
        assertEquals(actualPractitionerRole.getPractitionerIdentifier(), expectedPractitionerRole.getPractitionerIdentifier());
        assertEquals(actualPractitionerRole.getCode().getText(), expectedPractitionerRole.getCode().getText());
        assertEquals(actualPractitionerRole.getActive(), expectedPractitionerRole.getActive());
    }

    @Test
    public void testCreateShouldCreateNewPractitionerRoleResource() throws Exception {
        doReturn(new PractitionerRole()).when(practitionerRoleService).addOrUpdatePractitionerRole((PractitionerRole) any());

        PractitionerRole expectedPractitioner = initTestPractitionerRole1();

        postRequestWithJsonContent(BASE_URL, practitionerRoleJson, MockMvcResultMatchers.status().isCreated());

        verify(practitionerRoleService).addOrUpdatePractitionerRole(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPractitioner.getIdentifier());

    }

    @Test
    public void testUpdateShouldUpdateExistingPractitionerRoleResource() throws Exception {
        PractitionerRole expectedPractitionerRole = initTestPractitionerRole1();

        String practitionerRoleJson = new Gson().toJson(expectedPractitionerRole, new TypeToken<PractitionerRole>(){}.getType());
        putRequestWithJsonContent(BASE_URL, practitionerRoleJson, MockMvcResultMatchers.status().isCreated());

        verify(practitionerRoleService).addOrUpdatePractitionerRole(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPractitionerRole.getIdentifier());
    }

    @Test
    public void testBatchSaveShouldCreateNewPractitionerRole() throws Exception {
        doReturn(new PractitionerRole()).when(practitionerRoleService).addOrUpdatePractitionerRole((PractitionerRole) any());

        PractitionerRole expectedPractitioner = initTestPractitionerRole1();

        postRequestWithJsonContent(BASE_URL + BATCH_SAVE_ENDPOINT , practitionerRoleListJson, MockMvcResultMatchers.status().isCreated());

        verify(practitionerRoleService).addOrUpdatePractitionerRole(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPractitioner.getIdentifier());

    }

    @Test
    public void testBatchSaveShouldUpdateExistingPractitionerRole() throws Exception {
        List<PractitionerRole> expectedPractitionerRoles = new ArrayList<>();
        PractitionerRole expectedPractitionerRole = initTestPractitionerRole1();
        expectedPractitionerRoles.add(expectedPractitionerRole);

        String practitionerRolesJson = new Gson().toJson(expectedPractitionerRoles, new TypeToken<List<PractitionerRole>>() {
        }.getType());

        postRequestWithJsonContent(BASE_URL + BATCH_SAVE_ENDPOINT , practitionerRolesJson, MockMvcResultMatchers.status().isCreated());

        verify(practitionerRoleService).addOrUpdatePractitionerRole(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPractitionerRole.getIdentifier());
    }

    @Test
    public void testDeleteShouldDeleteExistingPractitionerRoleResource() throws Exception {

        deleteRequestWithParams(BASE_URL + DELETE_ENDPOINT + "practitioner-role-id", null, MockMvcResultMatchers.status().isNoContent());

        verify(practitionerRoleService).deletePractitionerRole(stringArgumentCaptor.capture());
        assertEquals(stringArgumentCaptor.getValue(), "practitioner-role-id");
    }

    @Test
    public void testDeleteByOrgAndPractitionerShouldDeleteExistingPractitionerRoleResource() throws Exception {

        deleteRequestWithParams(BASE_URL + DELETE_BY_PRACTITIONER_ENDPOINT , "organization=org1&practitioner=pract1", MockMvcResultMatchers.status().isNoContent());

        verify(practitionerRoleService).deletePractitionerRole(stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        assertEquals(stringArgumentCaptor.getAllValues().get(0), "org1");
        assertEquals(stringArgumentCaptor.getAllValues().get(1), "pract1");
    }

    @Test
    public void testCreateWithInternalError() throws Exception {
        doThrow(new IllegalArgumentException()).when(practitionerRoleService).addOrUpdatePractitionerRole((PractitionerRole) any());
        postRequestWithJsonContent(BASE_URL, practitionerRoleJson, MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerRoleService).addOrUpdatePractitionerRole(argumentCaptor.capture());
        verifyNoMoreInteractions(practitionerRoleService);
    }

    @Test
    public void testCreateWithJsonSyntaxException() throws Exception {
        doThrow(new JsonSyntaxException("Unable to parse JSON")).when(practitionerRoleService).addOrUpdatePractitionerRole((PractitionerRole) any());
        postRequestWithJsonContent(BASE_URL, practitionerRoleJson, MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerRoleService).addOrUpdatePractitionerRole(argumentCaptor.capture());
        verifyNoMoreInteractions(practitionerRoleService);
    }

    @Test
    public void testUpdateWithInternalError() throws Exception {
        doThrow(new IllegalArgumentException()).when(practitionerRoleService).addOrUpdatePractitionerRole((PractitionerRole) any());
        putRequestWithJsonContent(BASE_URL, practitionerRoleJson, MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerRoleService).addOrUpdatePractitionerRole(argumentCaptor.capture());
        verifyNoMoreInteractions(practitionerRoleService);
    }

    @Test
    public void testUpdateWithJsonSyntaxException() throws Exception {
        doThrow(new JsonSyntaxException("Unable to parse JSON")).when(practitionerRoleService).addOrUpdatePractitionerRole((PractitionerRole) any());
        putRequestWithJsonContent(BASE_URL, practitionerRoleJson, MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerRoleService).addOrUpdatePractitionerRole(argumentCaptor.capture());
        verifyNoMoreInteractions(practitionerRoleService);
    }

    @Test
    public void testBatchSaveWithInternalError() throws Exception {
        doThrow(new IllegalArgumentException()).when(practitionerRoleService).addOrUpdatePractitionerRole((PractitionerRole) any());
        postRequestWithJsonContent(BASE_URL + BATCH_SAVE_ENDPOINT , practitionerRoleListJson, MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerRoleService).addOrUpdatePractitionerRole(argumentCaptor.capture());
        verifyNoMoreInteractions(practitionerRoleService);
    }

    @Test
    public void testBatchSaveWithJsonSyntaxException() throws Exception {
        doThrow(new JsonSyntaxException("Unable to parse JSON")).when(practitionerRoleService).addOrUpdatePractitionerRole((PractitionerRole) any());
        postRequestWithJsonContent(BASE_URL + BATCH_SAVE_ENDPOINT , practitionerRoleListJson, MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerRoleService).addOrUpdatePractitionerRole(argumentCaptor.capture());
        verifyNoMoreInteractions(practitionerRoleService);
    }


    private static PractitionerRole initTestPractitionerRole1(){
        PractitionerRole practitionerRole = new PractitionerRole();
        practitionerRole.setIdentifier("pr1-identifier");
        practitionerRole.setActive(true);
        practitionerRole.setOrganizationIdentifier("org1");
        practitionerRole.setPractitionerIdentifier("p1-identifier");
        PractitionerRoleCode code = new PractitionerRoleCode();
        code.setText("pr1Code");
        practitionerRole.setCode(code);
        return practitionerRole;
    }

    private static PractitionerRole initTestPractitionerRole2(){
        PractitionerRole practitionerRole = new PractitionerRole();
        practitionerRole.setIdentifier("pr2-identifier");
        practitionerRole.setActive(true);
        practitionerRole.setOrganizationIdentifier("org1");
        practitionerRole.setPractitionerIdentifier("p2-identifier");
        PractitionerRoleCode code = new PractitionerRoleCode();
        code.setText("pr2Code");
        practitionerRole.setCode(code);
        return practitionerRole;
    }

    @Override
    protected void assertListsAreSameIgnoringOrder(List<PractitionerRole> expectedList, List<PractitionerRole> actualList) {
        if (expectedList == null || actualList == null) {
            throw new AssertionError("One of the lists is null");
        }

        assertEquals(expectedList.size(), actualList.size());

        Set<String> expectedIds = new HashSet<>();
        for (PractitionerRole practitionerRole : expectedList) {
            expectedIds.add(practitionerRole.getIdentifier());
        }

        for (PractitionerRole practitionerRole : actualList) {
            assertTrue(expectedIds.contains(practitionerRole.getIdentifier()));
        }
    }
}
