package com.carshare.admin.controller.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.common.core.controller.BaseController;
import com.carshare.common.core.domain.AjaxResult;
import com.carshare.common.core.page.TableDataInfo;
import com.carshare.common.utils.PageUtils;
import com.carshare.entity.Evidence;
import com.carshare.service.EvidenceService;

@RestController
@RequestMapping("/business/evidence")
public class EvidenceAdminController extends BaseController
{
    @Autowired
    private EvidenceService evidenceService;

    @PreAuthorize("@ss.hasPermi('business:evidence:list')")
    @GetMapping("/list")
    public TableDataInfo list(Evidence evidence)
    {
        Page<Evidence> page = PageUtils.startMpPage();
        Page<Evidence> result = evidenceService.adminListPage(page, evidence);
        return getDataTable(result);
    }

    @PreAuthorize("@ss.hasPermi('business:evidence:query')")
    @GetMapping("/{evidenceId}")
    public AjaxResult getInfo(@PathVariable Long evidenceId)
    {
        return success(evidenceService.getEvidenceDetail(evidenceId));
    }
}
