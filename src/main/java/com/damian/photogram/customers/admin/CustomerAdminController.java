package com.damian.photogram.customers.admin;

import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.CustomerService;
import com.damian.photogram.customers.dto.CustomerDTO;
import com.damian.photogram.customers.dto.CustomerDTOMapper;
import com.damian.photogram.customers.dto.CustomerWithAllDataDTO;
import com.damian.photogram.customers.http.request.CustomerEmailUpdateRequest;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class CustomerAdminController {
    private final CustomerService customerService;

    @Autowired
    public CustomerAdminController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // endpoint to receive all customers
    @GetMapping("/admin/customers")
    public ResponseEntity<?> getCustomers(
            @PageableDefault(size = 2, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Customer> customers = customerService.getCustomers(pageable);
        Page<CustomerDTO> customerDTO = CustomerDTOMapper.toCustomerDTOPage(customers);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }

    // endpoint to receive certain customers
    @GetMapping("/admin/customers/{id}")
    public ResponseEntity<?> getCustomer(
            @PathVariable @Positive
            Long id
    ) {
        Customer customer = customerService.getCustomer(id);
        CustomerWithAllDataDTO customerDTO = CustomerDTOMapper.toCustomerWithAllDataDTO(customer);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }

    // endpoint to delete a customers
    @DeleteMapping("/admin/customers/{id}")
    public ResponseEntity<?> deleteCustomer(
            @PathVariable @Positive
            Long id
    ) {
        customerService.deleteCustomer(id);

        // returns 204
        return ResponseEntity.noContent().build();
    }

    // endpoint to update customers email
    @PatchMapping("/admin/customers/{id}/email")
    public ResponseEntity<?> updateCustomerEmail(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            CustomerEmailUpdateRequest request
    ) {
        Customer customer = customerService.updateEmail(id, request.newEmail());
        CustomerDTO customerDTO = CustomerDTOMapper.toCustomerDTO(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }
}

