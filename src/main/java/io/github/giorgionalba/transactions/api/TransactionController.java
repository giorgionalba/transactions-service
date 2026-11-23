package io.github.giorgionalba.transactions.api;

import io.github.giorgionalba.transactions.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PutMapping("/{id}")
    public StatusResponse create(@PathVariable @Positive long id, @RequestBody @Valid TransactionRequest request) {
        transactionService.create(request.toTransaction(id));
        return StatusResponse.ok();
    }

    @GetMapping("/types/{type}")
    public List<Long> findIdsByType(@PathVariable String type) {
        return transactionService.findIdsByType(type);
    }
}
