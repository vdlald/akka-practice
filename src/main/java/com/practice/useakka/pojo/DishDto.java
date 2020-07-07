package com.practice.useakka.pojo;

import com.practice.useakka.models.Additional;
import com.practice.useakka.models.Dish;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DishDto {

    private Dish dish;

    private List<Additional> additional;

    public static DishDto of(Dish dish, List<Additional> additional) {
        return new DishDto(dish, additional);
    }

    public static DishDto of(Dish dish, Additional... additional) {
        return new DishDto(dish, Arrays.asList(additional));
    }

}
