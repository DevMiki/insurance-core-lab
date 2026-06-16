package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoverageIdMapperTest {

    private final CoverageIdMapper coverageIdMapper = new CoverageIdMapper();

    @Test
    void mapsCoveragesToSortedIds() {
        CoverageEntity theft = coverageWithId(11L, "THEFT");
        CoverageEntity fire = coverageWithId(10L, "FIRE");

        List<Long> coverageIds = coverageIdMapper.toSortedIds(new LinkedHashSet<>(List.of(theft, fire)));

        assertEquals(List.of(10L, 11L), coverageIds);
    }

    private CoverageEntity coverageWithId(Long id, String code) {
        CoverageEntity coverage = new CoverageEntity(
                code,
                code + " coverage",
                "Test coverage",
                new BigDecimal("100.00")
        );
        ReflectionTestUtils.setField(coverage, "id", id);
        return coverage;
    }
}
