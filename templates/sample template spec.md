Absolutely—here’s a **full, layered mapping specification** for a single template using the approach we discussed. I’ve chosen a realistic scenario:

> **Template:** *Enrollment Form – Medicare – California (v1)*  
> **Context:** Market = Medicare, State = CA  
> **Goal:** Show inheritance from common + market + state layers, plus template-specific overrides; and provide the **resolved mapping** your mapping engine would generate.

***

## 1) Layered Catalog (Common + Market + State)

> These catalogs are reusable across templates. They aren’t “the template spec” yet—they’re the base building blocks.

```yaml
# =========================
# LAYERED MAPPING CATALOGS
# =========================

$schema: "https://example.org/mapping-spec.schema.json"
catalog_version: "2025.11.01"

# ---------------------------------------
# 1) COMMON FIELDS (shared across forms)
# ---------------------------------------
catalogs:
  common:
    fields:
      - logical_name: MemberFirstName
        pdf_field: Member_First_Name
        source_path: source.member.name.first
        type: string
        required: true
        transforms: [trim, title_case]
        validation:
          max_length: 50

      - logical_name: MemberLastName
        pdf_field: Member_Last_Name
        source_path: source.member.name.last
        type: string
        required: true
        transforms: [trim, title_case]
        validation:
          max_length: 50

      - logical_name: MemberDOB
        pdf_field: Member_DOB
        source_path: source.member.dob
        type: date
        required: true
        transforms:
          - format_date: "MM/dd/yyyy"
        validation:
          min_date: "01/01/1900"
          max_date: "today"

      - logical_name: MemberSSN
        pdf_field: Member_SSN
        source_path: source.member.ssn
        type: string
        required: false
        transforms: [remove_non_digits]
        validation:
          pattern: "^[0-9]{9}$"
        visibility:
          condition: "source.member.ssn != null"

      - logical_name: ResidentialAddressLine1
        pdf_field: Residential_Address_Line1
        source_path: source.member.address.residential.line1
        type: string
        required: true
        transforms: [trim, upper_if_required]
        validation:
          max_length: 100

      - logical_name: ResidentialCity
        pdf_field: Residential_City
        source_path: source.member.address.residential.city
        type: string
        required: true
        transforms: [trim, title_case]

      - logical_name: ResidentialState
        pdf_field: Residential_State
        source_path: source.member.address.residential.state
        type: string
        required: true
        validation:
          enum_ref: "states_us"

      - logical_name: ResidentialZip
        pdf_field: Residential_Zip
        source_path: source.member.address.residential.zip
        type: string
        required: true
        transforms: [remove_non_digits]
        validation:
          pattern: "^[0-9]{5}(-[0-9]{4})?$"

      - logical_name: MailingAddressSameAsResidential
        pdf_field: Mailing_Same_As_Residential
        source_path: source.member.address.mailing.same_as_residential
        type: boolean
        required: false
        default: true

      - logical_name: MailingAddressLine1
        pdf_field: Mailing_Address_Line1
        source_path: "coalesce(source.member.address.mailing.line1, source.member.address.residential.line1)"
        type: string
        required: true
        transforms: [trim]

      - logical_name: PrimaryPhone
        pdf_field: Primary_Phone
        source_path: source.member.contact.phone.primary
        type: string
        required: false
        transforms: [normalize_phone_us]
        validation:
          pattern: "^\\(\\d{3}\\) \\d{3}-\\d{4}$"

      - logical_name: Email
        pdf_field: Email
        source_path: source.member.contact.email
        type: string
        required: false
        validation:
          pattern: "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"

      - logical_name: Gender
        pdf_field: Gender
        source_path: source.member.gender
        type: string
        required: false
        validation:
          enum: ["Male", "Female", "Non-Binary", "Unspecified"]

      - logical_name: EffectiveDate
        pdf_field: Effective_Date
        source_path: source.coverage.effective_date
        type: date
        required: true
        transforms:
          - format_date: "MM/dd/yyyy"

      - logical_name: PlanId
        pdf_field: Plan_ID
        source_path: source.coverage.plan_id
        type: string
        required: true
        validation:
          max_length: 20

      - logical_name: LanguagePreference
        pdf_field: Language_Preference
        source_path: source.member.language_preference
        type: string
        required: false
        validation:
          enum_ref: "languages_common"

      - logical_name: SignatureMember
        pdf_field: Signature_Member
        source_path: source.signatures.member.signature_image
        type: image
        required: true
        validation:
          content_type: ["image/png", "image/jpeg"]

      - logical_name: SignatureDate
        pdf_field: Signature_Date
        source_path: source.signatures.member.signed_at
        type: date
        required: true
        transforms:
          - format_date: "MM/dd/yyyy"

# ---------------------------------------
# 2) MARKET-SPECIFIC: Medicare, Medicaid
# ---------------------------------------
  market:
    Medicare:
      fields:
        - logical_name: MedicareBeneficiaryId
          pdf_field: Medicare_Beneficiary_ID
          source_path: source.member.medicare.mbi
          type: string
          required: true
          transforms: [trim, upper]
          validation:
            pattern: "^[A-Z0-9]{11}$"   # Example MBI format constraint
        - logical_name: MedicareConsent
          pdf_field: Medicare_Consent_Checkbox
          source_path: source.member.medicare.consent
          type: boolean
          required: true

    Medicaid:
      fields:
        - logical_name: MedicaidId
          pdf_field: Medicaid_ID
          source_path: source.member.medicaid.id
          type: string
          required: true

# ---------------------------------------
# 3) STATE-SPECIFIC: California
# ---------------------------------------
  state:
    CA:
      fields:
        - logical_name: County
          pdf_field: County
          source_path: source.member.address.residential.county
          type: string
          required: false
          transforms: [title_case]
        - logical_name: StateConsentCA
          pdf_field: CA_State_Consent
          source_path: source.state_specific.ca.consent
          type: boolean
          required: true
        - logical_name: AB1672DisclosureAck
          pdf_field: CA_AB1672_Disclosure_Acknowledgment
          source_path: source.state_specific.ca.ab1672_ack
          type: boolean
          required: false

# ---------------------------------------
# 4) CODELISTS (VALIDATION ENUMS)
# ---------------------------------------
codelists:
  states_us: ["AL","AK","AZ","AR","CA","CO","CT","DE","FL","GA","HI","IA","ID","IL","IN","KS","KY","LA","MA","MD","ME","MI","MN","MO","MS","MT","NC","ND","NE","NH","NJ","NM","NV","NY","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VA","VT","WA","WI","WV","WY","DC"]
  languages_common: ["English","Spanish","Chinese","Tagalog","Vietnamese","Korean","Arabic","Russian","Hindi","Other"]

# ---------------------------------------
# 5) TRANSFORM LIBRARY (REFERENCE)
# ---------------------------------------
transforms:
  trim: {}
  upper: {}
  title_case: {}
  remove_non_digits: {}
  format_date: {args: ["pattern"]}       # e.g., "MM/dd/yyyy"
  normalize_phone_us: {}
  upper_if_required: {args: ["flag_path"]}
  coalesce: {args: ["pathA","pathB"]}
```

