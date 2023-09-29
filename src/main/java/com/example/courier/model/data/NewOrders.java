package com.example.courier.model.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewOrders {

    private String code;

    private long millisecondsSinceEpoch;

    private String body;

}
