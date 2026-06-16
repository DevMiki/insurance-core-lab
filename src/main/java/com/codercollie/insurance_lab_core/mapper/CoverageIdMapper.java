package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class CoverageIdMapper {

    public List<Long> toSortedIds(Set<CoverageEntity> coverages) {
        return coverages.stream()
                .map(CoverageEntity::getId)
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}
