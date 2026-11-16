package com.oryx.reports;

import com.oryx.model.Order;
import java.util.List;

/** Common behaviour for reports generated from orders. */
public interface Reportable {
    void generateReport(List<Order> orders);
}
