<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Invoice ${invoiceNo}</title>
  ${asset(
</head>
<body>
  <header>
    ${asset(
    <h1>Invoice ${invoiceNo}</h1>
    <p>Date: ${date}</p>
  </header>

  <h3>Bill To</h3>
  <p>${billTo.name}<br/>${billTo.address?html}</p>

  <table>
    <thead><tr><th>Description</th><th>Qty</th><th>Price</th></tr></thead>
    <tbody>
    <#list items as it>
      <tr>
        <td>${it.desc?html}</td>
        <td>${it.qty}</td>
        <td>${it.price?string["0.00"]}</td>
      </tr>
    </#list>
    </tbody>
  </table>

  <footer>Total: ${totals.grandTotal?string["0.00"]}</footer>
</body>
</html>
