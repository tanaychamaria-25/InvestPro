package com.investpro.controller;

import com.investpro.model.Investment;
import com.investpro.model.MarketMovementResult;
import com.investpro.service.AuthService;
import com.investpro.service.MarketSimulationService;
import jakarta.servlet.http.HttpSession;
import com.investpro.repository.InvestmentRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {
    private final InvestmentRepository investments;
    private final MarketSimulationService marketSimulation;
    private final AuthService auth;

    public InvestmentController(InvestmentRepository investments, MarketSimulationService marketSimulation, AuthService auth) {
        this.investments = investments;
        this.marketSimulation = marketSimulation;
        this.auth = auth;
    }

    @GetMapping
    public List<Investment> list() { return investments.findAll(); }

    @PostMapping("/simulate")
    public MarketMovementResult simulate(HttpSession session) {
        Object id = session.getAttribute("INVESTPRO_USER_ID");
        if (!(id instanceof Number number)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Please sign in to continue.");
        }
        auth.findById(number.longValue());
        return marketSimulation.simulateMovement();
    }
}
