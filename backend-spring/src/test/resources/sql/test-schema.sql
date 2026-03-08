CREATE TYPE user_role AS ENUM ('user', 'admin');

CREATE TYPE submission_status AS ENUM (
  'received',
  'parse_error',
  'parsed_ok',
  'scoring_in_progress',
  'scored',
  'finalized'
);

CREATE TABLE questionnaires (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  version INTEGER NOT NULL,
  status VARCHAR(255) NOT NULL,
  template_file_key VARCHAR(255),
  created_at TIMESTAMPTZ
);

CREATE TABLE questions (
  id UUID PRIMARY KEY,
  questionnaire_id UUID NOT NULL,
  question_tab VARCHAR(255) NOT NULL,
  question_category VARCHAR(255) NOT NULL,
  question_index VARCHAR(255) NOT NULL,
  question_text VARCHAR(255) NOT NULL,
  question_type VARCHAR(255) NOT NULL,
  question_help VARCHAR(255)
);

CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role user_role NOT NULL,
  created_at TIMESTAMPTZ
);

CREATE TABLE submissions (
  id UUID PRIMARY KEY,
  lc_id UUID NOT NULL,
  user_id UUID NOT NULL,
  uploaded_file_key VARCHAR(255),
  status submission_status NOT NULL,
  submitted_at TIMESTAMPTZ,
  parsed_at TIMESTAMPTZ,
  error_json JSONB
);

CREATE TABLE answers (
  id UUID PRIMARY KEY,
  submission_id UUID NOT NULL,
  question_id VARCHAR(255) NOT NULL,
  raw_answer TEXT,
  normalized_json JSONB
);

CREATE TABLE lc (
  id UUID PRIMARY KEY,
  questionnaire_id UUID,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  year INTEGER NOT NULL
);
