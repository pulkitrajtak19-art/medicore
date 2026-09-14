/**
 * Supabase Data Transfer & Migration Script
 * Direct connection to: postgresql://postgres:pulkit1907TAK@db.zeriogmzhbsqilcaewuc.supabase.co:5432/postgres
 */
const { Client } = require("pg");

const connectionString = "postgresql://postgres:pulkit1907TAK@db.zeriogmzhbsqilcaewuc.supabase.co:5432/postgres";

async function transferAllData() {
  const client = new Client({
    connectionString,
    ssl: { rejectUnauthorized: false }
  });

  console.log("Connecting to Supabase PostgreSQL database...");
  await client.connect();
  console.log("Connected successfully to:", client.host);

  // 1. Schema setup
  await client.query(`
    CREATE TABLE IF NOT EXISTS public.users (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      email TEXT,
      phone TEXT,
      role TEXT NOT NULL,
      abha_id TEXT UNIQUE,
      age INT,
      gender TEXT,
      blood_group TEXT,
      emergency_contact TEXT,
      city TEXT,
      qr_token TEXT,
      created_at TIMESTAMPTZ DEFAULT NOW(),
      updated_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.health_vitals (
      id TEXT PRIMARY KEY,
      patient_id TEXT,
      timestamp TEXT NOT NULL,
      bp_systolic INT,
      bp_diastolic INT,
      pulse_rate INT,
      temperature_f DOUBLE PRECISION,
      weight_kg DOUBLE PRECISION,
      spo2_percent INT,
      recorded_by TEXT,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.doctor_consultations (
      id TEXT PRIMARY KEY,
      patient_id TEXT,
      doctor_name TEXT NOT NULL,
      doctor_specialty TEXT,
      clinic_name TEXT,
      timestamp TEXT,
      chief_complaint TEXT,
      diagnosis TEXT,
      clinical_remarks TEXT,
      vitals_snapshot TEXT,
      follow_up_days INT,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.prescriptions (
      id TEXT PRIMARY KEY,
      consultation_id TEXT,
      patient_id TEXT,
      patient_name TEXT,
      doctor_name TEXT,
      doctor_specialty TEXT,
      clinic_name TEXT,
      prescription_date TEXT,
      qr_prescription_token TEXT,
      is_dispensed BOOLEAN DEFAULT false,
      dispensed_date TEXT,
      dispensed_by_centre TEXT,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.prescription_items (
      id TEXT PRIMARY KEY,
      prescription_id TEXT REFERENCES public.prescriptions(id) ON DELETE CASCADE,
      medicine_name TEXT NOT NULL,
      dosage TEXT,
      frequency TEXT,
      timing TEXT,
      duration_days INT,
      quantity INT,
      instructions TEXT,
      is_dispensed BOOLEAN DEFAULT false
    );

    CREATE TABLE IF NOT EXISTS public.medication_reminders (
      id TEXT PRIMARY KEY,
      prescription_id TEXT,
      medicine_name TEXT NOT NULL,
      dosage TEXT,
      time_slot TEXT,
      meal_instruction TEXT,
      is_taken_today BOOLEAN DEFAULT false,
      streak_days INT DEFAULT 0,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.patient_consents (
      id TEXT PRIMARY KEY,
      patient_id TEXT,
      requester_name TEXT,
      requester_role TEXT,
      facility_name TEXT,
      categories TEXT,
      duration_hours INT,
      status TEXT,
      granted_at TEXT,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.case_intakes (
      id TEXT PRIMARY KEY,
      patient_id TEXT,
      timestamp TEXT,
      intake_mode TEXT,
      language TEXT,
      voice_transcript TEXT,
      chief_symptoms TEXT[],
      symptom_duration TEXT,
      severity_level TEXT,
      allergies TEXT[],
      current_medicines TEXT[],
      attached_record_title TEXT,
      attached_record_ocr_text TEXT,
      is_synthesized BOOLEAN DEFAULT true,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.doctor_review_reports (
      id TEXT PRIMARY KEY,
      intake_id TEXT,
      patient_id TEXT,
      patient_name TEXT,
      abha_id TEXT,
      generated_at TEXT,
      chief_complaint_summary TEXT,
      extracted_symptoms TEXT[],
      severity_level TEXT,
      duration TEXT,
      allergies TEXT[],
      current_medicines TEXT[],
      vitals_snapshot TEXT,
      attached_report_title TEXT,
      attached_ocr_summary TEXT,
      clinical_impression TEXT,
      editable_doctor_notes TEXT,
      provisional_diagnosis TEXT,
      is_doctor_confirmed BOOLEAN DEFAULT false,
      confirmed_by_doctor_name TEXT,
      confirmed_at TEXT,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.facility_checkins (
      id TEXT PRIMARY KEY,
      facility_id TEXT,
      facility_name TEXT,
      department TEXT,
      counter_number TEXT,
      token_number TEXT,
      timestamp TEXT,
      estimated_wait_minutes INT,
      status TEXT,
      abha_shared TEXT,
      qr_raw_data TEXT,
      security_checksum TEXT,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.audit_logs (
      id TEXT PRIMARY KEY,
      timestamp TEXT,
      actor_name TEXT,
      actor_role TEXT,
      action TEXT,
      details TEXT,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.health_metrics (
      id TEXT PRIMARY KEY,
      patient_id TEXT,
      steps_today INT,
      step_goal INT,
      calories_burned INT,
      distance_km DOUBLE PRECISION,
      water_glasses INT,
      sleep_hours DOUBLE PRECISION,
      updated_at TIMESTAMPTZ DEFAULT NOW()
    );
  `);

  console.log("Database schema verified. Querying current counts...");

  const tables = [
    "users",
    "health_vitals",
    "doctor_consultations",
    "prescriptions",
    "prescription_items",
    "medication_reminders",
    "patient_consents",
    "case_intakes",
    "doctor_review_reports",
    "facility_checkins",
    "audit_logs",
    "health_metrics"
  ];

  for (const tbl of tables) {
    const res = await client.query(`SELECT COUNT(*) as count FROM public.${tbl};`);
    console.log(`- ${tbl}: ${res.rows[0].count} records in Supabase`);
  }

  await client.end();
}

transferAllData().catch(console.error);
