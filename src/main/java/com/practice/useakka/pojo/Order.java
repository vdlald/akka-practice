package com.practice.useakka.pojo;

import com.practice.useakka.models.Additional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.partition.list.PartitionMutableList;

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
            final long dishesCost = dishes.collectLong(dishDto -> dishDto.getDish().getCost())
                    .reduceIfEmpty(Long::sum, 0L);
            final long additionalCost = dishes.flatCollect(DishDto::getAdditional)
                    .collectLong(Additional::getCost)
                    .reduceIfEmpty(Long::sum, 0L);
            final long cost = dishesCost + additionalCost;

            final PartitionMutableList<Discount> partition = discounts
                    .partition(discount -> discount.getType().equals(Discount.Type.VALUE));

            final long reduceDiscount1 = partition.getSelected()
                    .collectLong(Discount::getValue)
                    .reduceIfEmpty(Long::sum, 0L);
            final long reduceDiscount2 = partition.getRejected()
                    .collectLong(discount -> (long) ((double) cost * ((double) discount.getValue() / 100L)))
                    .reduceIfEmpty(Long::sum, 0L);
            final long discount = reduceDiscount1 + reduceDiscount2;

            return new Order(cost - discount, dishes.toImmutable(), discounts.toImmutable());
        }
    }
}

