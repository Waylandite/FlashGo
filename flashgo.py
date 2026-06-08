from __future__ import annotations

from dataclasses import dataclass
from typing import ClassVar
from uuid import uuid4


@dataclass(frozen=True)
class Product:
    name: str
    price: float

    def __post_init__(self) -> None:
        if self.price <= 0:
            raise ValueError("price must be greater than zero")


@dataclass(frozen=True)
class CartItem:
    product: Product
    quantity: int

    def __post_init__(self) -> None:
        if self.quantity <= 0:
            raise ValueError("quantity must be greater than zero")

    @property
    def total_price(self) -> float:
        return self.product.price * self.quantity


class ShoppingCart:
    def __init__(self) -> None:
        self._items: dict[str, CartItem] = {}

    def add_item(self, product: Product, quantity: int = 1) -> None:
        if quantity <= 0:
            raise ValueError("quantity must be greater than zero")

        existing = self._items.get(product.name)
        if existing:
            quantity += existing.quantity

        self._items[product.name] = CartItem(product=product, quantity=quantity)

    @property
    def items(self) -> list[CartItem]:
        return list(self._items.values())

    @property
    def subtotal(self) -> float:
        return sum(item.total_price for item in self._items.values())

    def checkout(self, delivery_address: str) -> "DeliveryOrder":
        if not delivery_address.strip():
            raise ValueError("delivery address is required")

        if not self._items:
            raise ValueError("cart is empty")

        return DeliveryOrder(
            order_id=uuid4().hex,
            items=tuple(self.items),
            delivery_address=delivery_address,
            _status="pending",
        )


@dataclass
class DeliveryOrder:
    order_id: str
    items: tuple[CartItem, ...]
    delivery_address: str
    _status: str

    _ALLOWED_STATUSES: ClassVar[tuple[str, ...]] = (
        "pending",
        "preparing",
        "out_for_delivery",
        "delivered",
    )

    @property
    def status(self) -> str:
        return self._status

    def update_status(self, new_status: str) -> None:
        if new_status not in self._ALLOWED_STATUSES:
            raise ValueError(f"invalid status: {new_status}")

        current_index = self._ALLOWED_STATUSES.index(self.status)
        new_index = self._ALLOWED_STATUSES.index(new_status)
        if new_index < current_index:
            raise ValueError("cannot move order status backwards")

        self._status = new_status
