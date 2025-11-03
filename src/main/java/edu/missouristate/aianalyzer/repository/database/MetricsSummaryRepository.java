package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.MetricsSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricsSummaryRepository extends JpaRepository<MetricsSummary, Integer> {
}
