package com.damian.photogram.customer;

import com.damian.photogram.customer.dto.CustomerDTO;
import com.damian.photogram.customer.dto.CustomerDTOMapper;
import com.damian.photogram.customer.dto.CustomerNonFriendDTO;
import com.damian.photogram.customer.dto.CustomerWithProfileDTO;
import com.damian.photogram.customer.http.request.CustomerEmailUpdateRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/api/v1")
@RestController
public class CustomerController {
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // endpoint to fetch all non friends from logged customer filtered by name.
    @GetMapping("/customers/{name}")
    public ResponseEntity<?> searchCustomers(
            @PathVariable @NotNull
            String name
    ) {
        Set<Customer> customers = customerService.searchCustomers(name);
        Set<CustomerNonFriendDTO> customersDTO = CustomerDTOMapper.apply(customers);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customersDTO);
    }

    // endpoint to receive logged customer
    @GetMapping("/customers/me")
    public ResponseEntity<?> getLoggedCustomerData() {
        Customer customer = customerService.getCustomer();
        CustomerWithProfileDTO dto = CustomerDTOMapper.toCustomerWithProfileDTO(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(dto);
    }

    // endpoint to modify logged customer email
    @PatchMapping("/customers/me/email")
    public ResponseEntity<?> updateLoggedCustomerEmail(
            @Validated @RequestBody
            CustomerEmailUpdateRequest request
    ) {
        Customer customer = customerService.updateEmail(request);
        CustomerDTO customerDTO = CustomerDTOMapper.toCustomerDTO(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }
}

