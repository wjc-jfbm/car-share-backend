package com.carshare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Evidence;
import java.util.List;

public interface EvidenceService {
    Evidence getEvidenceDetail(Long evidenceId);
    List<Evidence> adminList(Evidence evidence);
    Page<Evidence> adminListPage(Page<Evidence> page, Evidence evidence);
}
