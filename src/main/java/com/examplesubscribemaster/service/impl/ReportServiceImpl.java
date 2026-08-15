package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.currency.CurrencyService;
import com.examplesubscribemaster.entity.PaymentHistory;
import com.examplesubscribemaster.repository.PaymentHistoryRepository;
import com.examplesubscribemaster.service.ReportService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {
    private static final String[] HEADERS = {"Subscription name", "Monthly price", "Original currency", "Base currency equivalent", "Annual total expense"};
    private final PaymentHistoryRepository repo;
    private final CurrencyService currency;

    public ReportServiceImpl(PaymentHistoryRepository r, CurrencyService c) {
        repo = r;
        currency = c;
    }

    private Map<Long, List<PaymentHistory>> grouped(Long u, int year) {
        var ps = repo.findBySubscriptionUserIdAndPaymentDateBetween(u, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        Map<Long, List<PaymentHistory>> grouped = new LinkedHashMap<>();
        ps.forEach(p -> grouped.computeIfAbsent(p.getSubscription().getId(), k -> new ArrayList<>()).add(p));
        return grouped;
    }

    @Transactional(readOnly = true)
    public ByteArrayResource annualXlsx(Long u, int year) {
        var grouped = grouped(u, year);
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sh = wb.createSheet("Annual Expense");
            var h = sh.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) h.createCell(i).setCellValue(HEADERS[i]);
            int row = 1;
            for (var list : grouped.values()) {
                var p = list.getFirst();
                BigDecimal annual = list.stream().map(PaymentHistory::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal base = list.stream().map(x -> currency.toUzs(x.getAmount(), x.getCurrency())).reduce(BigDecimal.ZERO, BigDecimal::add);
                var r = sh.createRow(row++);
                r.createCell(0).setCellValue(p.getSubscription().getName());
                r.createCell(1).setCellValue(p.getSubscription().getPrice().doubleValue());
                r.createCell(2).setCellValue(p.getCurrency().name());
                r.createCell(3).setCellValue(base.doubleValue());
                r.createCell(4).setCellValue(annual.doubleValue());
            }
            wb.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Could not create report", e);
        }
    }

    @Transactional(readOnly = true)
    public ByteArrayResource annualCsv(Long u, int year) {
        var grouped = grouped(u, year);
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", HEADERS)).append("\r\n");
        for (var list : grouped.values()) {
            var p = list.getFirst();
            BigDecimal annual = list.stream().map(PaymentHistory::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal base = list.stream().map(x -> currency.toUzs(x.getAmount(), x.getCurrency())).reduce(BigDecimal.ZERO, BigDecimal::add);
            csv.append(escape(p.getSubscription().getName())).append(',')
                    .append(p.getSubscription().getPrice()).append(',')
                    .append(p.getCurrency().name()).append(',')
                    .append(base).append(',')
                    .append(annual).append("\r\n");
        }
        return new ByteArrayResource(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
