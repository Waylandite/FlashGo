import unittest

from flashgo import Product, ShoppingCart


class FlashGoTests(unittest.TestCase):
    def test_cart_subtotal_with_multiple_items(self):
        cart = ShoppingCart()
        cart.add_item(Product("Apple", 1.5), quantity=3)
        cart.add_item(Product("Milk", 2.0), quantity=2)

        self.assertEqual(cart.subtotal, 8.5)

    def test_checkout_creates_pending_delivery_order(self):
        cart = ShoppingCart()
        cart.add_item(Product("Bread", 3.0), quantity=1)

        order = cart.checkout("221B Baker Street")

        self.assertEqual(order.status, "pending")
        self.assertEqual(order.delivery_address, "221B Baker Street")
        self.assertEqual(len(order.items), 1)

    def test_delivery_status_progression(self):
        cart = ShoppingCart()
        cart.add_item(Product("Rice", 5.0), quantity=1)
        order = cart.checkout("Baker Street")

        order.update_status("preparing")
        order.update_status("out_for_delivery")
        order.update_status("delivered")

        self.assertEqual(order.status, "delivered")

    def test_invalid_quantity_raises_error(self):
        cart = ShoppingCart()

        with self.assertRaises(ValueError):
            cart.add_item(Product("Egg", 0.5), quantity=0)


if __name__ == "__main__":
    unittest.main()
