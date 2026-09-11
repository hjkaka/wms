package com.example.wms_backend.controller;

import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.ReportQueryDTO;
import com.example.wms_backend.service.ReportService;
import com.example.wms_backend.vo.AbcVO;
import com.example.wms_backend.vo.DormantVO;
import com.example.wms_backend.vo.TurnoverVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 报表接口（s3-5）
 * 只读分析，任何已登录角色可访问
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // GET /api/report/turnover?startDate=&endDate=  → 库存周转率
    @GetMapping("/turnover")
    public Result<List<TurnoverVO>> turnover(ReportQueryDTO dto) {
        return Result.success(reportService.turnover(dto));
    }

    // GET /api/report/dormant?days=30  → 呆滞分析
    @GetMapping("/dormant")
    public Result<List<DormantVO>> dormant(ReportQueryDTO dto) {
        return Result.success(reportService.dormant(dto));
    }

    // GET /api/report/abc?startDate=&endDate=  → ABC 分类
    @GetMapping("/abc")
    public Result<List<AbcVO>> abc(ReportQueryDTO dto) {
        return Result.success(reportService.abc(dto));
    }
}