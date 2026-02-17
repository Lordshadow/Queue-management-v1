package com.queue.management.service;

import com.queue.management.enums.CounterName;

public interface StatisticsService {

    // Calculate average service time for a counter
    // Based on last 10 completed tokens
    // Returns average in minutes
    double getAverageServiceTime(CounterName counterName);

    // Calculate estimated wait time for a student
    // Based on: tokens ahead × average service time
    // Returns estimated wait time in minutes
    int getEstimatedWaitTime(CounterName counterName, int position);

    // Get service trend for a counter
    // Compares current average with previous average
    // Returns: "FASTER", "SLOWER", "STABLE"
    String getServiceTrend(CounterName counterName);

    // Get previous average service time
    // Used to calculate trend
    double getPreviousAverageServiceTime(CounterName counterName);
}
