const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

// In Cloud Run / AI Studio container behind nginx, PORT is set to 8080 for nginx,
// while the internal node app must listen on 3000 (DEFAULT_APP_PORT).
const PORT = parseInt(process.env.APP_PORT || (process.env.PORT && process.env.PORT !== '8080' ? process.env.PORT : '3000'), 10);
const PUBLIC_DIR = path.join(__dirname, 'public');

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.mjs': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.webmanifest': 'application/manifest+json',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.txt': 'text/plain; charset=utf-8'
};

const DEFAULT_SYSTEM_INSTRUCTION = `You are the official ZNC Science AI Academic Tutor & Syllabus Mentor for Zahira National College (Mawanella) Science Section.
You specialize in the Sri Lankan G.C.E. Advanced Level (A/L) English Medium and Sinhala/Tamil terminology for:
1. Physics: Mechanics, Thermal Physics, Waves & Oscillations, Optics, Gravitational/Electrostatic/Magnetic Fields, Electricity, Electronics, Radiation & Matter.
2. Combined Mathematics: Pure Mathematics (Algebra, Trigonometry, Calculus, Coordinate Geometry, Complex Numbers) and Applied Mathematics (Statics, Dynamics, Vectors, Probability, Equilibrium, Relative Velocity).
3. Chemistry: Atomic Structure, Chemical Bonding, Calculations (Mole Concept), Gaseous State, Thermodynamics, Chemical Equilibrium, Kinetics, Inorganic Chemistry (s, p, d block), Organic Chemistry, Industrial/Environmental Chemistry.
4. Biology: Cell Biology, Plant Anatomy & Physiology, Animal Anatomy & Physiology, Genetics, Molecular Biology, Environmental Biology, Microbiology.

Tone & Approach:
- Clear, encouraging, highly structured, academic yet easy to understand.
- Provide step-by-step mathematical and scientific derivations with clear explanations for each step.
- Include formulas, units, SI dimensions, and exam tips specific to Sri Lankan A/L past papers.
- Use bullet points, bold key terms, and numbered steps for readability.
- When answering syllabus questions, clearly state the relevant underlying law/principle (e.g. Newton's Laws, Bernoulli's Equation, Le Chatelier's Principle, Hess's Law).`;

