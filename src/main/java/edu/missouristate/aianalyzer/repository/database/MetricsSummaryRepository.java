package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.MetricsSummary;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * MetricsSummary repository which keeps track of total virus scans, AI Descriptions, and id
 */
public interface MetricsSummaryRepository extends JpaRepository<MetricsSummary, Integer> {
}