***

## 2) Template Profile (Inheritance & Overrides)

> This section defines the **specific template** and how it composes the catalogs. It’s brief because it references the reusable layers.

```yaml
# ===============================
# TEMPLATE PROFILE (Declarative)
# ===============================

template_profile:
  id: "EnrollmentForm_Medicare_CA_v1"
  title: "Enrollment Form – Medicare – California"
  version: "1.0.0"
  pdf_template_file: "Enrollment_Medicare_CA_v1.pdf"
  applies_to:
    market: "Medicare"
    state: "CA"

  inherits:
    - catalog: common
    - catalog: market.Medicare
    - catalog: state.CA

  overrides:
    # Example: override residential address label to use Mailing if 'same_as' is true
    - target_logical_name: ResidentialAddressLine1
      replace:
        pdf_field: Residential_Address_Line1
        source_path: |
          if source.member.address.mailing.same_as_residential == true
          then source.member.address.residential.line1
          else source.member.address.mailing.line1
        comment: "Use mailing address when different; otherwise residential."

    # Example: hide SSN for Medicare if MBI is present (policy choice)
    - target_logical_name: MemberSSN
      replace:
        visibility:
          condition: "source.member.medicare.mbi == null"

  suppress:
    # Example: template does not capture Gender explicitly
    - logical_name: Gender

  additional:
    # Template-specific checkbox the plan added
    - logical_name: BrokerAttestation
      pdf_field: Broker_Attestation
      source_path: source.broker.attestation
      type: boolean
      required: false
```

