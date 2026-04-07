package com.eric.store.common.entity;

import com.eric.store.common.util.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Column(nullable = false, length = 512)
    @Convert(converter = EncryptedStringConverter.class)
    private String name;

    @Column(nullable = false, length = 512)
    @Convert(converter = EncryptedStringConverter.class)
    private String addressLine1;

    @Column(length = 512)
    @Convert(converter = EncryptedStringConverter.class)
    private String addressLine2;

    @Column(nullable = false, length = 512)
    @Convert(converter = EncryptedStringConverter.class)
    private String city;

    @Column(nullable = false, length = 512)
    @Convert(converter = EncryptedStringConverter.class)
    private String state;

    @Column(nullable = false, length = 512)
    @Convert(converter = EncryptedStringConverter.class)
    private String zip;

    @Column(nullable = false, length = 512)
    @Convert(converter = EncryptedStringConverter.class)
    private String country;
}
