.Below is a **complete, realistic Healthcare Enrollment YAML mapping specification**, designed for processing a typical U.S. medical/insurance enrollment payload (member info, dependents, coverage selections, PCP, address, etc.).
This YAML follows the mapping-engine structure we established:

* `expr:` — JSONPath or JSONata
* `transform:` — list of Java transforms (masking, date decomposition, formatting, etc.)
* Handles: primary subscriber, dependents, selected coverage, PCP, employer, etc.

---

# ✅ **Healthcare Enrollment – Mapping YAML (Realistic, Production-Style)**

```yaml
template:
  file: "healthcare_enrollment_form.pdf"
  location: "classpath:/forms"

fields:

  ####################################################################
  # Primary Subscriber (Member)
  ####################################################################

  memberFirstName:
    expr: "$.subscriber.name.first"

  memberLastName:
    expr: "$.subscriber.name.last"

  memberMiddleInitial:
    expr: "$.subscriber.name.middle"
    transform: ["firstCharOrBlank"]

  memberMaskedSSN:
    expr: "$.subscriber.ssn"
    transform: ["maskSSN"]

  memberDOB_Day:
    expr: "$.subscriber.dob"
    transform: ["parseDate:day"]

  memberDOB_Month:
    expr: "$.subscriber.dob"
    transform: ["parseDate:month"]

  memberDOB_Year:
    expr: "$.subscriber.dob"
    transform: ["parseDate:year"]

  memberGender:
    expr: "$.subscriber.gender"
    transform: ["uppercase"]

  ####################################################################
  # Contact Information
  ####################################################################

  homePhone:
    expr: "$.subscriber.phones[?(@.type=='HOME')].number"

  mobilePhone:
    expr: "$.subscriber.phones[?(@.type=='MOBILE')].number"

  email:
    expr: "$.subscriber.email"

  ####################################################################
  # Address (Primary)
  ####################################################################

  addrLine1:
    expr: "$.subscriber.address.line1"

  addrLine2:
    expr: "$.subscriber.address.line2"

  city:
    expr: "$.subscriber.address.city"

  state:
    expr: "$.subscriber.address.state"

  zipCode:
    expr: "$.subscriber.address.zip"

  ####################################################################
  # Employer / Group Information
  ####################################################################

  employerName:
    expr: "$.employer.name"

  employerGroupNumber:
    expr: "$.employer.groupNumber"

  employerPhone:
    expr: "$.employer.contact.phone"

  ####################################################################
  # Coverage Selection
  ####################################################################
  
  selectedMedicalPlan:
    expr: "$.coverage.medical.planName"

  selectedDentalPlan:
    expr: "$.coverage.dental.planName"

  selectedVisionPlan:
    expr: "$.coverage.vision.planName"

  medicalEffectiveDate_Day:
    expr: "$.coverage.medical.effectiveDate"
    transform: ["parseDate:day"]

  medicalEffectiveDate_Month:
    expr: "$.coverage.medical.effectiveDate"
    transform: ["parseDate:month"]

  medicalEffectiveDate_Year:
    expr: "$.coverage.medical.effectiveDate"
    transform: ["parseDate:year"]

  ####################################################################
  # PCP (Primary Care Physician) Details
  ####################################################################

  pcpName:
    expr: "$.pcp.name"

  pcpId:
    expr: "$.pcp.id"

  pcpClinic:
    expr: "$.pcp.clinic"

  pcpPhone:
    expr: "$.pcp.phone"

  ####################################################################
  # Dependents (Handles up to 3 dependents for this form)
  # Uses array indexing: dependents[0], dependents[1], dependents[2]
  ####################################################################

  # Dependent 1
  dep1FirstName:
    expr: "$.dependents[0].name.first"

  dep1LastName:
    expr: "$.dependents[0].name.last"

  dep1DOB_Day:
    expr: "$.dependents[0].dob"
    transform: ["parseDate:day"]

  dep1DOB_Month:
    expr: "$.dependents[0].dob"
    transform: ["parseDate:month"]

  dep1DOB_Year:
    expr: "$.dependents[0].dob"
    transform: ["parseDate:year"]

  dep1Relationship:
    expr: "$.dependents[0].relationship"

  dep1Gender:
    expr: "$.dependents[0].gender"
    transform: ["uppercase"]

  # Dependent 2
  dep2FirstName:
    expr: "$.dependents[1].name.first"

  dep2LastName:
    expr: "$.dependents[1].name.last"

  dep2DOB_Day:
    expr: "$.dependents[1].dob"
    transform: ["parseDate:day"]

  dep2DOB_Month:
    expr: "$.dependents[1].dob"
    transform: ["parseDate:month"]

  dep2DOB_Year:
    expr: "$.dependents[1].dob"
    transform: ["parseDate:year"]

  dep2Relationship:
    expr: "$.dependents[1].relationship"

  dep2Gender:
    expr: "$.dependents[1].gender"
    transform: ["uppercase"]

  # Dependent 3
  dep3FirstName:
    expr: "$.dependents[2].name.first"

  dep3LastName:
    expr: "$.dependents[2].name.last"

  dep3DOB_Day:
    expr: "$.dependents[2].dob"
    transform: ["parseDate:day"]

  dep3DOB_Month:
    expr: "$.dependents[2].dob"
    transform: ["parseDate:month"]

  dep3DOB_Year:
    expr: "$.dependents[2].dob"
    transform: ["parseDate:year"]

  dep3Relationship:
    expr: "$.dependents[2].relationship"

  dep3Gender:
    expr: "$.dependents[2].gender"
    transform: ["uppercase"]

  ####################################################################
  # Attestation & Signature Section
  ####################################################################

  signatureName:
    expr: "$.subscriber.name.full"

  signatureDate_Day:
    expr: "$.submissionDate"
    transform: ["parseDate:day"]

  signatureDate_Month:
    expr: "$.submissionDate"
    transform: ["parseDate:month"]

  signatureDate_Year:
    expr: "$.submissionDate"
    transform: ["parseDate:year"]
```

---

# ✅ **What This YAML Covers**

| Section                                     | Included? |
| ------------------------------------------- | --------- |
| Subscriber demographics                     | ✔         |
| Masked SSN                                  | ✔         |
| DOB → Day/Month/Year                        | ✔         |
| Address                                     | ✔         |
| Phones (home/mobile)                        | ✔         |
| Employer/group details                      | ✔         |
| Coverage selections (medical/dental/vision) | ✔         |
| PCP (Primary Care Provider)                 | ✔         |
| Multiple dependents                         | ✔         |
| Signature/attestation                       | ✔         |

---

# 👉 If you want next:

I can generate **realistic sample JSON input** matching this YAML:

➡ Subscriber
➡ Employer
➡ Coverage
➡ Dependents
➡ PCP
➡ Submission metadata

OR:

* Produce **multiple enrollment YAMLs** for:
  ✓ Medical only
  ✓ Dental-only
  ✓ Medicare
  ✓ Medicaid
  ✓ Employer-sponsored group coverage

Just tell me **“Give me the sample JSON”** or **“Give me Medicare YAML”**.
