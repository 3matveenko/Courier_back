package com.example.courier.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Assign {

    private Driver driver;

    private List<Order> orders;
}
