package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.TransferInfoRequestDTO;
import com.amlakie.usermanagment.dto.TransferInfoResponseDTO;
import com.amlakie.usermanagment.service.TransferInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "http://localhost:3000")
public class TransferInfoController {

    private final TransferInfoService transferInfoService;

    public TransferInfoController(TransferInfoService transferInfoService) {
        this.transferInfoService = transferInfoService;
    }

    @PostMapping
    public ResponseEntity<TransferInfoResponseDTO> createTransfer(
            @RequestBody TransferInfoRequestDTO transferInfoDTO) {
        TransferInfoResponseDTO response = transferInfoService.createTransfer(transferInfoDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TransferInfoResponseDTO>> getAllTransfers() {
        List<TransferInfoResponseDTO> transfers = transferInfoService.getAllTransfers();
        return ResponseEntity.ok(transfers);
    }

}