***

## 3) **Resolved Mapping** (What the Engine Produces)

> This is the **fully expanded** mapping for this template after applying inherits + overrides + suppressions. It’s the artifact you ship to your AcroForm filling runtime.

```yaml
# ======================================
# RESOLVED MAPPING (for this template)
# ======================================

resolved_mapping:
  spec_meta:
    template_id: "EnrollmentForm_Medicare_CA_v1"
    version: "1.0.0"
    pdf_template_file: "Enrollment_Medicare_CA_v1.pdf"
    context:
      market: "Medicare"
      state: "CA"
      generation_timestamp: "2025-11-20T19:10:07+05:30"

  fields:
    - logical_name: MemberFirstName
      pdf_field: Member_First_Name
      source_path: source.member.name.first
      type: string
      required: true
      transforms: [trim, title_case]
      validation: { max_length: 50 }

    - logical_name: MemberLastName
      pdf_field: Member_Last_Name
      source_path: source.member.name.last
      type: string
      required: true
      transforms: [trim, title_case]
      validation: { max_length: 50 }

    - logical_name: MemberDOB
      pdf_field: Member_DOB
      source_path: source.member.dob
      type: date
      required: true
      transforms:
        - format_date: "MM/dd/yyyy"
      validation:
        min_date: "01/01/1900"
        max_date: "today"

    - logical_name: MemberSSN
      pdf_field: Member_SSN
      source_path: source.member.ssn
      type: string
      required: false
      transforms: [remove_non_digits]
      validation:
        pattern: "^[0-9]{9}$"
      visibility:
        condition: "source.member.medicare.mbi == null"  # overridden

    - logical_name: ResidentialAddressLine1
      pdf_field: Residential_Address_Line1
      # overridden to select mailing or residential dynamically
      source_path: |
        if source.member.address.mailing.same_as_residential == true
        then source.member.address.residential.line1
        else source.member.address.mailing.line1
      type: string
      required: true
      transforms: [trim, upper_if_required]
      validation: { max_length: 100 }

    - logical_name: ResidentialCity
      pdf_field: Residential_City
      source_path: source.member.address.residential.city
      type: string
      required: true
      transforms: [trim, title_case]

    - logical_name: ResidentialState
      pdf_field: Residential_State
      source_path: source.member.address.residential.state
      type: string
      required: true
      validation: { enum_ref: "states_us" }

    - logical_name: ResidentialZip
      pdf_field: Residential_Zip
      source_path: source.member.address.residential.zip
      type: string
      required: true
      transforms: [remove_non_digits]
      validation:
        pattern: "^[0-9]{5}(-[0-9]{4})?$"

    - logical_name: MailingAddressSameAsResidential
      pdf_field: Mailing_Same_As_Residential
      source_path: source.member.address.mailing.same_as_residential
      type: boolean
      required: false
      default: true

    - logical_name: MailingAddressLine1
      pdf_field: Mailing_Address_Line1
      source_path: "coalesce(source.member.address.mailing.line1, source.member.address.residential.line1)"
      type: string
      required: true
      transforms: [trim]

    - logical_name: PrimaryPhone
      pdf_field: Primary_Phone
      source_path: source.member.contact.phone.primary
      type: string
      required: false
      transforms: [normalize_phone_us]
      validation:
        pattern: "^\\(\\d{3}\\) \\d{3}-\\d{4}$"

    - logical_name: Email
      pdf_field: Email
      source_path: source.member.contact.email
      type: string
      required: false
      validation:
        pattern: "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"

    - logical_name: EffectiveDate
      pdf_field: Effective_Date
      source_path: source.coverage.effective_date
      type: date
      required: true
      transforms:
        - format_date: "MM/dd/yyyy"

    - logical_name: PlanId
      pdf_field: Plan_ID
      source_path: source.coverage.plan_id
      type: string
      required: true
      validation: { max_length: 20 }

    - logical_name: LanguagePreference
      pdf_field: Language_Preference
      source_path: source.member.language_preference
      type: string
      required: false
      validation: { enum_ref: "languages_common" }

    - logical_name: SignatureMember
      pdf_field: Signature_Member
      source_path: source.signatures.member.signature_image
      type: image
      required: true
      validation:
        content_type: ["image/png", "image/jpeg"]

    - logical_name: SignatureDate
      pdf_field: Signature_Date
      source_path: source.signatures.member.signed_at
      type: date
      required: true
      transforms:
        - format_date: "MM/dd/yyyy"

    # --------- Market (Medicare) fields ---------
    - logical_name: MedicareBeneficiaryId
      pdf_field: Medicare_Beneficiary_ID
      source_path: source.member.medicare.mbi
      type: string
      required: true
      transforms: [trim, upper]
      validation:
        pattern: "^[A-Z0-9]{11}$"

    - logical_name: MedicareConsent
      pdf_field: Medicare_Consent_Checkbox
      source_path: source.member.medicare.consent
      type: boolean
      required: true

    # --------- State (CA) fields ---------
    - logical_name: County
      pdf_field: County
      source_path: source.member.address.residential.county
      type: string
      required: false
      transforms: [title_case]

    - logical_name: StateConsentCA
      pdf_field: CA_State_Consent
      source_path: source.state_specific.ca.consent
      type: boolean
      required: true

    - logical_name: AB1672DisclosureAck
      pdf_field: CA_AB1672_Disclosure_Acknowledgment
      source_path: source.state_specific.ca.ab1672_ack
      type: boolean
      required: false

    # --------- Template-specific additions ---------
    - logical_name: BrokerAttestation
      pdf_field: Broker_Attestation
      source_path: source.broker.attestation
      type: boolean
      required: false

  codelists:
    states_us: ["AL","AK","AZ","AR","CA","CO","CT","DE","FL","GA","HI","IA","ID","IL","IN","KS","KY","LA","MA","MD","ME","MI","MN","MO","MS","MT","NC","ND","NE","NH","NJ","NM","NV","NY","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VA","VT","WA","WI","WV","WY","DC"]
    languages_common: ["English","Spanish","Chinese","Tagalog","Vietnamese","Korean","Arabic","Russian","Hindi","Other"]

  transforms:
    trim: {}
    upper: {}
    title_case: {}
    remove_non_digits: {}
    format_date: {args: ["pattern"]}
    normalize_phone_us: {}
    upper_if_required: {args: ["flag_path"]}
    coalesce: {args: ["pathA","pathB"]}

  suppression_log:
    - logical_name: Gender
      reason: "Not captured in this template"
```

