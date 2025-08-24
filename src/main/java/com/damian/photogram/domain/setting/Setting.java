package com.damian.photogram.domain.setting;

import com.damian.photogram.domain.customer.model.Customer;
import jakarta.persistence.*;

@Entity
@Table(name = "customer_settings")
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Column
    private String settingKey;

    @Column
    private String settingValue;

    public Setting() {

    }

    public Setting(Customer customer) {
        this.customer = customer;
    }

    public Setting(Customer customer, String settingKey, String settingValue) {
        this.customer = customer;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public static Setting create(Customer owner) {
        return new Setting(owner);
    }

    public Long getId() {
        return id;
    }

    public Setting setId(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        return "Setting {" +
               "id=" + id +
               "customerId=" + getCustomer().getId() +
               "settingKey=" + getSettingKey() +
               "settingValue=" + getSettingValue() +
               "}";
    }

    public String getSettingKey() {
        return settingKey;
    }

    public Setting setSettingKey(String key) {
        this.settingKey = key;
        return this;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public Setting setSettingValue(String settingValue) {
        this.settingValue = settingValue;
        return this;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Setting setCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public boolean isOwner(Customer customer) {
        return this.customer.getId().equals(customer.getId());
    }
}
