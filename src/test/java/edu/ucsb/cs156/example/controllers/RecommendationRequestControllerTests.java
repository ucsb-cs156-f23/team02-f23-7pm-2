package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.controllers.RecommendationRequestController;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRequestRepository recommendationrequestRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsbdiningcommons/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange
        
                when(recommendationrequestRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest?id=1"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(recommendationrequestRepository, times(1)).findById(eq(1L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("RecommendationRequest with id 1 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recommendation() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2023-10-31T00:00:00");

                RecommendationRequest recommendation1 = RecommendationRequest.builder()
                    .requesterEmail("requesterEmail1@gmail.com")
                    .professorEmail("professorEmail1@gmail.com")
                    .explanation("explanation1")
                    .dateRequested(ldt1)
                    .dateNeeded(ldt1)
                    .done(false)
                    .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2023-10-30T00:00:00");

                RecommendationRequest recommendation2 = RecommendationRequest.builder()
                    .requesterEmail("requesterEmail2@gmail.com")
                    .professorEmail("professorEmail2@gmail.com")
                    .explanation("explanation2")
                    .dateRequested(ldt2)
                    .dateNeeded(ldt2)
                    .done(false)
                    .build();

                ArrayList<RecommendationRequest> expectedRecommendation = new ArrayList<>();
                expectedRecommendation.addAll(Arrays.asList(recommendation1,recommendation2));

                when(recommendationrequestRepository.findAll()).thenReturn(expectedRecommendation);

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationrequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRecommendation);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsbdiningcommons...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/RecommendationRequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/RecommendationRequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_recommendation() throws Exception {
                // arrange

                LocalDateTime dr = LocalDateTime.parse("2023-05-20T00:00:00");
                LocalDateTime dn = LocalDateTime.parse("2023-11-15T00:00:00");

                RecommendationRequest recommendation1 = RecommendationRequest.builder()
                                        .requesterEmail("phtcon@ucsb.edu")
                                        .professorEmail("phtcon@ucsb.edu")
                                        .explanation("explanation")
                                        .dateRequested(dr)
                                        .dateNeeded(dn)
                                        .done(true)
                                        .build();

                when(recommendationrequestRepository.save(eq(recommendation1))).thenReturn(recommendation1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/RecommendationRequest/post?requesterEmail=phtcon@ucsb.edu&professorEmail=phtcon@ucsb.edu&explanation=explanation&dateRequested=2023-05-20T00:00:00&dateNeeded=2023-11-15T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationrequestRepository, times(1)).save(recommendation1);
                String expectedJson = mapper.writeValueAsString(recommendation1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/ucsbdiningcommons?...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2023-11-01T00:00:00");

                RecommendationRequest recommendation = RecommendationRequest.builder()
                        .requesterEmail("requesterEmail@gmail.com")
                        .professorEmail("professorEmail@gmail.com")
                        .explanation("explanation")
                        .dateRequested(ldt)
                        .dateNeeded(ldt)
                        .done(false)
                        .build();

                when(recommendationrequestRepository.findById(eq(1L))).thenReturn(Optional.of(recommendation));

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationrequestRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(recommendation);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for DELETE /api/ucsbdiningcommons?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2023-11-01T00:00:00");

                RecommendationRequest recommendation = RecommendationRequest.builder()
                        .requesterEmail("requesterEmail@gmail.com")
                        .professorEmail("professorEmail@gmail.com")
                        .explanation("explanation")
                        .dateRequested(ldt)
                        .dateNeeded(ldt)
                        .done(false)
                        .build();

                when(recommendationrequestRepository.findById(eq(2L))).thenReturn(Optional.of(recommendation));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/RecommendationRequest?id=2")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationrequestRepository, times(1)).findById(eq(2L));
                verify(recommendationrequestRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 2 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(recommendationrequestRepository.findById(eq(2L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/RecommendationRequest?id=2")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationrequestRepository, times(1)).findById(eq(2L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 2 not found", json.get("message"));
        }

        // Tests for PUT /api/ucsbdiningcommons?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_commons() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-10-31T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-10-31T00:00:00");

                RecommendationRequest recOrig = RecommendationRequest.builder()
                    .requesterEmail("requesterEmail@gmail.com")
                    .professorEmail("professorEmail@gmail.com")
                    .explanation("explanation")
                    .dateRequested(ldt1)
                    .dateNeeded(ldt1)
                    .done(false)
                    .build();

                RecommendationRequest recEdited = RecommendationRequest.builder()
                    .requesterEmail("requesterEmail1@gmail.com")
                    .professorEmail("professorEmail1@gmail.com")
                    .explanation("explanation1")
                    .dateRequested(ldt2)
                    .dateNeeded(ldt2)
                    .done(true)
                    .build();

                String requestBody = mapper.writeValueAsString(recEdited);

                when(recommendationrequestRepository.findById(eq(3L))).thenReturn(Optional.of(recOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/RecommendationRequest?id=3")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationrequestRepository, times(1)).findById(eq(3L));
                verify(recommendationrequestRepository, times(1)).save(recEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }


        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2023-10-31T00:00:00");

                RecommendationRequest editedRec = RecommendationRequest.builder()
                    .requesterEmail("requesterEmail@gmail.com")
                    .professorEmail("professorEmail@gmail.com")
                    .explanation("explanation")
                    .dateRequested(ldt)
                    .dateNeeded(ldt)
                    .done(false)
                    .build();

                String requestBody = mapper.writeValueAsString(editedRec);

                when(recommendationrequestRepository.findById(eq(3L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/RecommendationRequest?id=3")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationrequestRepository, times(1)).findById(eq(3L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 3 not found", json.get("message"));

        }
}