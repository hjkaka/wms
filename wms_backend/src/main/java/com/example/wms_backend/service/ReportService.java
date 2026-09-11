package com.example.wms_backend.service;

import com.example.wms_backend.dto.ReportQueryDTO;
import com.example.wms_backend.vo.AbcVO;
import com.example.wms_backend.vo.DormantVO;
import com.example.wms_backend.vo.TurnoverVO;
import java.util.List;

/**
 * 报表业务（s3-5）
 * 口径按金额：出库金额 = 出库数量 × 单价
 */
public interface ReportService {

    // 库存周转率（按金额口径）
    List<TurnoverVO> turnover(ReportQueryDTO dto);

    // 呆滞分析（超过 days 天未出库且有库存）
    List<DormantVO> dormant(ReportQueryDTO dto);

    // ABC 分类（按期间出库金额累计占比）
    List<AbcVO> abc(ReportQueryDTO dto);
}