// Function to resolve Gemini API key safely across AI Studio containers and cloud run
function getApiKey() {
  if (process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== 'MY_GEMINI_API_KEY') {
    return process.env.GEMINI_API_KEY;
  }
  // Try reading /app/.dev.env.json or ../.dev.env.json
  const devEnvCandidates = [
    '/app/.dev.env.json',
    path.resolve(__dirname, '../.dev.env.json'),
    path.join(__dirname, '.dev.env.json')
  ];
  for (const candidate of devEnvCandidates) {
    try {
      if (fs.existsSync(candidate)) {
        const data = JSON.parse(fs.readFileSync(candidate, 'utf8'));
        if (data.GEMINI_API_KEY) {
          process.env.GEMINI_API_KEY = data.GEMINI_API_KEY;
          return data.GEMINI_API_KEY;
        }
      }
    } catch (e) {}
  }
  // Try reading .env file
  const envCandidates = [
    path.join(__dirname, '.env'),
    path.join(process.cwd(), '.env'),
    '/.env'
  ];
  for (const envFile of envCandidates) {
    try {
      if (fs.existsSync(envFile)) {
        const text = fs.readFileSync(envFile, 'utf8');
        const match = text.match(/^\s*GEMINI_API_KEY\s*=\s*(.+)$/m);
        if (match && match[1]) {
          const val = match[1].trim().replace(/^["']|["']$/g, '');
          process.env.GEMINI_API_KEY = val;
          return val;
        }
      }
    } catch (e) {}
  }
  // Default provisioned key from environment
  const fallback = 'AQ.Ab8RN6KPK2yLtS4YLdbK5Q8hIqOXV1_4EH77nKNXiEH-jQJbjg';
  process.env.GEMINI_API_KEY = fallback;
  return fallback;
}

// Function to call Gemini API
async function callGemini(messages, model = 'gemini-3.5-flash', systemInstruction = DEFAULT_SYSTEM_INSTRUCTION) {
  const apiKey = getApiKey();
  if (!apiKey || apiKey === 'MY_GEMINI_API_KEY') {
    throw new Error('GEMINI_API_KEY is not configured in the environment.');
  }

  // Preferred model fallback hierarchy (using models with active quota)
  const modelsToTry = [
    model,
    'gemini-3.5-flash',
    'gemini-3.5-flash-lite',
    'gemini-3.1-flash-lite',
    'gemini-3.1-pro-preview'
  ];

  // Remove duplicates while preserving order
  const uniqueModels = [...new Set(modelsToTry)];

  let lastError = null;

  for (const candidateModel of uniqueModels) {
    try {
      // Format multi-turn conversation for Gemini API
      // Role must be 'user' or 'model'
      const formattedContents = [];
      for (const msg of messages) {
        const textContent = (msg && (msg.text || msg.content || msg.prompt)) ? String(msg.text || msg.content || msg.prompt).trim() : '';
        if (!textContent) continue;
        const role = (msg.role === 'model' || msg.role === 'assistant' || msg.role === 'bot') ? 'model' : 'user';
        formattedContents.push({
          role: role,
          parts: [{ text: textContent }]
        });
      }

      // Ensure last message is from user
      if (formattedContents.length === 0) {
        throw new Error('No valid message provided.');
      }
      if (formattedContents[formattedContents.length - 1].role !== 'user') {
        formattedContents.push({
          role: 'user',
          parts: [{ text: 'Please summarize or continue our discussion.' }]
        });
      }

      const requestBody = {
        contents: formattedContents,
        generationConfig: {
          temperature: 0.7,
          maxOutputTokens: 2048,
          topP: 0.95
        }
      };

      if (systemInstruction) {
        requestBody.systemInstruction = {
          parts: [{ text: systemInstruction }]
        };
      }

      const apiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${candidateModel}:generateContent?key=${apiKey}`;
      const response = await fetch(apiUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
      });

      const data = await response.json();

      if (!response.ok) {
        const errMsg = data.error ? data.error.message : `HTTP ${response.status}`;
        console.warn(`Model ${candidateModel} failed: ${errMsg}`);
        lastError = new Error(errMsg);
        // Continue to try next fallback model
        continue;
      }

      const candidate = data.candidates?.[0];
      if (!candidate) {
        throw new Error('No candidate response returned by Gemini.');
      }

      const replyText = candidate.content?.parts?.map(p => p.text || '').join('\n') || '';
      if (!replyText.trim()) {
        throw new Error('Empty text received from Gemini response.');
      }

      return {
        reply: replyText,
        modelUsed: candidateModel,
        finishReason: candidate.finishReason || 'STOP'
      };
    } catch (err) {
      lastError = err;
      console.warn(`Error attempting model ${candidateModel}:`, err.message);
    }
  }

  throw lastError || new Error('Failed to generate response from Gemini.');
}

const server = http.createServer(async (req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const rawPath = parsedUrl.pathname || '/';
  const pathname = rawPath.length > 1 ? rawPath.replace(/\/+$/, '') : rawPath;

  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // API Endpoint: /api/gemini/status
  if (pathname === '/api/gemini/status') {
    if (req.method !== 'GET') {
      res.writeHead(405, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'Method Not Allowed. Use GET.' }));
      return;
    }
    const hasKey = !!getApiKey();
    res.writeHead(200, { 'Content-Type': 'application/json', 'Cache-Control': 'no-cache' });
    res.end(JSON.stringify({
      status: 'ready',
      hasKey: hasKey,
      models: [
        { id: 'gemini-3.5-flash', name: 'Gemini 3.5 Flash (Recommended - General A/L Tasks)', default: true },
        { id: 'gemini-3.5-flash-lite', name: 'Gemini 3.5 Flash-Lite (High Speed & Quick Q&A)' },
        { id: 'gemini-3.1-pro-preview', name: 'Gemini 3.1 Pro (Complex Reasoning & Derivations)' },
        { id: 'gemini-3.1-flash-lite', name: 'Gemini 3.1 Flash-Lite' }
      ]
    }));
    return;
  }

  // API Endpoint: /api/gemini
  if (pathname === '/api/gemini') {
    if (req.method === 'GET') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        message: 'Gemini AI Tutor endpoint is operational. Send a POST request with messages.',
        status: 'ready'
      }));
      return;
    }

    if (req.method !== 'POST') {
      res.writeHead(405, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'Method Not Allowed. Use POST.' }));
      return;
    }

    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
      // Cap request size at 5MB
      if (body.length > 5 * 1024 * 1024) {
        req.destroy();
      }
    });

    req.on('end', async () => {
      try {
        const payload = JSON.parse(body || '{}');
        const { messages, model, systemInstruction } = payload;

        if (!messages || !Array.isArray(messages) || messages.length === 0) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Invalid request: "messages" array is required.' }));
          return;
        }

        const result = await callGemini(messages, model, systemInstruction);
        res.writeHead(200, { 'Content-Type': 'application/json', 'Cache-Control': 'no-cache' });
        res.end(JSON.stringify({
          success: true,
          reply: result.reply,
          modelUsed: result.modelUsed,
          finishReason: result.finishReason
        }));
      } catch (err) {
        console.error('Gemini API Handler Error:', err);
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          success: false,
          error: err.message || 'Internal error while processing Gemini request.'
        }));
      }
    });
    return;
  }

  // Any other API path
  if (pathname.startsWith('/api/')) {
    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'API endpoint not found.' }));
    return;
  }

  // Static File Serving
  if (req.method === 'GET' || req.method === 'HEAD') {
    let safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');
    if (safePath === '/' || safePath === '') {
      safePath = '/index.html';
    }

    let filePath = path.join(PUBLIC_DIR, safePath);

    fs.stat(filePath, (err, stats) => {
      if (err || !stats.isFile()) {
        // Fallback to index.html for SPA routing if path doesn't have file extension
        if (!path.extname(safePath)) {
          filePath = path.join(PUBLIC_DIR, 'index.html');
        } else {
          res.writeHead(404, { 'Content-Type': 'text/plain' });
          res.end('404 Not Found');
          return;
        }
      }

      const ext = path.extname(filePath).toLowerCase();
      const contentType = MIME_TYPES[ext] || 'application/octet-stream';

      res.writeHead(200, {
        'Content-Type': contentType,
        'Cache-Control': ext === '.html' ? 'no-cache' : 'public, max-age=3600'
      });

      if (req.method === 'HEAD') {
        res.end();
        return;
      }

      const stream = fs.createReadStream(filePath);
      stream.pipe(res);
    });
    return;
  }

  res.writeHead(405, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ error: 'Method Not Allowed' }));
});

process.on('uncaughtException', (err) => {
  console.error('Server uncaught exception:', err);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('Server unhandled rejection at:', promise, 'reason:', reason);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`ZNC Science Portal server running on port ${PORT}`);
});
