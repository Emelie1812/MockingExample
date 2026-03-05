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
}
