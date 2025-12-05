<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <style>
        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            border: 1px solid #333;
            padding: 5px;
            text-align: center;
        }

        /* Give header row space for rotation */
        th {
            height: 140px;
            overflow: visible !important;
        }

        /* Inner div that we actually rotate */
        .vertical-header {
            display: inline-block;
            transform: rotate(-90deg);
            transform-origin: left bottom;
            white-space: nowrap;
        }
    </style>
</head>

<body>
<h2>Sample Table with Vertical Headers</h2>
<table>
    <thead>
    <tr>
        <th><div class="vertical-header">Employee Name</div></th>
        <th><div class="vertical-header">Department</div></th>
        <th><div class="vertical-header">Joining Date</div></th>
        <th><div class="vertical-header">Active Status</div></th>
    </tr>
    </thead>

    <tbody>
    <#list employees as emp>
        <tr>
            <td>${emp.name}</td>
            <td>${emp.department}</td>
            <td>${emp.joiningDate}</td>
            <td>${emp.active?string("Yes","No")}</td>
        </tr>
    </#list>
    </tbody>
</table>

</body>
</html>