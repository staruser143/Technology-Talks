// Create web server
const express = require('express');
const app = express();
const port = 3000;

// Import comments data
const comments = require('./comments.json');

// Serve comments data
app.get('/comments', (req, res) => {
  res.json(comments);
});

// Start the server
app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
});
