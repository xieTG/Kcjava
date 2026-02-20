package com.xietg.kc.db.entity;

/**
 * Must match the values of the PostgreSQL enum type "submission_status" from infra/postgres/schema.sql.
 */
public enum SubmissionStatus {
    received,
    parse_error,
    parsed_ok,
    scoring_in_progress,
    scored,
    finalized
}
