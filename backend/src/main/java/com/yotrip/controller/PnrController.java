package com.yotrip.controller;

import com.yotrip.dto.*;
import com.yotrip.service.PnrService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pnr")
@Tag(name = "PNR", description = "PNR lookup, flight status, and web check-in")
public class PnrController {

    private final PnrService pnrService;

    public PnrController(PnrService pnrService) {
        this.pnrService = pnrService;
    }

    @GetMapping("/{code}")
    @SecurityRequirements
    public ResponseEntity<PnrInfoResponse> lookup(@PathVariable String code) {
        PnrInfoResponse info = pnrService.fetchPnrInfo(code);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    @GetMapping("/{code}/flight-status")
    @SecurityRequirements
    public PnrLookupMessageResponse flightStatus(@PathVariable String code) {
        return pnrService.flightStatus(code);
    }

    @GetMapping("/{code}/web-checkin")
    @SecurityRequirements
    public PnrLookupMessageResponse webCheckin(@PathVariable String code) {
        return pnrService.webCheckin(code);
    }
}
