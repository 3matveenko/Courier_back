package com.example.courier.model.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AllSettings {

    public String crmToken;

    public String timerSum;

    public String timerStartTime;

    public String angle;

    public String timerSumNodriver;

    public String feLatitude;

    public String feLongtitude;

    public String protocol;

    public String serverName;

    public String serverPort;

    public String backQueueName;

    public String rabbitServerName;

    public String rabbitServerPort;

    public String rabbitUsername;

    public String rabbitPassword;

    public String orderDistributionPrinciple;

    public String beginningWork;
}
