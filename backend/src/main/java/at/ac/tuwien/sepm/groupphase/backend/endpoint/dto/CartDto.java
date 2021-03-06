package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class CartDto {

    private Long id;

    @NotNull(message = "CartItems darf nicht null sein")
    private Set<CartItemDto> cartItems = new HashSet<>();

    public CartDto() {
    }

    public CartDto(Long id, Set<CartItemDto> cartItems) {
        this.id = id;
        this.cartItems = cartItems;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<CartItemDto> getCartItems() {
        return cartItems;
    }

    public void setCartItems(Set<CartItemDto> cartItems) {
        this.cartItems = cartItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CartDto cartDto = (CartDto) o;
        return Objects.equals(id, cartDto.id) && Objects.equals(cartItems, cartDto.cartItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cartItems);
    }

}


