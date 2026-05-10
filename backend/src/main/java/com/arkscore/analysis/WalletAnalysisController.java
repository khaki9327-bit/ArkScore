package com.arkscore.analysis;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WalletAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(WalletAnalysisController.class);

    private final WalletAnalysisService walletAnalysisService;

    public WalletAnalysisController(WalletAnalysisService walletAnalysisService) {
        this.walletAnalysisService = walletAnalysisService;
    }

    @PostMapping("/analyze")
    public WalletAnalysisResponse analyze(@Valid @RequestBody AnalyzeWalletRequest request) {
        log.debug("POST /api/analyze request: {}", request);

        WalletAnalysisResponse response = walletAnalysisService.analyze(request.walletAddress());

        log.debug("POST /api/analyze response: {}", response);

        return response;
    }
}
