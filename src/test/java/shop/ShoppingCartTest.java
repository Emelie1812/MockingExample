package shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ShoppingCartTest {
    private ShoppingCart cart;

    @BeforeEach
    void setup() {
        cart = new ShoppingCart();
    }

    @Test
    void newCart_shouldHaveZeroTotalPrice() {
        assertThat(cart.getTotalPrice()).isEqualTo(0.0);
    }

    @Test
    void addingItem_shouldIncreaseTotalPrice() {
        cart.addItem(new Item("Milk", 20.0, 1));

        assertThat(cart.getTotalPrice()).isEqualTo(20.0);
    }

    @Test
    void updatingQuantity_shouldUpdateTotalPrice() {
        Item milk = new Item("Milk", 20.0, 1);
        cart.addItem(milk);

        milk.updateQuantity(3);

        assertThat(cart.getTotalPrice()).isEqualTo(60.0);
    }

    @Test
    void removingItem_shouldDecreaseTotalPrice() {
        Item milk = new Item("Milk", 20.0, 1);
        cart.addItem(milk);

        cart.removeItem("milk");

        assertThat(cart.getTotalPrice()).isEqualTo(0.0);
    }

    @Test
    void applyingDiscount_shouldReduceTotalPrice() {
        cart.addItem(new Item("Milk", 20.0, 2));

        cart.applyDiscount(10);

        assertThat(cart.getTotalPrice()).isEqualTo(36.0);
    }

    @Test
    void discountOver100_shouldThrowException() {
        assertThatThrownBy(() ->
                cart.applyDiscount(200))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeQuantity_shouldThrowException() {
        assertThatThrownBy(() ->
                new Item("Milk", 20.0, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removingNonExistentItem_shouldNotCrash() {
        cart.removeItem("Banana");

        assertThat(cart.getTotalPrice()).isEqualTo(0.0);
    }
}
