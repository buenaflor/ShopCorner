package at.ac.tuwien.sepm.groupphase.backend.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String name;
    private String description;
    @DecimalMin("0.0")
    private Double price;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category", referencedColumnName = "id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tax_rate")
    private TaxRate taxRate;



    public Product() {
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

    public void setPrice(Double amount) {
        this.price = amount;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }
        Product product = (Product) o;
        return Objects.equals(id, product.id)
            && Objects.equals(name, product.name);

    }


    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Product{"
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
            ", taxRate=" + taxRate
            +
            '}';
    }
}
