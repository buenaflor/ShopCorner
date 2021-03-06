package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepm.groupphase.backend.entity.Category;
import at.ac.tuwien.sepm.groupphase.backend.entity.TaxRate;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

public class ProductDto {
    private Long id;
    @NotBlank
    @Size(min = 3, max = 50, message = "Name muss mindestens 3 und maximal 50 Zeichen enthalten")
    private String name;
    @Length(max = 200, message = "Beschreibung darf maximal 200 Zeichen enthalten")
    @NotBlank(message = "Beschreibung darf nicht leer sein")
    private String description;
    @DecimalMin(value = "0.0", message = "Preis kann nicht 0€ sein")
    private Double price;
    private Category category;
    private TaxRate taxRate;
    private boolean locked;
    private String picture;
    private Long saleCount;
    private LocalDateTime expiresAt;
    private boolean deleted;

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public ProductDto() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public TaxRate getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
    }

    public Long getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(Long saleCount) {
        this.saleCount = saleCount;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductDto)) {
            return false;
        }
        ProductDto productDto = (ProductDto) o;
        return Objects.equals(id, productDto.id)
            && Objects.equals(name, productDto.name)
            && Objects.equals(description, productDto.description)
            && Objects.equals(price, productDto.price)
            && Objects.equals(category, productDto.category)
            && Objects.equals(taxRate, productDto.taxRate)
            && Objects.equals(expiresAt, productDto.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, price, category, taxRate, expiresAt);
    }

    @Override
    public String toString() {
        return "ProductDto{"
            +
            "id=" + id
            +
            ", name='" + name + '\''
            +
            ", description='" + description + '\''
            +
            ", amount=" + price
            +
            ", category=" + category
            +
            ", isLocked=" + locked
            +
            ", taxRate=" + taxRate
            +
            ", expiresAt=" + expiresAt
            +
            '}';
    }
}



