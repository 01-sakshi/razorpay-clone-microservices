package common_lib.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Embeddable
@Getter
@Setter
@ToString
public class Money {

    @NotNull(message = "Provide valid amount units")
    private Long amountUnits;
    @NotBlank(message = "Provide valid currency")
    private String currency;

    private Money(Long amountUnits, String currency) {
        this.amountUnits = amountUnits;
        this.currency = currency;
    }

    public Money() {

    }

    /**
     * Creates a money value without converting the supplied minor units.
     *
     * @param amountUnits amount in the smallest currency unit
     * @param currency    currency code associated with the amount
     * @return the money value
     */
    public static Money of(Long amountUnits, String currency) {
        return new Money(amountUnits, currency);
    }

    /**
     * Adds the minor-unit amounts while retaining this value's currency; currencies must match.
     *
     * @param other amount to add
     * @return a new money value containing the sum
     * @throws IllegalArgumentException if the currencies differ
     */
    public Money add(Money other) {
        if (!other.currency.equals(this.currency)) throw new IllegalArgumentException("Chal be!");
        return new Money(this.amountUnits + other.amountUnits, this.currency);
    }

    /**
     * Subtracts the other minor-unit amount while retaining this value's currency; currencies must match.
     *
     * @param other amount to subtract
     * @return a new money value containing the difference
     * @throws IllegalArgumentException if the currencies differ
     */
    public Money subtract(Money other) {
        if (!other.currency.equals(this.currency)) throw new IllegalArgumentException("Chal be!");
        return new Money(this.amountUnits - other.amountUnits, this.currency);
    }
}
