package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Tag(name = "recommendationrequest")
@RequestMapping("/api/RecommendationRequest")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

    @Autowired
    RecommendationRequestRepository recommendationrequestRepository;

    @Operation(summary= "List all recommendations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequest> allRecommendationRequest() {
        Iterable<RecommendationRequest> recommendationrequest = recommendationrequestRepository.findAll();
        return recommendationrequest;
    }

    @Operation(summary= "Create a new recommendation")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public RecommendationRequest postRecommendationRequest(
        @Parameter(name="requesterEmail") @RequestParam String requesterEmail,
        @Parameter(name="professorEmail") @RequestParam String professorEmail,
        @Parameter(name="explanation") @RequestParam String explanation,
        @Parameter(name="dateRequested") @RequestParam("dataRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
        @Parameter(name="dateNeeded") @RequestParam("dateNeeded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded,
        @Parameter(name="done") @RequestParam boolean done
        )
        throws JsonProcessingException
        {
        log.info("dateRequested={}, dateNeeded={}", dateRequested, dateNeeded);
        RecommendationRequest recommendationrequest = new RecommendationRequest();
        recommendationrequest.setRequesterEmail(requesterEmail);
        recommendationrequest.setProfessorEmail(professorEmail);
        recommendationrequest.setExplanation(explanation);
        recommendationrequest.setDateRequested(dateRequested);
        recommendationrequest.setDateNeeded(dateNeeded);
        recommendationrequest.setDone(done);

        RecommendationRequest savedRecommendationRequest = recommendationrequestRepository.save(recommendationrequest);

        return savedRecommendationRequest;
    }

    @Operation(summary= "Get a single recommendation")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public RecommendationRequest getById(
            @Parameter(name="id") @RequestParam Long id) {
        RecommendationRequest recommendationrequest = recommendationrequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        return recommendationrequest;
    }

    @Operation(summary= "Delete a recommendation")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRecommendationRequest(
            @Parameter(name="id") @RequestParam Long id) {
        RecommendationRequest recommendationrequest = recommendationrequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        recommendationrequestRepository.delete(recommendationrequest);
        return genericMessage("RecommendationRequest with id %s deleted".formatted(id));
    }

    @Operation(summary= "Update a single recommendation")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public RecommendationRequest updateRecommendationRequest(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid RecommendationRequest incoming) {

        RecommendationRequest recommendationrequest = recommendationrequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));


        recommendationrequest.setRequesterEmail(incoming.getRequesterEmail());
        recommendationrequest.setProfessorEmail(incoming.getProfessorEmail());
        recommendationrequest.setExplanation(incoming.getExplanation());
        recommendationrequest.setDateRequested(incoming.getDateRequested());
        recommendationrequest.setDateNeeded(incoming.getDateNeeded());
        recommendationrequest.setDone(incoming.getDone());

        recommendationrequestRepository.save(recommendationrequest);

        return recommendationrequest;
    }
}
