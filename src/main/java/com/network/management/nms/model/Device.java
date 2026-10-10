package com.network.management.nms.model;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String host;

    private String vendor;

    private String model;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status = DeviceStatus.UNKNOWN;

    @ElementCollection(
            targetClass = ManagementProtocol.class,
            fetch = FetchType.EAGER
    )
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "device_management_protocols",
            joinColumns = @JoinColumn(name = "device_id")
    )
    @Column(name = "protocol")
    private Set<ManagementProtocol> managementProtocols = new HashSet<>();
}