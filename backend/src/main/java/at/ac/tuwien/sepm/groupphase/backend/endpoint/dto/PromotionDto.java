package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class PromotionDto {

    private Long id;
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    private double discount;
    @NotNull
    private LocalDate creationDate;
    @NotNull
    private LocalDate expirationDate;
    @NotNull
    @NotBlank
    private String code;
    @NotNull
    private double minimumOrderValue;

    public PromotionDto() {
    }

    public PromotionDto(Long id, String name, double discount, LocalDate creationDate, LocalDate expirationDate, String code, double minimumOrderValue) {
        this.id = id;
        this.name = name;
        this.discount = discount;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.code = code;
        this.minimumOrderValue = minimumOrderValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getMinimumOrderValue() {
        return minimumOrderValue;
    }

    public void setMinimumOrderValue(double minimumOrderValue) {
        this.minimumOrderValue = minimumOrderValue;
    }
}
