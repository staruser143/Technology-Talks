import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.Option;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class JsonPathExamples {
    
    // Sample nested JSON payload
    private static final String JSON_PAYLOAD = """
    {
        "company": {
            "name": "Tech Corp",
            "founded": 2010,
            "departments": [
                {
                    "name": "Engineering",
                    "employees": [
                        {
                            "id": 1,
                            "name": "Alice",
                            "skills": ["Java", "Python"],
                            "salary": 85000
                        },
                        {
                            "id": 2,
                            "name": "Bob",
                            "skills": ["JavaScript", "React"],
                            "salary": 75000
                        }
                    ]
                },
                {
                    "name": "Sales",
                    "employees": [
                        {
                            "id": 3,
                            "name": "Charlie",
                            "skills": ["Negotiation"],
                            "salary": 65000
                        }
                    ]
                }
            ]
        },
        "projects": [
            {
                "id": 101,
                "name": "Project Alpha",
                "team": {
                    "lead": "Alice",
                    "members": ["Bob", "Charlie"]
                },
                "budget": 100000
            },
            {
                "id": 102,
                "name": "Project Beta",
                "team": {
                    "lead": "Bob",
                    "members": ["Alice"]
                },
                "budget": 150000
            }
        ]
    }
    """;
    
    public static void main(String[] args) {
        System.out.println("=== HANDLING OPTIONAL/MISSING FIELDS ===\n");
        
        // Parse once for multiple queries
        DocumentContext context = JsonPath.parse(JSON_PAYLOAD);
        
        // METHOD 1: Try-Catch for missing paths
        handleWithTryCatch(context);
        
        // METHOD 2: Configure with default options
        handleWithConfiguration();
        
        // METHOD 3: Check before accessing
        handleWithConditionalCheck(context);
        
        System.out.println("\n=== BASIC EXAMPLES ===\n");
        basicExamples(context);
    }
    
    // METHOD 1: Using try-catch to handle missing fields
    private static void handleWithTryCatch(DocumentContext context) {
        System.out.println("--- Method 1: Try-Catch ---");
        
        // This field doesn't exist in the payload
        try {
            String address = context.read("$.company.address");
            System.out.println("Company Address: " + address);
        } catch (PathNotFoundException e) {
            System.out.println("Company Address: Field not found, using default");
            String address = "No address provided";
            System.out.println("Company Address: " + address);
        }
        
        // Nested optional field
        try {
            String city = context.read("$.company.headquarters.city");
            System.out.println("HQ City: " + city);
        } catch (PathNotFoundException e) {
            System.out.println("HQ City: Not available");
        }
        
        // Optional array element
        try {
            String thirdDept = context.read("$.company.departments[2].name");
            System.out.println("Third Department: " + thirdDept);
        } catch (PathNotFoundException e) {
            System.out.println("Third Department: Index out of bounds");
        }
        
        // Optional nested property in array
        try {
            List<String> phoneNumbers = context.read("$.company.departments[*].employees[*].phone");
            System.out.println("Phone Numbers: " + phoneNumbers);
        } catch (PathNotFoundException e) {
            System.out.println("Phone Numbers: Field doesn't exist");
        }
        
        System.out.println();
    }
    
    // METHOD 2: Using Configuration with Options
    private static void handleWithConfiguration() {
        System.out.println("--- Method 2: Configuration with Options ---");
        
        // Configure to return null instead of throwing exception
        Configuration conf = Configuration.builder()
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build();
        
        DocumentContext contextWithOptions = JsonPath.using(conf).parse(JSON_PAYLOAD);
        
        // These will return null instead of throwing exception
        String address = contextWithOptions.read("$.company.address");
        System.out.println("Company Address: " + (address != null ? address : "Not provided"));
        
        String website = contextWithOptions.read("$.company.website");
        System.out.println("Company Website: " + (website != null ? website : "Not available"));
        
        // SUPPRESS_EXCEPTIONS option returns empty list for missing arrays
        Configuration confSuppressExceptions = Configuration.builder()
            .options(Option.SUPPRESS_EXCEPTIONS)
            .build();
        
        DocumentContext safeParse = JsonPath.using(confSuppressExceptions).parse(JSON_PAYLOAD);
        
        List<String> certifications = safeParse.read("$.company.departments[*].employees[*].certifications");
        System.out.println("Certifications: " + (certifications != null ? certifications : "No data"));
        
        // ALWAYS_RETURN_LIST - returns empty list instead of exception
        Configuration confAlwaysList = Configuration.builder()
            .options(Option.ALWAYS_RETURN_LIST)
            .build();
        
        DocumentContext listContext = JsonPath.using(confAlwaysList).parse(JSON_PAYLOAD);
        
        List<String> nonExistentField = listContext.read("$.company.nonExistent");
        System.out.println("Non-existent field as list: " + nonExistentField + " (size: " + nonExistentField.size() + ")");
        
        System.out.println();
    }
    
    // METHOD 3: Conditional check with filter or validation
    private static void handleWithConditionalCheck(DocumentContext context) {
        System.out.println("--- Method 3: Conditional Checks ---");
        
        // Use filters to check if field exists
        List<Map<String, Object>> employeesWithPhone = context.read(
            "$.company.departments[*].employees[?(@.phone)]"
        );
        System.out.println("Employees with phone field: " + employeesWithPhone.size());
        
        // Get employees and safely access optional fields
        List<Map<String, Object>> allEmployees = context.read(
            "$.company.departments[*].employees[*]"
        );
        
        System.out.println("\nSafely accessing optional fields:");
        for (Map<String, Object> employee : allEmployees) {
            String name = (String) employee.get("name");
            String email = (String) employee.getOrDefault("email", "no-email@company.com");
            String phone = (String) employee.getOrDefault("phone", "N/A");
            
            System.out.println("  " + name + " - Email: " + email + ", Phone: " + phone);
        }
        
        // Check if specific path exists by getting parent and checking keys
        Map<String, Object> company = context.read("$.company");
        boolean hasAddress = company.containsKey("address");
        System.out.println("\nCompany has address field: " + hasAddress);
        
        System.out.println();
    }
    
    // Original basic examples
    private static void basicExamples(DocumentContext context) {
        
        // Example 1: Access nested object property
        String companyName = context.read("$.company.name");
        System.out.println("Company Name: " + companyName);
        
        // Example 2: Access element in nested array
        String firstDeptName = context.read("$.company.departments[0].name");
        System.out.println("First Department: " + firstDeptName);
        
        // Example 3: Get all department names
        List<String> deptNames = context.read("$.company.departments[*].name");
        System.out.println("All Departments: " + deptNames);
        
        // Example 4: Get all employee names across all departments
        List<String> allEmployees = context.read("$.company.departments[*].employees[*].name");
        System.out.println("All Employees: " + allEmployees);
        
        // Example 5: Use recursive descent to find all 'name' fields
        List<String> allNames = context.read("$..name");
        System.out.println("All Names (recursive): " + allNames);
        
        // Example 6: Filter employees with salary > 70000
        List<Map<String, Object>> highEarners = context.read(
            "$.company.departments[*].employees[?(@.salary > 70000)]"
        );
        System.out.println("High Earners: " + highEarners);
        
        // Example 7: Get specific employee by ID across all departments
        List<String> employeeName = context.read(
            "$.company.departments[*].employees[?(@.id == 2)].name"
        );
        System.out.println("Employee with ID 2: " + employeeName);
        
        // Example 8: Get all skills from all employees (flattened)
        List<String> allSkills = context.read("$..employees[*].skills[*]");
        System.out.println("All Skills: " + allSkills);
        
        // Example 9: Filter projects with budget >= 120000
        List<String> bigProjects = context.read(
            "$.projects[?(@.budget >= 120000)].name"
        );
        System.out.println("Big Budget Projects: " + bigProjects);
        
        // Example 10: Get team members from all projects
        List<List<String>> allTeamMembers = context.read("$.projects[*].team.members");
        System.out.println("All Team Members: " + allTeamMembers);
        
        // Example 11: Get project leads where Alice is a member
        List<String> projectLeads = context.read(
            "$.projects[?(@.team.members[*] == 'Alice')].team.lead"
        );
        System.out.println("Project Leads (Alice as member): " + projectLeads);
        
        // Example 12: Complex filter - employees with Java skill
        List<String> javaDevs = context.read(
            "$.company.departments[*].employees[?(@.skills[*] == 'Java')].name"
        );
        System.out.println("Java Developers: " + javaDevs);
        
        // Example 13: Get first employee from each department
        List<String> firstEmployees = context.read(
            "$.company.departments[*].employees[0].name"
        );
        System.out.println("First Employee per Dept: " + firstEmployees);
        
        // Example 14: Array slicing - get first 2 projects
        List<Map<String, Object>> firstTwoProjects = context.read("$.projects[0:2]");
        System.out.println("First Two Projects: " + firstTwoProjects.size() + " projects");
        
        // Example 15: Multiple conditions in filter
        List<String> specificEmployees = context.read(
            "$.company.departments[*].employees[?(@.salary > 60000 && @.salary < 80000)].name"
        );
        System.out.println("Employees (60k-80k salary): " + specificEmployees);
        
        // Example 16: Get nested array length
        Integer employeeCount = context.read(
            "$.company.departments[0].employees.length()"
        );
        System.out.println("Engineering Employee Count: " + employeeCount);
    }
}

/* 
Maven Dependency:
<dependency>
    <groupId>com.jayway.jsonpath</groupId>
    <artifactId>json-path</artifactId>
    <version>2.8.0</version>
</dependency>

Gradle Dependency:
implementation 'com.jayway.jsonpath:json-path:2.8.0'

KEY STRATEGIES FOR OPTIONAL FIELDS:

1. Try-Catch: Good for single field access, explicit error handling
   - Catches PathNotFoundException
   - Allows custom fallback logic

2. Configuration Options:
   - DEFAULT_PATH_LEAF_TO_NULL: Returns null for missing leaf nodes
   - SUPPRESS_EXCEPTIONS: Silently handles errors, returns null
   - ALWAYS_RETURN_LIST: Returns empty list instead of exception
   - REQUIRE_PROPERTIES: Throws exception if property missing (strict mode)

3. Conditional Checks:
   - Use filters [?(@.field)] to find objects with specific fields
   - Get parent object and check with containsKey()
   - Use getOrDefault() on resulting Map objects

4. Best Practices:
   - Use Configuration for consistent behavior across many queries
   - Use try-catch for specific fields with custom fallback logic
   - Use filters when you only want objects that have certain fields
   - Consider using Optional<T> wrapper for cleaner code
*/