package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.TravelRequestReqRes;
import com.amlakie.usermanagment.entity.TravelRequest;
import com.amlakie.usermanagment.repository.TravelRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TravelRequestService {

    @Autowired
    private TravelRequestRepository travelRequestRepository;

    public TravelRequestReqRes createTravelRequest(TravelRequestReqRes request) {
        TravelRequestReqRes response = new TravelRequestReqRes();
        try {
            TravelRequest travelRequest = new TravelRequest();
            travelRequest.setStartingPlace(request.getStartingPlace());
            travelRequest.setTravelerName(request.getTravelerName());
            travelRequest.setCarType(request.getCarType());
            travelRequest.setStartingDate(request.getStartingDate());
            travelRequest.setDepartment(request.getDepartment());
            travelRequest.setDestinationPlace(request.getDestinationPlace());
            travelRequest.setTravelReason(request.getTravelReason());
            travelRequest.setTravelDistance(request.getTravelDistance());
            travelRequest.setReturnDate(request.getReturnDate());
            travelRequest.setJobStatus(request.getJobStatus());
            travelRequest.setClaimantName(request.getClaimantName());
            travelRequest.setApprovement(request.getApprovement());
            travelRequest.setTeamLeaderName(request.getTeamLeaderName());

            TravelRequest savedRequest = travelRequestRepository.save(travelRequest);
            response.setTravelRequest(savedRequest);
            response.setMessage("Travel request created successfully");
            response.setCodStatus(200);
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public TravelRequestReqRes getAllTravelRequests() {
        TravelRequestReqRes response = new TravelRequestReqRes();
        try {
            List<TravelRequest> requests = travelRequestRepository.findAll();
            response.setTravelRequestList(requests);
            response.setCodStatus(200);
            response.setMessage("All travel requests retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public TravelRequestReqRes getTravelRequestById(Long id) {
        TravelRequestReqRes response = new TravelRequestReqRes();
        try {
            Optional<TravelRequest> request = travelRequestRepository.findById(id);
            if (request.isPresent()) {
                response.setTravelRequest(request.get());
                response.setCodStatus(200);
                response.setMessage("Travel request retrieved successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Travel request not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public TravelRequestReqRes updateTravelRequest(Long id, TravelRequestReqRes updateRequest) {
        TravelRequestReqRes response = new TravelRequestReqRes();
        try {
            Optional<TravelRequest> requestOptional = travelRequestRepository.findById(id);
            if (requestOptional.isPresent()) {
                TravelRequest existingRequest = requestOptional.get();
                existingRequest.setStartingPlace(updateRequest.getStartingPlace());
                existingRequest.setTravelerName(updateRequest.getTravelerName());
                existingRequest.setCarType(updateRequest.getCarType());
                existingRequest.setStartingDate(updateRequest.getStartingDate());
                existingRequest.setDepartment(updateRequest.getDepartment());
                existingRequest.setDestinationPlace(updateRequest.getDestinationPlace());
                existingRequest.setTravelReason(updateRequest.getTravelReason());
                existingRequest.setTravelDistance(updateRequest.getTravelDistance());
                existingRequest.setReturnDate(updateRequest.getReturnDate());
                existingRequest.setJobStatus(updateRequest.getJobStatus());
                existingRequest.setClaimantName(updateRequest.getClaimantName());
                existingRequest.setApprovement(updateRequest.getApprovement());
                existingRequest.setTeamLeaderName(updateRequest.getTeamLeaderName());

                TravelRequest updatedRequest = travelRequestRepository.save(existingRequest);
                response.setTravelRequest(updatedRequest);
                response.setCodStatus(200);
                response.setMessage("Travel request updated successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Travel request not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public TravelRequestReqRes deleteTravelRequest(Long id) {
        TravelRequestReqRes response = new TravelRequestReqRes();
        try {
            if (travelRequestRepository.existsById(id)) {
                travelRequestRepository.deleteById(id);
                response.setCodStatus(200);
                response.setMessage("Travel request deleted successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Travel request not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public TravelRequestReqRes searchTravelRequests(String query) {
        TravelRequestReqRes response = new TravelRequestReqRes();
        try {
            List<TravelRequest> requests = travelRequestRepository
                    .findByTravelerNameContainingOrTravelReasonContaining(query, query);
            response.setTravelRequestList(requests);
            response.setCodStatus(200);
            response.setMessage("Search results retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public TravelRequestReqRes getRequestsByDepartment(String department) {
        TravelRequestReqRes response = new TravelRequestReqRes();
        try {
            List<TravelRequest> requests = travelRequestRepository.findByDepartment(department);
            response.setTravelRequestList(requests);
            response.setCodStatus(200);
            response.setMessage("Department requests retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public TravelRequestReqRes getRequestsByStatus(String status) {
        TravelRequestReqRes response = new TravelRequestReqRes();
        try {
            List<TravelRequest> requests = travelRequestRepository.findByJobStatus(status);
            response.setTravelRequestList(requests);
            response.setCodStatus(200);
            response.setMessage("Status-based requests retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }
}