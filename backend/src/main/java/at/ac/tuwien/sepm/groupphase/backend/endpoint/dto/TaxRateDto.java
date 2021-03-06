package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import java.util.Objects;

public class TaxRateDto {
    private Long id;
    private Double percentage;
    private Double calculationFactor;

    public Double getCalculationFactor() {
        return calculationFactor;
    }

    public void setCalculationFactor(Double calculationFactor) {
        this.calculationFactor = calculationFactor;
    }

    public TaxRateDto(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaxRateDto)) {
            return false;
        }
        TaxRateDto taxRateDto = (TaxRateDto) o;
        return Objects.equals(id, taxRateDto.id)
            && Objects.equals(percentage, taxRateDto.percentage);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, percentage);
    }

    @Override
    public String toString() {
        return "TaxRateDto{"
            +
            "id=" + id
            +
            ", percentage=" + percentage
            +
            '}';
    }
}
