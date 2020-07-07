package com.practice.useakka.pojo;

import com.practice.useakka.models.Additional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Collection;
import java.util.stream.LongStream;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {

    long cost;
    ImmutableList<DishDto> dishes;
    ImmutableList<Discount> discounts;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private final MutableList<DishDto> dishes;
        private final MutableList<Discount> discounts;

        private Builder() {
            dishes = Lists.mutable.empty();
            discounts = Lists.mutable.empty();
        }

        public Builder addDish(DishDto dish) {
            dishes.add(dish);
            return this;
        }

        public Builder addDiscount(Discount discount) {
            discounts.add(discount);
            return this;
        }

        public Order build() {
            long cost = LongStream.concat(
                    dishes.stream()
                            .mapToLong(dishDto -> dishDto.getDish().getCost()),
                    dishes.stream()
                            .map(DishDto::getAdditional)
                            .flatMap(Collection::stream)
                            .mapToLong(Additional::getCost)
            ).reduce(0L, Long::sum);

            long discount = 0L;
            for (Discount discountObject : discounts)
                if (discountObject.getType().equals(Discount.Type.VALUE))
                    discount += discountObject.getValue();
                else if (discountObject.getType().equals(Discount.Type.PERCENT))
                    discount += (double) cost * ((double) discountObject.getValue() / 100L);

            return new Order(cost - discount, dishes.toImmutable(), discounts.toImmutable());
        }
    }
}

