package com.investpro.controller;

import com.investpro.model.TradeResult;
import com.investpro.service.PortfolioService;
import com.investpro.service.TradingService;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PortfolioController {
    private final AuthController auth;
    private final PortfolioService portfolio;
    private final TradingService trading;

    public PortfolioController(AuthController auth, PortfolioService portfolio, TradingService trading) {
        this.auth = auth;
        this.portfolio = portfolio;
        this.trading = trading;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(HttpSession session) {
        return portfolio.dashboard(auth.currentUserId(session));
    }

    @GetMapping("/transactions")
    public List<Map<String, Object>> transactions(HttpSession session) {
        return portfolio.transactions(auth.currentUserId(session));
    }

    @PostMapping("/trades")
    public TradeResult trade(@RequestBody TradeRequest request, HttpSession session) {
        if (request.investmentId() == null || request.quantity() == null) {
            throw new IllegalArgumentException("Select an investment and enter a quantity.");
        }
        return trading.trade(auth.currentUserId(session), request.investmentId(), request.action(), request.quantity());
    }

    @GetMapping("/transactions/export.csv")
    public ResponseEntity<byte[]> exportTransactions(HttpSession session) throws IOException {
        List<Map<String, Object>> rows = portfolio.transactions(auth.currentUserId(session));
        StringBuilder csv = new StringBuilder("Date,Action,Symbol,Investment,Quantity,Unit Price,Total Amount\r\n");
        for (Map<String, Object> row : rows) {
            csv.append(csv(row.get("createdAt"))).append(',')
                    .append(csv(row.get("action"))).append(',')
                    .append(csv(row.get("symbol"))).append(',')
                    .append(csv(row.get("name"))).append(',')
                    .append(csv(row.get("quantity"))).append(',')
                    .append(csv(row.get("price"))).append(',')
                    .append(csv(row.get("totalAmount"))).append("\r\n");
        }
        // Demonstrates Java file handling: persist a local export before sending it to the browser.
        Path exportDirectory = Path.of("exports");
        Files.createDirectories(exportDirectory);
        Path exportFile = exportDirectory.resolve("investpro-transactions.csv");
        Files.writeString(exportFile, csv.toString(), StandardCharsets.UTF_8);
        byte[] fileBytes = Files.readAllBytes(exportFile);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=investpro-transactions.csv")
                .body(fileBytes);
    }

    private static String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    public record TradeRequest(Long investmentId, String action, Integer quantity) { }
}
