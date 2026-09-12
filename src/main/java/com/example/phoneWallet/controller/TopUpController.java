package com.example.phoneWallet.controller;

import com.example.phoneWallet.dto.*;
import com.example.phoneWallet.services.TopUpService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topups")
public class TopUpController {
    private final TopUpService topUpService;

    public TopUpController(TopUpService topUpService) {
        this.topUpService = topUpService;
    }

    @PostMapping("/wallet/{walletId}")
    public ResponseEntity<TopUpIntentResponse> initiate(@PathVariable Long walletId, @Valid @RequestBody TopUpIntentRequest request) {
        return ResponseEntity.ok(topUpService.initiate(walletId, request));
    }

    @PostMapping("/{intentId}/demo-complete")
    public ResponseEntity<TopUpIntentResponse> completeDemo(@PathVariable Long intentId) {
        return ResponseEntity.ok(topUpService.completeDemo(intentId));
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<PageResponse<TopUpIntentResponse>> list(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(topUpService.listMine(
                walletId, PageRequest.of(page, Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt")))));
    }

    @PostMapping("/webhook")
    public ResponseEntity<TopUpIntentResponse> webhook(
            @RequestHeader("X-Topup-Signature") String signature,
            @Valid @RequestBody TopUpWebhookRequest request) {
        return ResponseEntity.ok(topUpService.handleWebhook(request, signature));
    }
}
