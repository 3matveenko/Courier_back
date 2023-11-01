package com.example.courier.model.data;

import com.example.courier.model.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignReject {

    private int queue_id;

    private String driver_token;

    private List<Order> orders;
}
