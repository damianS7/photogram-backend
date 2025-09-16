package com.damian.photogram.web.user.dto.mapper;

import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.user.dto.response.CustomerDto;
import com.damian.photogram.web.user.dto.response.CustomerWithProfileDto;
import org.springframework.data.domain.Page;

import java.util.List;

public class CustomerDtoMapper {
    public static CustomerDto toCustomerDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static CustomerWithProfileDto toCustomerWithProfileDto(Customer customer) {
        return new CustomerWithProfileDto(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                ProfileDtoMapper.toProfileDto(customer.getProfile()),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static List<CustomerDto> toCustomerDtoList(List<Customer> customers) {
        return customers
                .stream()
                .map(
                        CustomerDtoMapper::toCustomerDto
                ).toList();
    }

    public static Page<CustomerDto> toCustomerDtoWithPagination(Page<Customer> customers) {
        return customers.map(
                CustomerDtoMapper::toCustomerDto
        );
    }
}
