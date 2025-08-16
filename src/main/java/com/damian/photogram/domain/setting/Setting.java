package com.damian.photogram.domain.setting;

import com.damian.photogram.domain.customer.model.Customer;
import jakarta.persistence.*;

@Entity
@Table(name = "customer_settings")
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Column
    private String settingKey;

    @Column
    private String settingValue;

    public Setting() {

    }

    public Setting(Customer customer, String settingKey, String settingValue) {
        this.customer = customer;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Setting {" +
               "id=" + id +
               "settingKey=" + getSettingKey() +
               "settingValue=" + getSettingValue() +
               "}";
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String key) {
        this.settingKey = key;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
