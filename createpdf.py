from fpdf import FPDF

pdf = FPDF()
pdf.add_page()
pdf.set_font("Arial", size=12)

# Insert unique fact blocks for your RAG system to find
text_content = """
Project Orion Architecture Protocol:
1. The secondary backup reactor password is Alpha-7741.
2. Sarah Jenkins is the chief network security engineer.
3. System updates are pushed every Tuesday at 3:00 AM EST.
"""

for line in text_content.strip().split("\n"):
    pdf.cell(200, 10, txt=line, ln=True)

# Save it directly into your source documents folder!
pdf.output("source_documents/orion_specs.pdf")
print("✅ Created source_documents/orion_specs.pdf successfully!")
