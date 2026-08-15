package com.examplesubscribemaster.service;

import org.springframework.core.io.ByteArrayResource;

public interface ReportService {
    ByteArrayResource annualXlsx(Long userId, int year);

    ByteArrayResource annualCsv(Long userId, int year);
}
