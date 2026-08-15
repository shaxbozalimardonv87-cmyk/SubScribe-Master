package com.examplesubscribemaster.controller;

import com.examplesubscribemaster.security.UserPrincipal;
import com.examplesubscribemaster.service.ReportService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService s;

    public ReportController(ReportService s) {
        this.s = s;
    }

    @GetMapping("/annual")
    ResponseEntity<ByteArrayResource> annual(Authentication a, @RequestParam int year, @RequestParam(defaultValue = "xlsx") String format) {
        Long userId = ((UserPrincipal) a.getPrincipal()).id();
        if ("csv".equalsIgnoreCase(format)) {
            var r = s.annualCsv(userId, year);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=annual-report-" + year + ".csv")
                    .contentLength(r.contentLength()).body(r);
        }
        var r = s.annualXlsx(userId, year);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=annual-report-" + year + ".xlsx")
                .contentLength(r.contentLength()).body(r);
    }
}