***

## 4) Notes on Execution & Maintenance

*   **Execution flow:** Your mapping engine should:
    1.  Load `catalogs.common`, `catalogs.market.Medicare`, `catalogs.state.CA`.
    2.  **Merge** fields by `logical_name` (dedupe).
    3.  Apply `overrides` and `suppress` from `template_profile`.
    4.  Add any `additional` fields from the template.
    5.  Emit `resolved_mapping` to drive the AcroForm population.

*   **Avoid duplication:** Common and codelists are defined once; state/market specifics live in separate catalogs and are reused across templates.

*   **Validation:** Keep pattern/enum checks close to the fields to ensure quality at fill time and early error detection.

*   **Versioning:** Bump `catalog_version` when catalogs change; bump `template_profile.version` when the template evolves (e.g., new PDF field, consent text update).

*   **Readability:** Logical names stay stable even if `pdf_field` changes between template versions—this helps with future refactors.

***

## 5) Optional: Minimal Engine Resolution Pseudocode

```pseudo
function resolveMapping(templateProfile, catalogs):
  base = []
  for inheritRef in templateProfile.inherits:
    base += catalogs.resolve(inheritRef).fields

  # Deduplicate by logical_name (last wins)
  fieldsByLogical = dict()
  for f in base:
    fieldsByLogical[f.logical_name] = f

  # Apply overrides
  for o in templateProfile.overrides:
    ln = o.target_logical_name
    fieldsByLogical[ln] = merge(fieldsByLogical[ln], o.replace)

  # Suppress
  for s in templateProfile.suppress:
    remove fieldsByLogical[s.logical_name]

  # Add additional
  for a in templateProfile.additional:
    fieldsByLogical[a.logical_name] = a

  return fieldsByLogical.values()
```

***

