package shop;

import java.util.*;

public class ShoppingCart {
    private final List<Item> items = new ArrayList<>();
    private double discount = 0;

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public void applyDiscount(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException();
        }
        this.discount = percentage;
    }

    public double getTotalPrice() {
        double total = items.stream()
                .mapToDouble(Item::getTotalPrice)
                .sum();
        return total - (total * discount / 100);
    }
}
