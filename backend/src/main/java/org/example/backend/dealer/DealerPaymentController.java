package org.example.backend.dealer;

import jakarta.validation.Valid;
import org.example.backend.dealer.dto.DealerLedgerResponse;
import org.example.backend.dealer.dto.DealerOutstandingResponse;
import org.example.backend.dealer.dto.DealerPaymentRequest;
import org.example.backend.dealer.dto.DealerPaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dealer-payments")
public class DealerPaymentController {

    private final DealerPaymentService service;

    public DealerPaymentController(
            DealerPaymentService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DealerPaymentResponse payDealer(
            @Valid
            @RequestBody
            DealerPaymentRequest request
    ) {
        return service.payDealer(request);
    }
}