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

    @GetMapping("/checkout-config")
    public ResponseEntity<RazorpayCheckoutConfigResponse> checkoutConfig() {
        return ResponseEntity.ok(topUpService.checkoutConfig());
    }

    @PostMapping("/wallet/{walletId}")
    public ResponseEntity<TopUpIntentResponse> initiate(
            @PathVariable Long walletId,
            @Valid @RequestBody TopUpIntentRequest request) {
        return ResponseEntity.ok(topUpService.initiate(walletId, request));
    }

    @PostMapping("/{intentId}/verify-payment")
    public ResponseEntity<TopUpIntentResponse> verifyPayment(
            @PathVariable Long intentId,
            @Valid @RequestBody RazorpayPaymentVerificationRequest request) {
        return ResponseEntity.ok(topUpService.verifyRazorpayPayment(intentId, request));
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<PageResponse<TopUpIntentResponse>> list(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(topUpService.listMine(
                walletId,
                PageRequest.of(
                        page,
                        Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "createdAt")))));
    }

    /**
     * Real Razorpay webhook endpoint. Signature is verified against the exact raw body.
     * Configure this URL in Razorpay Dashboard:
     * https://YOUR_HOST/api/topups/razorpay/webhook
     */
    @PostMapping(value = "/razorpay/webhook", consumes = "application/json")
    public ResponseEntity<Void> razorpayWebhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String rawBody) {
        topUpService.handleRazorpayWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}
