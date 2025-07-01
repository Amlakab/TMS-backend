package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.focform.FOCRequest;
import com.amlakie.usermanagment.entity.focform.FOC;
import com.amlakie.usermanagment.service.FOCService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/foc-forms")
@RequiredArgsConstructor
public class FOCController {

    private final FOCService focService;

    @PostMapping
    public ResponseEntity<FOC> createFOCForm(@RequestBody @Valid FOCRequest request) {
        FOC savedFOC = focService.saveFOCForm(request);
        return ResponseEntity.ok(savedFOC);
    }
}
