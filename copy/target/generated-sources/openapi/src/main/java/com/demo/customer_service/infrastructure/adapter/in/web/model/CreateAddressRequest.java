package com.demo.customer_service.infrastructure.adapter.in.web.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateAddressRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-13T09:19:43.397853810+02:00[Europe/Berlin]", comments = "Generator version: 7.4.0")
public class CreateAddressRequest {

  private String street;

  private String city;

  private String postalCode;

  private String country;

  public CreateAddressRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateAddressRequest(String street, String city, String postalCode, String country) {
    this.street = street;
    this.city = city;
    this.postalCode = postalCode;
    this.country = country;
  }

  public CreateAddressRequest street(String street) {
    this.street = street;
    return this;
  }

  /**
   * Get street
   * @return street
  */
  @NotNull @Size(min = 1, max = 255) 
  @Schema(name = "street", example = "Carnotstr. 4", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("street")
  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public CreateAddressRequest city(String city) {
    this.city = city;
    return this;
  }

  /**
   * Get city
   * @return city
  */
  @NotNull @Size(min = 1, max = 100) 
  @Schema(name = "city", example = "Berlin", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("city")
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public CreateAddressRequest postalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  /**
   * Get postalCode
   * @return postalCode
  */
  @NotNull @Size(min = 1, max = 20) 
  @Schema(name = "postalCode", example = "10587", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("postalCode")
  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public CreateAddressRequest country(String country) {
    this.country = country;
    return this;
  }

  /**
   * Get country
   * @return country
  */
  @NotNull @Size(min = 2, max = 2) 
  @Schema(name = "country", example = "DE", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("country")
  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateAddressRequest createAddressRequest = (CreateAddressRequest) o;
    return Objects.equals(this.street, createAddressRequest.street) &&
        Objects.equals(this.city, createAddressRequest.city) &&
        Objects.equals(this.postalCode, createAddressRequest.postalCode) &&
        Objects.equals(this.country, createAddressRequest.country);
  }

  @Override
  public int hashCode() {
    return Objects.hash(street, city, postalCode, country);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateAddressRequest {\n");
    sb.append("    street: ").append(toIndentedString(street)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

