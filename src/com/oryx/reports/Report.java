package com.oryx.reports;

import com.oryx.model.Order;
import java.util.List;

/** Base class for all ORYX sales reports. */
public abstract class Report implements Reportable {
    @Override
    public abstract void generateReport(List<Order> orders);
}
