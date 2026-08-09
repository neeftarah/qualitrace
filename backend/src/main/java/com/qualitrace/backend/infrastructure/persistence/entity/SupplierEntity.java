package com.qualitrace.backend.infrastructure.persistence.entity;

import com.qualitrace.backend.domain.type.SupplierStatus;
import com.qualitrace.backend.domain.type.UserStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "suppliers")
public class SupplierEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private SupplierStatus status;

    protected SupplierEntity() {
        // requis par JPA/Hibernate
    }

    public SupplierEntity(Long id, String code, String name, String address, SupplierStatus status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.address = address;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public SupplierStatus getStatus() { return status; }

    public void setStatus(SupplierStatus status) { this.status = status; }
}